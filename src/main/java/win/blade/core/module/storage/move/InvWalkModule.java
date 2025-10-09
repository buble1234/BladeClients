package win.blade.core.module.storage.move;

import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;
import win.blade.common.utils.math.TimerUtil;
import win.blade.common.utils.network.PacketUtility;
import win.blade.common.utils.network.ServerUtility;
import win.blade.common.utils.player.MovementUtility;
import win.blade.core.event.controllers.EventHandler;
import win.blade.core.event.impl.minecraft.UpdateEvents;
import win.blade.core.event.impl.network.PacketEvent;
import win.blade.core.event.impl.player.PlayerActionEvents;
import win.blade.core.module.api.Category;
import win.blade.core.module.api.Module;
import win.blade.core.module.api.ModuleInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@ModuleInfo(
        name = "InvWalk",
        category = Category.MOVE,
        desc = "Позволяет двигаться в открытых интерфейсах."
)
public class InvWalkModule extends Module {
    private final List<Packet<?>> packets = Collections.synchronizedList(new ArrayList<>());
    private ScheduledExecutorService scheduler;
    private final TimerUtil wait = new TimerUtil();
    private KeyBinding[] keys;

    @Override
    protected void onEnable() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        super.onEnable();
    }

    @EventHandler
    public void onTick(UpdateEvents.Update event) {
        if (mc.player == null || mc.currentScreen instanceof ChatScreen || mc.currentScreen instanceof CreativeInventoryScreen) {
            return;
        }

        if (keys == null && mc.options != null) {
            keys = new KeyBinding[]{
                    mc.options.forwardKey,
                    mc.options.backKey,
                    mc.options.leftKey,
                    mc.options.rightKey,
                    mc.options.jumpKey,
                    mc.options.sprintKey
            };
        }

        if (mc.currentScreen != null && !(mc.currentScreen instanceof ChatScreen) && keys != null) {
            long window = mc.getWindow().getHandle();
            for (KeyBinding key : keys) {
                if (key != null) {
                    boolean pressed = GLFW.glfwGetKey(window, key.getDefaultKey().getCode()) == GLFW.GLFW_PRESS;
                    key.setPressed(pressed);
                }
            }
        }
    }

    @EventHandler
    public void onPacket(PacketEvent.Send event) {
//        }
    }

    @EventHandler
    public void onClose(PlayerActionEvents.CloseInventory event) {
    }

    private void sendPackets() {
        synchronized (packets) {
            List<Packet<?>> packetsCopy = new ArrayList<>(packets);
            packets.clear();
            for (Packet<?> p : packetsCopy) {
                PacketUtility.sendPacket(p);
            }
        }
        PacketUtility.sendSprinting(true);
        PacketUtility.sendSneaking(false);

        if (keys != null && mc != null) {
            long window = mc.getWindow().getHandle();
            for (KeyBinding key : keys) {
                if (key != null) {
                    boolean pressed = GLFW.glfwGetKey(window, key.getDefaultKey().getCode()) == GLFW.GLFW_PRESS;
                    key.setPressed(pressed);
                }
            }
        }
    }

    @Override
    protected void onDisable() {
        if (mc.player != null) {
            if (!packets.isEmpty()) {
                sendPackets();
            }
            PacketUtility.sendSneaking(false);
            if (keys != null) {
                for (KeyBinding key : keys) {
                    if (key != null) {
                        key.setPressed(false);
                    }
                }
            }
        }
        if (scheduler != null) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(1, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        super.onDisable();
    }
}