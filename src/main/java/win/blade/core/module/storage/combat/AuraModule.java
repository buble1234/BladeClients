package win.blade.core.module.storage.combat;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Vec3d;
import win.blade.common.gui.impl.gui.setting.implement.*;
import win.blade.common.gui.impl.gui.setting.implement.GroupSetting;
import win.blade.common.ui.NotificationType;
import win.blade.common.utils.aim.manager.AimManager;
import win.blade.common.utils.aim.manager.TargetTask;
import win.blade.common.utils.aim.base.AimCalculator;
import win.blade.common.utils.aim.core.AimSettings;
import win.blade.common.utils.aim.core.ViewDirection;
import win.blade.common.utils.aim.mode.AdaptiveSmooth;
import win.blade.common.utils.aim.point.PointCalculator;
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
import win.blade.core.neuro.NeuralController;
import win.blade.core.neuro.model.ModelType;
import win.blade.core.neuro.model.ModelData;
import win.blade.core.Manager;
import java.util.Random;

/**
 * Автор: NoCap
 * Дата создания: 28.06.2025
 */
@ModuleInfo(name = "Aura", category = Category.COMBAT, desc = "Автоматически находит и атакует цели.")
public class AuraModule extends Module {

    private final SelectSetting aimModeSetting = new SelectSetting("Режим", "Когда активировать прицеливание.")
            .value("Постоянный", "Во время удара", "Нейро");

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

    private final SelectSetting neuralModel = new SelectSetting("Нейро модель", "Выбор модели для нейронного режима.")
            .value("BLD1", "BLD2")
            .visible(() -> aimModeSetting.isSelected("Нейро"));

    private final BooleanSetting neuralRecord = new BooleanSetting("Запись", "Записывать данные для обучения нейросети.")
            .setValue(false)
            .visible(() -> aimModeSetting.isSelected("Нейро"));

    private final ValueSetting recordDuration = new ValueSetting("Длительность записи", "Продолжительность записи в минутах.")
            .setValue(5f).range(1f, 30f)
            .visible(() -> aimModeSetting.isSelected("Нейро") && neuralRecord.getValue());

    private final SelectSetting pvpMode = new SelectSetting("Режим PvP", "Адаптация под боевую систему 1.8 или 1.9+.")
            .value("1.9", "1.8");

    private final ValueSetting cps = new ValueSetting("Скорость атаки", "Количество ударов в секунду (CPS) для PvP 1.8.")
            .setValue(12f).range(8f, 16f)
            .visible(() -> pvpMode.isSelected("1.8"));

    private final SelectSetting criticalMode = new SelectSetting("Критические удары", "Управляет нанесением критических ударов.")
            .value("Всегда", "Умные", "Нету");

    private final SelectSetting pointMode = new SelectSetting("Точка прицеливания", "Точка на теле цели для прицеливания.")
            .value("Умные", "Центр", "Мульти")
            .visible(() -> aimGroup.getValue() && !aimModeSetting.isSelected("Нейро"));

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

    private Entity currentTarget;
    private float aimTicks;
    private final NeuralController neuralController = new NeuralController();
    private long recordStartTime = 0;
    private long combatStartTime = 0;
    private boolean wasAttacking = false;
    private boolean attackConditionMet = false;
    private int recordFrameCount = 0;
    private final Random random = new Random();

    public AuraModule() {
        addSettings(
                aimGroup, attackRange, aimRange, rotateTick,
                neuralModel, neuralRecord, recordDuration,
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
        combatStartTime = System.currentTimeMillis();
        recordFrameCount = 0;

        if (aimModeSetting.isSelected("Нейро")) {
            ModelType modelType = neuralModel.isSelected("BLD1") ? ModelType.BLD1 : ModelType.BLD2;
            neuralController.switchModel(modelType);
            Manager.notificationManager.add("Aura: Нейро-модель " + modelType.name() + " загружена", NotificationType.INFO, 1500);

            if (neuralRecord.getValue()) {
                recordStartTime = System.currentTimeMillis();
                neuralController.startRecord("aura_session_" + System.currentTimeMillis());
                Manager.notificationManager.add("Aura: Начата запись данных (" + recordDuration.getValue() + " мин)", NotificationType.SUCCESS, 2000);
            } else {
                neuralController.start();
                Manager.notificationManager.add("Aura: Нейро-предсказание активировано", NotificationType.SUCCESS, 1500);
            }
        }

        super.onEnable();
    }

    @Override
    protected void onDisable() {
        clearTarget();

        if (aimModeSetting.isSelected("Нейро")) {
            if (neuralController.isRecording()) {
                neuralController.stopRecord();
                Manager.notificationManager.add("Aura: Запись остановлена и модель обновлена", NotificationType.INFO, 2000);
            }
            if (neuralController.isActive()) {
                neuralController.stop();
                Manager.notificationManager.add("Aura: Нейро-предсказание деактивировано", NotificationType.INFO, 1500);
            }
        }

        super.onDisable();
    }

    private void clearTarget() {
        currentTarget = null;
        aimTicks = 0;
        AimManager.INSTANCE.disableWithSmooth(0.9f);
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
            AimManager.INSTANCE.disableWithSmooth(5);
            return;
        }

        boolean hitThroughBlocks = getBooleanSetting(behaviorOptions, "Бить сквозь блоки").getValue();
        attackConditionMet = false;
        if (hitThroughBlocks) {
            attackConditionMet = MathUtility.canRaytraceToTarget(currentTarget, attackRange.getValue(), true);
        } else {
            if (mc.crosshairTarget instanceof EntityHitResult result && result.getEntity() == currentTarget) {
                attackConditionMet = true;
            }
        }

        if (aimModeSetting.isSelected("Нейро") && neuralRecord.getValue()) {
            long recordTime = (System.currentTimeMillis() - recordStartTime) / 1000 / 60;
            if (recordTime >= recordDuration.getValue() || recordFrameCount >= 5000) {
                neuralController.stopRecord();
                neuralController.start();
                neuralRecord.setValue(false);
                Manager.notificationManager.add("Aura: Запись завершена (лимит/время), предсказание активно", NotificationType.SUCCESS, 2000);
            }
        }

        handleAimLogic();

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
            AimManager.INSTANCE.disableWithSmooth(0.9f);
        } else {
            currentTarget = null;
        }
    }

    private void handleAimLogic() {
        if (!aimGroup.getValue()) {
            AimManager.INSTANCE.disableWithSmooth(0.9f);
            return;
        }

        if (aimModeSetting.isSelected("Нейро")) {
            handleNeuralAim();
        } else if (aimModeSetting.isSelected("Во время удара")) {
            if (aimTicks > 0) {
                aimTicks--;
                aimAtTarget();
            } else {
                AimManager.INSTANCE.disableWithSmooth(0.9f);
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

    private void handleNeuralAim() {
        if (currentTarget == null) return;

        float currentYaw = mc.player.getYaw();
        float currentPitch = mc.player.getPitch();
        float distance = (float) mc.player.distanceTo(currentTarget);
        boolean targetMoving = currentTarget.getVelocity().lengthSquared() > 0.01;
        float playerHealth = mc.player.getHealth();
        long combatTime = System.currentTimeMillis() - combatStartTime;

        boolean isAttackingIntent = attackConditionMet;
        wasAttacking = isAttackingIntent;

        PointMode selectedPointMode;
        switch (pointMode.getSelected()) {
            case "Умные" -> selectedPointMode = PointMode.SMART;
            case "Мульти" -> selectedPointMode = PointMode.MULTI;
            default -> selectedPointMode = PointMode.CENTER;
        }

        ViewDirection baseDirection = AimCalculator.calculateToEntity(currentTarget, selectedPointMode);
        float baseYaw = baseDirection.yaw();
        float basePitch = baseDirection.pitch();

        float desiredYawDelta = MathUtility.normalizeAngle(baseYaw - currentYaw);
        float desiredPitchDelta = MathUtility.clamp(basePitch - currentPitch, -90f, 90f);
        float desiredSpeed = 0.5f;
        if (targetMoving) {
            double targetSpeed = currentTarget.getVelocity().length();
            double leadTime = distance / (0.3 + targetSpeed * 0.5);
            Vec3d velocity = currentTarget.getVelocity();
            Vec3d currentTargetPoint = PointCalculator.getPoint(currentTarget, selectedPointMode);
            Vec3d predictedTargetPoint = currentTargetPoint.add(velocity.multiply(leadTime));
            ViewDirection leadDirection = AimCalculator.calculateToPosition(mc.player.getCameraPosVec(1.0f), predictedTargetPoint);
            desiredYawDelta = MathUtility.normalizeAngle(leadDirection.yaw() - currentYaw);
            desiredPitchDelta = MathUtility.clamp(leadDirection.pitch() - currentPitch, -90f, 90f);
            desiredSpeed = 0.8f;
        }
        desiredSpeed = isAttackingIntent ? 1.0f : desiredSpeed;
        desiredSpeed *= (1.0f - (distance / aimRange.getValue()));

        desiredYawDelta = Math.max(-90f, Math.min(90f, desiredYawDelta));
        desiredPitchDelta = Math.max(-90f, Math.min(90f, desiredPitchDelta));

        if (neuralController.isRecording()) {
            recordFrameCount++;
            neuralController.record(currentYaw, currentPitch, distance, isAttackingIntent, targetMoving, playerHealth, combatTime, desiredYawDelta, desiredPitchDelta, desiredSpeed);
        } else if (neuralController.isActive()) {
            ModelData prediction = neuralController.predict(currentYaw, currentPitch, distance, targetMoving, playerHealth, combatTime, isAttackingIntent);

            float neuralYawAdjust = prediction.getYaw();
            float neuralPitchAdjust = prediction.getPitch();
            float targetYaw = baseYaw + neuralYawAdjust;
            float targetPitch = basePitch + neuralPitchAdjust;

            long currentTimeMillis = System.currentTimeMillis();
            float timeFactor = ((currentTimeMillis - combatStartTime) % 10000L) / 10000f;
            float distFactor = (float) Math.sin(distance * 0.1f) * 2f;
            double gaussianYaw = random.nextGaussian();
            double gaussianPitch = random.nextGaussian();
            float patternYaw = (float) (Math.sin(timeFactor * Math.PI * 2) * 2f + distFactor + gaussianYaw * 0.5f);
            float patternPitch = (float) (Math.cos(timeFactor * Math.PI * 2) * 2f - distFactor + gaussianPitch * 0.5f);

            targetYaw += patternYaw;
            targetPitch = MathUtility.clamp(targetPitch + patternPitch, -90f, 90f);

            Vec3d predictedPos = currentTarget.getPos();
            if (targetMoving) {
                double targetSpeed = currentTarget.getVelocity().length();
                double leadTime = distance / (0.3 + targetSpeed * 0.5);
                Vec3d velocity = currentTarget.getVelocity();
                predictedPos = predictedPos.add(velocity.multiply(leadTime));
            }

            float neuralSpeed = prediction.getSpeed();
            float speedNoise = (random.nextFloat() - 0.5f) * 0.1f;
            neuralSpeed = Math.max(0f, Math.min(1f, neuralSpeed + speedNoise));
            int smoothTime = (int) (1000 + 4000 * (1 - neuralSpeed));

            float pitchDeviation = Math.abs(currentPitch - basePitch);
            if (distance < 2.5f && pitchDeviation > 10f) {
                smoothTime = (int) Math.max(500, smoothTime * 0.5f);
                float correctionFactor = 1.0f - (pitchDeviation / 90f);
                targetPitch = basePitch + (targetPitch - basePitch) * correctionFactor;
                targetPitch = MathUtility.clamp(targetPitch, -90f, 90f);
            }

            boolean enableViewSync = getBooleanSetting(behaviorOptions, "Синхронизировать взгляд").getValue();

            boolean enableMovementCorrection = false;
            boolean enableSilent = false;

            if (moveCorrectionGroup.getValue()) {
                enableMovementCorrection = true;

                enableSilent= moveCorrectionMode.isSelected("Слабая");

            }

            ViewDirection targetDirection = new ViewDirection(targetYaw, targetPitch);
            AimSettings aimSettings = new AimSettings(
                    new AdaptiveSmooth(smoothTime),
                    enableViewSync,
                    enableMovementCorrection,
                    enableSilent
            );

            TargetTask smoothTask = aimSettings.buildTask(targetDirection, predictedPos, currentTarget);
            AimManager.INSTANCE.execute(smoothTask);
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
                new AdaptiveSmooth(4),
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
        wasAttacking = true;
        AttackManager.attack(livingTarget, settings);
        wasAttacking = false;
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


//    public static void update(){

//    }
}