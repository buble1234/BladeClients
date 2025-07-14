package win.blade.core.module.storage.combat;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.hit.EntityHitResult;
import win.blade.common.gui.impl.menu.settings.impl.*;
import win.blade.common.utils.aim.manager.AimManager;
import win.blade.common.utils.aim.manager.TargetTask;
import win.blade.common.utils.aim.base.AimCalculator;
import win.blade.common.utils.aim.core.AimSettings;
import win.blade.common.utils.aim.core.ViewDirection;
import win.blade.common.utils.aim.mode.AdaptiveSmooth;
import win.blade.common.utils.aim.point.PointMode;
import win.blade.common.utils.attack.AttackSettings;
import win.blade.common.utils.attack.AttackManager;
import win.blade.common.utils.attack.AttackMode;
import win.blade.common.utils.attack.CriticalMode;
import win.blade.common.utils.player.TargetUtility;
import win.blade.core.event.controllers.EventHandler;
import win.blade.core.event.impl.minecraft.UpdateEvents;
import win.blade.core.module.api.Category;
import win.blade.core.module.api.Module;
import win.blade.core.module.api.ModuleInfo;

/**
 * Автор: NoCap
 * Дата создания: 28.06.2025
 */
@ModuleInfo(name = "Aura", category = Category.COMBAT)
public class AuraModule extends Module {

    public final ModeSetting aimMode = new ModeSetting(this, "Режим прицеливания", "Постоянный", "Во время удара", "Нету");
    private final SliderSetting attackRange = new SliderSetting(this, "Дистанция атаки", 3.0f, 1.0f, 6.0f, 0.1f);
    private final SliderSetting aimRange = new SliderSetting(this, "Дистанция прицеливания", 4.5f, 2.0f, 8.0f, 0.1f).setVisible(() -> !aimMode.is("Нету"));
    private final SliderSetting rotateTick = new SliderSetting(this, "Тики поворота", 5, 1, 10, 1.0f).setVisible(() -> aimMode.is("Во время удара"));
    private final ModeSetting pvpMode = new ModeSetting(this, "Режим PvP", "1.9", "1.8");
    private final SliderSetting cps = new SliderSetting(this, "Скорость атаки", 12, 8, 16, 0.5f).setVisible(() -> pvpMode.is("1.8"));
    private final ModeSetting criticalMode = new ModeSetting(this, "Критические удары", "Всегда", "Умные", "Нету");
    private final ModeSetting pointMode = new ModeSetting(this, "Точка прицеливания", "Умные", "Центр", "Мульти").setVisible(() -> !aimMode.is("Нету"));

    private final MultiBooleanSetting targetTypes = new MultiBooleanSetting(this, "Типы целей",
            BooleanSetting.of("Игроки без брони", true).onAction(this::updateTargetTypes),
            BooleanSetting.of("Игроки с бронёй", true).onAction(this::updateTargetTypes),
            BooleanSetting.of("Невидимые игроки", false).onAction(this::updateTargetTypes),
            BooleanSetting.of("Тиммейты", false).onAction(this::updateTargetTypes),
            BooleanSetting.of("Мобы", true).onAction(this::updateTargetTypes),
            BooleanSetting.of("Животные", false).onAction(this::updateTargetTypes),
            BooleanSetting.of("Жители", false).onAction(this::updateTargetTypes)
    );

    private final MultiBooleanSetting behaviorOptions = new MultiBooleanSetting(this, "Опции",
            BooleanSetting.of("Корректировать движения", true).setVisible(() -> !aimMode.is("Нету")),
            BooleanSetting.of("Сбрасывать спринт", true),
            BooleanSetting.of("Отжимать щит", true),
            BooleanSetting.of("Проверять еду", true),
            BooleanSetting.of("Синхронизировать взгляд", true).setVisible(() -> !aimMode.is("Нету")),
            BooleanSetting.of("Фокусировать одну цель", false)
    );

    private final ModeSetting moveMode = new ModeSetting(this, "Режим коррекции движений", "Слабая", "Сильная", "Нету").setVisible(() -> !aimMode.is("Нету"));

    private final ModeSetting sortMode = new ModeSetting(this, "Сортировка целей", "Дистанция", "Здоровье", "Броня", "Поле зрения", "Общая");

    private Entity currentTarget;
    private float aimTicks;

    @Override
    public void onEnable() {
        currentTarget = null;
        aimTicks = 0;
        super.onEnable();
    }

    @Override
    protected void onDisable() {
        clearTarget();
        super.onDisable();
    }

    private void clearTarget() {
        currentTarget = null;
        aimTicks = 0;
        AimManager.INSTANCE.disable();
    }

    private void updateTargetTypes() {
        TargetUtility.updateTargetTypes(targetTypes);
        if (currentTarget != null && !TargetUtility.isValidTarget(currentTarget)) {
            clearTarget();
        }
    }

    @EventHandler
    public void onUpdate(UpdateEvents.PlayerUpdate event) {
        if (mc.player == null || mc.world == null || !mc.player.isAlive() || mc.currentScreen != null) {
            clearTarget();
            return;
        }

        updateTargetTypes();
        updateCurrentTarget();

        if (currentTarget == null) {
            AimManager.INSTANCE.disable();
            return;
        }

        handleAimLogic();

        if (mc.crosshairTarget instanceof EntityHitResult result && result.getEntity() == currentTarget) {
            if (currentTarget != null) {
                performAttack();
            }
        }
    }

    private void updateCurrentTarget() {
        boolean focusTarget = behaviorOptions.get("Фокусировать одну цель").getValue();
        float totalRange = aimRange.getValue() + attackRange.getValue();

        if (focusTarget && currentTarget != null && currentTarget.isAlive() && mc.player.distanceTo(currentTarget) <= totalRange && TargetUtility.isValidTarget(currentTarget)) {
            return;
        }

        LivingEntity potentialTarget = TargetUtility.findBestTarget(totalRange, sortMode.getValue());
        if (potentialTarget != null && TargetUtility.isValidTarget(potentialTarget)) {
            currentTarget = potentialTarget;
        } else {
            currentTarget = null;
        }
    }

    private void handleAimLogic() {
        if (aimMode.is("Нету")) {
            AimManager.INSTANCE.disable();
            return;
        }

        if (aimMode.is("Во время удара")) {
            if (aimTicks > 0) {
                aimTicks--;
                aimAtTarget();
            } else {
                AimManager.INSTANCE.disable();
            }

            if (currentTarget instanceof LivingEntity livingTarget) {
                AttackSettings settings = buildAttackSettings();
                if (AttackManager.canAttack(livingTarget, settings)) {
                    aimTicks = rotateTick.getValue();
                }
            }
        } else {
            aimAtTarget();
        }
    }

    private void aimAtTarget() {
        PointMode selectedPointMode;
        switch (pointMode.getValue()) {
            case "Умные":
                selectedPointMode = PointMode.SMART;
                break;
            case "Мульти":
                selectedPointMode = PointMode.MULTI;
                break;
            default:
                selectedPointMode = PointMode.CENTER;
        }

        ViewDirection targetDirection = AimCalculator.calculateToEntity(currentTarget, selectedPointMode);

        boolean enableViewSync = behaviorOptions.get("Синхронизировать взгляд").getValue();
        boolean enableMovementCorrection = false;
        boolean enableSilent = false;

        if (moveMode.is("Слабая")) {
            enableMovementCorrection = true;
            enableSilent = true;
        } else if (moveMode.is("Сильная")) {
            enableMovementCorrection = true;
            enableSilent = false;
        } else if (moveMode.is("Нету")) {
            enableMovementCorrection = false;
            enableSilent = false;
        }

        AimSettings aimSettings = new AimSettings(
                new AdaptiveSmooth(12f),
                enableViewSync,
                enableMovementCorrection,
                enableSilent
        );

        TargetTask smoothTask = aimSettings.buildTask(targetDirection, currentTarget.getPos(), currentTarget);
        AimManager.INSTANCE.execute(smoothTask);
    }

    private void performAttack() {
        if (!(currentTarget instanceof LivingEntity livingTarget)) {
            return;
        }

        AttackSettings settings = buildAttackSettings();
        AttackManager.attack(livingTarget, settings);
    }

    private boolean shouldCrit() {
        return !mc.player.isOnGround() && !mc.player.isInLava() && !mc.player.isSubmergedInWater()
                && !mc.player.hasStatusEffect(StatusEffects.BLINDNESS) && mc.player.fallDistance > 0;
    }

    private AttackSettings buildAttackSettings() {
        AttackMode attackMode = pvpMode.is("1.8") ? AttackMode.LEGACY : AttackMode.MODERN;

        CriticalMode critical = switch (criticalMode.getValue()) {
            case "Всегда" -> CriticalMode.ALWAYS;
            case "Умные" -> CriticalMode.ADAPTIVE;
            default -> CriticalMode.NONE;
        };

        return new AttackSettings(
                attackMode,
                critical,
                cps.getValue(),
                behaviorOptions.get("Отжимать щит").getValue(),
                behaviorOptions.get("Проверять еду").getValue(),
                attackRange.getValue(),
                behaviorOptions.get("Сбрасывать спринт").getValue()
        );
    }

    public Entity getCurrentTarget() {
        return currentTarget;
    }

    public double getDistanceToTarget() {
        return currentTarget != null ? mc.player.distanceTo(currentTarget) : -1;
    }
}