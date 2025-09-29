package win.blade.core.module.storage.move;

import net.minecraft.entity.effect.StatusEffects;
import win.blade.common.gui.impl.gui.setting.implement.BooleanSetting;
import win.blade.common.utils.player.SprintUtility;
import win.blade.core.event.controllers.EventHandler;
import win.blade.core.event.impl.minecraft.UpdateEvents;
import win.blade.core.event.impl.player.KeepSprintEvent;
import win.blade.core.event.impl.player.PlayerActionEvents;
import win.blade.core.event.impl.render.WorldLoadEvent;
import win.blade.core.module.api.Category;
import win.blade.core.module.api.Module;
import win.blade.core.module.api.ModuleInfo;

@ModuleInfo(
        name = "AutoSprint",
        category = Category.MOVE,
        desc = "Автоматически включает бег при движении."
)
public class AutoSprintModule extends Module {
    public int tickStop = 0;
    private final BooleanSetting keepSprintSetting = new BooleanSetting("Keep Sprint", "Preserve sprint knock-back momentum").setValue(true);
    public final BooleanSetting ignoreHungerSetting = new BooleanSetting("Ignore Hunger", "Sprint even with low saturation").setValue(true);

    public AutoSprintModule() {
        addSettings(keepSprintSetting,ignoreHungerSetting);
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent e) {
        tickStop = 3;
    }

    @EventHandler
    public void onTick(UpdateEvents.Update event) {
        if (mc.player == null || mc.world == null) {
            return;
        }
        boolean collidedHoriz = mc.player.horizontalCollision && !mc.player.collidedSoftly;
        boolean sneaking = mc.player.isSneaking() && !mc.player.isSwimming();
        if (tickStop > 0 || sneaking) {
            mc.player.setSprinting(false);
        } else if (canStartSprinting() && !collidedHoriz && !mc.options.sprintKey.isPressed()) {
            mc.player.setSprinting(true);
        }
        if (tickStop > 0) tickStop--;
    }

    @EventHandler
    public void onKeepSprint(KeepSprintEvent e) {
        if (keepSprintSetting.getValue()) {
            mc.player.setVelocity(mc.player.getVelocity().x / 0.6F, mc.player.getVelocity().y, mc.player.getVelocity().z / 0.6F);
            mc.player.setSprinting(true);
        }
    }
    private boolean canStartSprinting() {
        boolean blinded = mc.player.hasStatusEffect(StatusEffects.BLINDNESS);
        return !mc.player.isSprinting() && mc.player.input.hasForwardMovement() && !blinded && !mc.player.isGliding();
    }

}