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
@ModuleInfo(name = "Aura", category = Category.COMBAT, desc = "Автоматически находит и атакует цели.")
public class AuraModule extends Module {

    private final SelectSetting aimModeSetting = new SelectSetting("Режим", "Когда активировать прицеливание.")
            .value("Постоянный", "Во время удара");

    public final GroupSetting aimGroup = new GroupSetting("Прицеливание", "Настройки наведения на цель.")
            .settings(aimModeSetting);

    private final ValueSetting attackRange = new ValueSetting("Дистанция атаки", "Максимальное расстояние для атаки.")
            .setValue(3.0f).range(1.0f, 6.0f);

    private final ValueSetting aimRange = new ValueSetting("Дистанция прицеливания", "Максимальное расстояние для наведения.")
            .setValue(4.5f).range(2.0f, 8.0f)
            .visible(() -> aimGroup.getValue());

    private final ValueSetting rotateTick = new ValueSetting("Тики поворота", "Как долго (в тиках) наводиться перед ударом.")
            .setValue(5f).range(1f, 10f)
            .visible(() -> aimGroup.getValue() && aimModeSetting.isSelected("Во время удара"));

    private final SelectSetting pvpMode = new SelectSetting("Режим PvP", "Адаптация под боевую систему 1.8 или 1.9+.")
            .value("1.9", "1.8");

    private final ValueSetting cps = new ValueSetting("Скорость атаки", "Количество ударов в секунду (CPS) для PvP 1.8.")
            .setValue(12f).range(8f, 16f)
            .visible(() -> pvpMode.isSelected("1.8"));

    private final SelectSetting criticalMode = new SelectSetting("Критические удары", "Управляет нанесением критических ударов.")
            .value("Всегда", "Умные", "Нету");

    private final SelectSetting pointMode = new SelectSetting("Точка прицеливания", "Точка на теле цели для прицеливания.")
            .value("Умные", "Центр", "Мульти")
            .visible(() -> aimGroup.getValue());

    private final GroupSetting targetTypes = new GroupSetting("Типы целей", "Какие типы существ атаковать.").setToggleable().settings(
            new BooleanSetting("Игроки без брони", "Атаковать игроков без брони.").setValue(true),
            new BooleanSetting("Игроки с бронёй", "Атаковать игроков в броне.").setValue(true),
            new BooleanSetting("Невидимые игроки", "Атаковать невидимых игроков.").setValue(false),
            new BooleanSetting("Тиммейты", "Атаковать союзников.").setValue(false),
            new BooleanSetting("Друзья", "Атаковать друзей.").setValue(false),
            new BooleanSetting("Мобы", "Атаковать враждебных мобов.").setValue(true),
            new BooleanSetting("Животные", "Атаковать мирных животных.").setValue(false),
            new BooleanSetting("Жители", "Атаковать деревенских жителей.").setValue(false)
    );

    private final GroupSetting behaviorOptions = new GroupSetting("Опции", "Дополнительные настройки поведения.").setToggleable().settings(
            new BooleanSetting("Сбрасывать спринт", "Сбрасывать спринт перед атакой.").setValue(true),
            new BooleanSetting("Бить сквозь блоки", "Атаковать цели за блоками.").setValue(false),
            new BooleanSetting("Отжимать щит", "Пытаться обойти защиту щитом.").setValue(true),
            new BooleanSetting("Ломать щит", "Атаковать топором, если у цели поднят щит.").setValue(true),
            new BooleanSetting("Проверять еду", "Не атаковать во время еды или питья зелий.").setValue(true),
            new BooleanSetting("Синхронизировать взгляд", "Поворачивать всего персонажа, а не только камеру.").setValue(false).visible(() -> aimGroup.getValue()),
            new BooleanSetting("Фокусировать одну цель", "Атаковать текущую цель, игнорируя других.").setValue(false)
    );

    private final SelectSetting moveCorrectionMode = new SelectSetting("Режим", "Тип коррекции: 'Сильная' поворачивает персонажа, 'Слабая' — незаметна.")
            .value("Слабая", "Сильная");

    private final GroupSetting moveCorrectionGroup = new GroupSetting("Коррекция движений", "Помогает прицелу следовать за движущейся целью.")
            .visible(() -> aimGroup.getValue())
            .setValue(true)
            .settings(moveCorrectionMode);

    private final SelectSetting sortMode = new SelectSetting("Сортировка целей", "Критерий выбора приоритетной цели.")
            .value("Дистанция", "Здоровье", "Броня", "Поле зрения", "Общая");

    private final ValueSetting smoothReturn = new ValueSetting("Плавное возрашение", "Скорость плавного возращения")
            .setValue(2.5f).range(0.1f, 5f);

    private final GroupSetting smoothReturnGroup = new GroupSetting("Скорость", "Скорость возрашение ротации к прицелу")
            .visible(() -> aimGroup.getValue())
            .setValue(true)
            .settings(smoothReturn);

    private Entity currentTarget;
    private float aimTicks;

    public AuraModule() {
        addSettings(
                aimGroup, attackRange, aimRange, rotateTick,
                pointMode, moveCorrectionGroup, pvpMode,
                cps, criticalMode, targetTypes,
                behaviorOptions, sortMode, smoothReturnGroup
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
        if (smoothReturnGroup.getValue()) {
            AimManager.INSTANCE.disableWithSmooth(smoothReturn.getValue());
        } else {
            AimManager.INSTANCE.disable();
        }
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
            if (smoothReturnGroup.getValue()) {
                AimManager.INSTANCE.disableWithSmooth(smoothReturn.getValue());
            } else {
                AimManager.INSTANCE.disable();
            }
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
            if (smoothReturnGroup.getValue()) {
                AimManager.INSTANCE.disableWithSmooth(smoothReturn.getValue());
            } else {
                AimManager.INSTANCE.disable();
            }
            return;
        }

        if (aimModeSetting.isSelected("Во время удара")) {
            if (aimTicks > 0) {
                aimTicks--;
                aimAtTarget();
            } else {
                if (smoothReturnGroup.getValue()) {
                    AimManager.INSTANCE.disableWithSmooth(smoothReturn.getValue());
                } else {
                    AimManager.INSTANCE.disable();
                }
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

//            if (moveCorrectionMode.isSelected("Слабая")) {
//                enableSilent = true;
//            } else {
//                enableSilent = false;
//            }
            enableSilent = moveCorrectionMode.isSelected("Слабая");
        }

        AimSettings aimSettings = new AimSettings(
                new AdaptiveSmooth(5000),
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