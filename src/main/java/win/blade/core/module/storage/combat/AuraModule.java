package win.blade.core.module.storage.combat;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.hit.EntityHitResult;
import win.blade.common.gui.impl.gui.setting.implement.*;
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
import win.blade.common.utils.math.MathUtility;
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

    private final SelectSetting aimModeSetting = new SelectSetting("Режим", "")
            .value("Постоянный", "Во время удара");

    public final GroupSetting aimGroup = new GroupSetting("Прицеливание", "")
            .settings(aimModeSetting);

    private final ValueSetting attackRange = new ValueSetting("Дистанция атаки", "")
            .setValue(3.0f).range(1.0f, 6.0f);

    private final ValueSetting aimRange = new ValueSetting("Дистанция прицеливания", "")
            .setValue(4.5f).range(2.0f, 8.0f)
            .visible(() -> aimGroup.getValue());

    private final ValueSetting rotateTick = new ValueSetting("Тики поворота", "")
            .setValue(5f).range(1f, 10f)
            .visible(() -> aimGroup.getValue() && aimModeSetting.isSelected("Во время удара"));

    private final SelectSetting pvpMode = new SelectSetting("Режим PvP", "")
            .value("1.9", "1.8");

    private final ValueSetting cps = new ValueSetting("Скорость атаки", "")
            .setValue(12f).range(8f, 16f)
            .visible(() -> pvpMode.isSelected("1.8"));

    private final SelectSetting criticalMode = new SelectSetting("Критические удары", "")
            .value("Всегда", "Умные", "Нету");

    private final SelectSetting pointMode = new SelectSetting("Точка прицеливания", "")
            .value("Умные", "Центр", "Мульти")
            .visible(() -> aimGroup.getValue());

    private final GroupSetting targetTypes = new GroupSetting("Типы целей", "").setToggleable().settings(
            new BooleanSetting("Игроки без брони", "").setValue(true),
            new BooleanSetting("Игроки с бронёй", "").setValue(true),
            new BooleanSetting("Невидимые игроки", "").setValue(false),
            new BooleanSetting("Тиммейты", "").setValue(false),
            new BooleanSetting("Мобы", "").setValue(true),
            new BooleanSetting("Животные", "").setValue(false),
            new BooleanSetting("Жители", "").setValue(false)
    );

    private final GroupSetting behaviorOptions = new GroupSetting("Опции", "").setToggleable().settings(
            new BooleanSetting("Сбрасывать спринт", "").setValue(true),
            new BooleanSetting("Бить сквозь блоки", "").setValue(false),
            new BooleanSetting("Отжимать щит", "").setValue(true),
            new BooleanSetting("Ломать щит", "").setValue(true),
            new BooleanSetting("Проверять еду", "").setValue(true),
            new BooleanSetting("Синхронизировать взгляд", "").setValue(false).visible(() -> aimGroup.getValue()),
            new BooleanSetting("Фокусировать одну цель", "").setValue(false)
    );

    private final SelectSetting moveCorrectionMode = new SelectSetting("Режим", "")
            .value("Слабая", "Сильная");

    private final GroupSetting moveCorrectionGroup = new GroupSetting("Коррекция движений", "")
            .visible(() -> aimGroup.getValue())
            .settings(moveCorrectionMode);


    private final SelectSetting sortMode = new SelectSetting("Сортировка целей", "")
            .value("Дистанция", "Здоровье", "Броня", "Поле зрения", "Общая");

    private Entity currentTarget;
    private float aimTicks;

    public AuraModule() {
        addSettings(
                aimGroup, attackRange, aimRange, rotateTick,
                pointMode, moveCorrectionGroup,
                pvpMode, cps, criticalMode,
                targetTypes, behaviorOptions, sortMode
        );
    }

    private BooleanSetting getBooleanSetting(GroupSetting group, String name) {
        return (BooleanSetting) group.getSubSetting(name);
    }

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
        AimManager.INSTANCE.disableWithSmooth();
    }

    private void updateTargetTypes() {
        TargetUtility.updateTargetTypes(targetTypes);
        if (currentTarget != null && !TargetUtility.isValidTarget(currentTarget)) {
            clearTarget();
        }
    }

    @EventHandler
    public void onUpdate(UpdateEvents.PlayerUpdate event) {
        if (mc.player == null || mc.world == null || !mc.player.isAlive()) {
            clearTarget();
            return;
        }

        updateTargetTypes();
        updateCurrentTarget();

        if (currentTarget == null) {
            AimManager.INSTANCE.disableWithSmooth();
            return;
        }

        handleAimLogic();

        boolean hitThroughBlocks = getBooleanSetting(behaviorOptions, "Бить сквозь блоки").getValue();
        boolean attackConditionMet = false;

        if (hitThroughBlocks) {
            attackConditionMet = MathUtility.canRaytraceToTarget(currentTarget, attackRange.getValue(), true);
        } else {
            if (mc.crosshairTarget instanceof EntityHitResult result && result.getEntity() == currentTarget) {
                attackConditionMet = true;
            }
        }

        if (attackConditionMet) {
            performAttack();
        }
    }

    private void updateCurrentTarget() {
        boolean focusTarget = getBooleanSetting(behaviorOptions, "Фокусировать одну цель").getValue();
        float totalRange = aimRange.getValue() + attackRange.getValue();

        if (focusTarget && currentTarget != null && currentTarget.isAlive() && mc.player.distanceTo(currentTarget) <= totalRange && TargetUtility.isValidTarget(currentTarget)) {
            return;
        }

        LivingEntity potentialTarget = TargetUtility.findBestTarget(totalRange, sortMode.getSelected());
        if (potentialTarget != null && TargetUtility.isValidTarget(potentialTarget)) {
            currentTarget = potentialTarget;
        } else {
            currentTarget = null;
        }
    }

    private void handleAimLogic() {
        if (!aimGroup.getValue()) {
            AimManager.INSTANCE.disableWithSmooth();
            return;
        }

        if (aimModeSetting.isSelected("Во время удара")) {
            if (aimTicks > 0) {
                aimTicks--;
                aimAtTarget();
            } else {
                AimManager.INSTANCE.disableWithSmooth();
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
        switch (pointMode.getSelected()) {
            case "Умные" -> selectedPointMode = PointMode.SMART;
            case "Мульти" -> selectedPointMode = PointMode.MULTI;
            default -> selectedPointMode = PointMode.CENTER;
        }

        ViewDirection targetDirection = AimCalculator.calculateToEntity(currentTarget, selectedPointMode);

        boolean enableViewSync = getBooleanSetting(behaviorOptions, "Синхронизировать взгляд").getValue();

        boolean enableMovementCorrection = false;
        boolean enableSilent = false;

        if (moveCorrectionGroup.getValue()) {
            enableMovementCorrection = true;

            if (moveCorrectionMode.isSelected("Слабая")) {
                enableSilent = true;
            } else {
                enableSilent = false;
            }
        }

        AimSettings aimSettings = new AimSettings(
                new AdaptiveSmooth(52),
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

    private AttackSettings buildAttackSettings() {
        AttackMode attackMode = pvpMode.isSelected("1.8") ? AttackMode.LEGACY : AttackMode.MODERN;

        CriticalMode critical = switch (criticalMode.getSelected()) {
            case "Всегда" -> CriticalMode.ALWAYS;
            case "Умные" -> CriticalMode.ADAPTIVE;
            default -> CriticalMode.NONE;
        };

        return new AttackSettings(
                attackMode,
                critical,
                cps.getValue(),
                getBooleanSetting(behaviorOptions, "Отжимать щит").getValue(),
                getBooleanSetting(behaviorOptions, "Проверять еду").getValue(),
                attackRange.getValue(),
                getBooleanSetting(behaviorOptions, "Сбрасывать спринт").getValue(),
                true
        );
    }

    public Entity getCurrentTarget() {
        return currentTarget;
    }

    public double getDistanceToTarget() {
        return currentTarget != null ? mc.player.distanceTo(currentTarget) : -1;
    }
}