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
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@ModuleInfo(
        name = "InvWalk",
        category = Category.MOVE,
        desc = "Позволяет передвигаться с открытым инвентарём"
)
public class InvWalkModule extends Module {
    private final List<Packet<?>> packets = new ArrayList<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final TimerUtil wait = new TimerUtil();
    private KeyBinding[] keys;

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

        int ping = PacketUtility.getPing();
        ping = MathHelper.clamp(ping, 125, 225);

        if (ServerUtility.isOnFuntime() && !packets.isEmpty() && !wait.hasReached(ping)) {
            PacketUtility.sendSneaking(true);
            PacketUtility.sendSprinting(false);
            for (KeyBinding key : keys) {
                key.setPressed(false);
            }
            return;
        }

        long window = mc.getWindow().getHandle();
        for (KeyBinding key : keys) {
            int code = key.getDefaultKey().getCode();
            boolean pressed = GLFW.glfwGetKey(window, code) == GLFW.GLFW_PRESS;
            key.setPressed(pressed);
        }
    }

    @EventHandler
    public void onPacket(PacketEvent.Send event) {
        if (ServerUtility.isOnFuntime() && mc.currentScreen instanceof InventoryScreen && MovementUtility.isMoving() && event.getPacket() instanceof ClickSlotC2SPacket pkt) {
            packets.add(pkt);
            event.cancel();
        }
    }

    @EventHandler
    public void onClose(PlayerActionEvents.CloseInventory event) {
        if (ServerUtility.isOnFuntime() && mc.currentScreen instanceof InventoryScreen && !packets.isEmpty() && MovementUtility.isMoving()) {
            wait.reset();
            if (!scheduler.isShutdown()) {
                scheduler.schedule(this::sendPackets, 100, TimeUnit.MILLISECONDS);
                event.cancel();
            } else {
                sendPackets();
            }
        }
    }

    private void sendPackets() {
        for (Packet<?> p : packets) {
            PacketUtility.sendPacket(p);
        }
        packets.clear();
        PacketUtility.sendSprinting(true);
        PacketUtility.sendSneaking(false);
    }

    @Override
    protected void onDisable() {
        if (mc.player != null) {
            if (!packets.isEmpty()) {
                sendPackets();
            }
            PacketUtility.sendSneaking(false);
            for (KeyBinding key : keys) {
                if (key != null) {
                    key.setPressed(false);
                }
            }
        }
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(1, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        super.onDisable();
    }
}