package win.blade.core.module.storage.move;

import org.lwjgl.glfw.GLFW;

import win.blade.common.utils.player.SprintUtility;
import win.blade.core.event.controllers.EventHandler;
import win.blade.core.event.impl.player.PlayerActionEvents;
import win.blade.core.event.impl.minecraft.UpdateEvent;
import win.blade.core.module.api.Category;
import win.blade.core.module.api.Module;
import win.blade.core.module.api.ModuleInfo;

@ModuleInfo(
        name = "AutoSprint",
        category = Category.MOVE,
        desc = "Автоматически включает спринт",
        bind = GLFW.GLFW_KEY_V
)
public class AutoSprintModule extends Module {

    @EventHandler
    public void onTick(UpdateEvent event) {
        if (mc.player == null) return;

        boolean canSprint = SprintUtility.canStartSprinting();

        if (canSprint && !mc.player.horizontalCollision) {
            if (!mc.player.isSprinting()) {
                mc.player.setSprinting(true);
            }
        } else if (!canSprint || SprintUtility.isEmergencyStop()) {
            if (mc.player.isSprinting()) {
                mc.player.setSprinting(false);
            }
        }

        SprintUtility.setEmergencyStop(false);
    }

    @EventHandler
    public void onKeepSprint(PlayerActionEvents.Attack event) {
        if (mc.player == null) return;

        if (SprintUtility.isKeepSprint() && !mc.player.horizontalCollision && !mc.player.isSprinting()) {
            mc.player.setSprinting(true);
        }
    }

    @Override
    protected void onDisable() {
        if (mc.player != null && mc.player.isSprinting()) {
            mc.player.setSprinting(false);
        }
        super.onDisable();
    }
}