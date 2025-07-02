package win.blade.core.module.storage.combat;

import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Hand;
import org.lwjgl.glfw.GLFW;
import win.blade.common.gui.impl.menu.settings.impl.BindSetting;
import win.blade.core.event.controllers.EventHandler;
import win.blade.core.event.impl.minecraft.UpdateEvents;
import win.blade.core.module.api.Category;
import win.blade.core.module.api.Module;
import win.blade.core.module.api.ModuleInfo;

/**
 * Автор: NoCap
 * Дата создания: 02.07.2025
 */
@ModuleInfo(
        name = "WindHop",
        category = Category.COMBAT,
        desc = "Кидает под себя заряд ветра"
)
public class WindHop extends Module {

    private final BindSetting bind = new BindSetting(this, "Key", GLFW.GLFW_KEY_V);

    private int previousSlot = -1;
    private long lastActionTime;
    private boolean wasKeyPressed;
    private boolean shouldThrow;

    @EventHandler
    public void onTick(UpdateEvents.PlayerUpdate event) {
        if (mc.player == null || mc.interactionManager == null) return;

        if (bind.getValue() == -1) {
            wasKeyPressed = false;
            return;
        }

        if (previousSlot != -1 && System.currentTimeMillis() - lastActionTime > 1) {
            mc.player.getInventory().selectedSlot = previousSlot;
            previousSlot = -1;
        }

        if (shouldThrow && System.currentTimeMillis() - lastActionTime > 1) {
            throwWindCharge();
            shouldThrow = false;
            lastActionTime = System.currentTimeMillis();
        }

        boolean isKeyPressed = GLFW.glfwGetKey(mc.getWindow().getHandle(), bind.getValue()) == GLFW.GLFW_PRESS;

        if (isKeyPressed && !wasKeyPressed) {
            prepareToThrow();
        }

        wasKeyPressed = isKeyPressed;
    }

    private void prepareToThrow() {
        int windChargeSlot = findWindChargeSlot();
        if (windChargeSlot == -1) return;

        previousSlot = mc.player.getInventory().selectedSlot;

        mc.player.getInventory().selectedSlot = windChargeSlot;
        shouldThrow = true;
        lastActionTime = System.currentTimeMillis();
    }

    private void throwWindCharge() {
        mc.player.networkHandler.sendPacket(new PlayerInteractItemC2SPacket(
                Hand.MAIN_HAND,
                0,
                0,
                90
        ));
        mc.player.swingHand(Hand.MAIN_HAND);
    }

    private int findWindChargeSlot() {
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == Items.WIND_CHARGE) {
                return i;
            }
        }
        return -1;
    }

    @Override
    protected void onDisable() {
        previousSlot = -1;
        shouldThrow = false;
        wasKeyPressed = false;
    }
}