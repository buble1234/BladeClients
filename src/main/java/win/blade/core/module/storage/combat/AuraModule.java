package win.blade.core.module.storage.combat;

import net.minecraft.entity.Entity;
import win.blade.common.gui.impl.menu.settings.impl.BooleanSetting;
import win.blade.common.gui.impl.menu.settings.impl.MultiBooleanSetting;
import win.blade.common.gui.impl.menu.settings.impl.SliderSetting;
import win.blade.common.utils.aim.manager.AimManager;
import win.blade.common.utils.aim.manager.TargetTask;
import win.blade.common.utils.aim.base.AimCalculator;
import win.blade.common.utils.aim.core.AimSettings;
import win.blade.common.utils.aim.core.ViewDirection;
import win.blade.common.utils.aim.mode.AdaptiveSmooth;
import win.blade.common.utils.player.TargetUtility;
import win.blade.core.event.controllers.EventHandler;
import win.blade.core.event.impl.minecraft.UpdateEvents;
import win.blade.core.module.api.Category;
import win.blade.core.module.api.Module;
import win.blade.core.module.api.ModuleInfo;

/**
 * Автор: NoCap
 * Дата создания: 18.06.2025
 */
@ModuleInfo(name = "Aura", category = Category.COMBAT)
public class AuraModule extends Module {


    private final SliderSetting aimRange = new SliderSetting(this, "Дистанция поворота", 4, 1, 8, 0.1f);

    private final MultiBooleanSetting targetType = new MultiBooleanSetting(this, "Типы целей",
            BooleanSetting.of("Игроки без брони", true).onAction(this::updateTargetTypes),
            BooleanSetting.of("Игроки с бронёй", true).onAction(this::updateTargetTypes),
            BooleanSetting.of("Невидимые игроки", false).onAction(this::updateTargetTypes),
            BooleanSetting.of("Тиммейты", false).onAction(this::updateTargetTypes),
            BooleanSetting.of("Мобы", true).onAction(this::updateTargetTypes),
            BooleanSetting.of("Животные", false).onAction(this::updateTargetTypes),
            BooleanSetting.of("Жители", false).onAction(this::updateTargetTypes)
    );

    private final BooleanSetting correctionMove = new BooleanSetting(this, "Корректировать движения", true);
    private final BooleanSetting viewSync = new BooleanSetting(this, "Синхронизировать взгляд", true);

    private Entity currentTarget;

    @Override
    public void onEnable() {
        resetState();
        super.onEnable();
    }

    @Override
    protected void onDisable() {
        cleanup();
        super.onDisable();
    }

    private void resetState() {
        currentTarget = null;
        AimManager.INSTANCE.disable();
    }

    private void cleanup() {
        AimManager.INSTANCE.disable();
        currentTarget = null;
    }

    private void updateTargetTypes() {
        TargetUtility.updateTargetTypes(targetType);
        if (currentTarget != null && !TargetUtility.isValidTarget(currentTarget)) {
            AimManager.INSTANCE.disable();
            currentTarget = null;
        }
    }

    @EventHandler
    public void onUpdate(UpdateEvents.Update event) {
        if (mc.player == null || mc.world == null) return;

        updateTargetTypes();

        Entity target = TargetUtility.findBestTarget(aimRange.getValue());

        if (target != null) {
            handleTargetFound(target);
        } else {
            handleNoTarget();
        }
    }

    private void handleTargetFound(Entity target) {
        currentTarget = target;
        double distanceToTarget = mc.player.distanceTo(target);

        if (distanceToTarget <= aimRange.getValue()) {
            aim(target);
        }
    }

    private void handleNoTarget() {
        if (currentTarget != null) {
            AimManager.INSTANCE.disable();
            currentTarget = null;
        }
    }

    private void aim(Entity target) {
        ViewDirection targetDirection = AimCalculator.calculateToEntity(target);
        AimSettings smoothSettings = new AimSettings(
                new AdaptiveSmooth(12f),
                viewSync.getValue(),
                correctionMove.getValue() || viewSync.getValue(),
                false
        );
        TargetTask smoothTask = smoothSettings.buildTask(targetDirection, target.getPos(), target);
        AimManager.INSTANCE.execute(smoothTask);
    }

    public Entity getCurrentTarget() {
        return currentTarget;
    }

    public double getDistanceToTarget() {
        return currentTarget != null ? mc.player.distanceTo(currentTarget) : -1;
    }
}