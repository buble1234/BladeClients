package win.blade.core.module.storage.combat;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import win.blade.common.gui.impl.menu.settings.impl.BooleanSetting;
import win.blade.common.gui.impl.menu.settings.impl.MultiBooleanSetting;
import win.blade.common.gui.impl.menu.settings.impl.SliderSetting;
import win.blade.common.gui.impl.menu.settings.impl.ModeSetting;
import win.blade.common.utils.aim.manager.AimManager;
import win.blade.common.utils.aim.manager.TargetTask;
import win.blade.common.utils.aim.base.AimCalculator;
import win.blade.common.utils.aim.core.AimSettings;
import win.blade.common.utils.aim.core.ViewDirection;
import win.blade.common.utils.aim.mode.AdaptiveSmooth;
import win.blade.common.utils.attack.AttackSettings;
import win.blade.common.utils.attack.AttackManager;
import win.blade.common.utils.player.TargetUtility;
import win.blade.core.event.controllers.EventHandler;
import win.blade.core.event.impl.minecraft.UpdateEvents;
import win.blade.core.module.api.Category;
import win.blade.core.module.api.Module;
import win.blade.core.module.api.ModuleInfo;

/**
 * Автор: NoCap
 * Дата создания: 28.06.2025
 * Обновлено: 28.06.2025 (фикс автопрыжка при autoJump = false)
 */
@ModuleInfo(name = "Aura", category = Category.COMBAT)
public class AuraModule extends Module {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuraModule.class);

    private final SliderSetting aimRange = new SliderSetting(this, "Дистанция поворота", 4.5f, 2, 8, 0.1f);
    private final MultiBooleanSetting targetType = new MultiBooleanSetting(this, "Типы целей",
            BooleanSetting.of("Игроки без брони", true).onAction(this::updateTargetTypes),
            BooleanSetting.of("Игроки с бронёй", true).onAction(this::updateTargetTypes),
            BooleanSetting.of("Невидимые игроки", false).onAction(this::updateTargetTypes),
            BooleanSetting.of("Тиммейты", false).onAction(this::updateTargetTypes),
            BooleanSetting.of("Мобы", true).onAction(this::updateTargetTypes),
            BooleanSetting.of("Животные", false).onAction(this::updateTargetTypes),
            BooleanSetting.of("Жители", false).onAction(this::updateTargetTypes)
    );

    private final ModeSetting pvpMode = new ModeSetting(this, "Режим PvP", "1.9", "1.8", "1.9");
    private final SliderSetting cps = new SliderSetting(this, "Скорость атаки", 12, 8, 16, 0.5f).setVisible(() -> pvpMode.is("1.8"));
    private final ModeSetting criticalMode = new ModeSetting(this, "Криты", "None", "None", "Jump", "Adaptive");

    private final MultiBooleanSetting auraOptions = new MultiBooleanSetting(this, "Опции",
            BooleanSetting.of("Корректировать движения", true),
            BooleanSetting.of("Сбрасывать спринт", true),
            BooleanSetting.of("Отжимать щит", true),
            BooleanSetting.of("Проверять еду", true),
            BooleanSetting.of("Авто прыжок", true),
            BooleanSetting.of("Синхронизировать взгляд", true)
    );

    private Entity currentTarget;
    private long lastJumpTime;

    @Override
    public void onEnable() {
        currentTarget = null;
        lastJumpTime = 0;
        LOGGER.info("AuraModule enabled");
        super.onEnable();
    }

    @Override
    protected void onDisable() {
        clearTarget();
        LOGGER.info("AuraModule disabled");
        super.onDisable();
    }

    private void clearTarget() {
        currentTarget = null;
        AimManager.INSTANCE.disable();
    }

    private void updateTargetTypes() {
        TargetUtility.updateTargetTypes(targetType);
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

        if (currentTarget != null) {
            aimAtTarget();
            performAttack();
        } else {
            AimManager.INSTANCE.disable();
        }
    }

    private void updateCurrentTarget() {
        Entity potentialTarget = TargetUtility.findBestTarget(aimRange.getValue());
        if (potentialTarget instanceof LivingEntity && mc.player.distanceTo(potentialTarget) <= aimRange.getValue() && TargetUtility.isValidTarget(potentialTarget)) {
            currentTarget = potentialTarget;
        } else {
            currentTarget = null;
        }
    }

    private void aimAtTarget() {
        ViewDirection targetDirection = AimCalculator.calculateToEntity(currentTarget);
        AimSettings smoothSettings = new AimSettings(
                new AdaptiveSmooth(12f),
                auraOptions.get("Синхронизировать взгляд").getValue(),
                auraOptions.get("Корректировать движения").getValue() || auraOptions.get("Синхронизировать взгляд").getValue(),
                false
        );
        TargetTask smoothTask = smoothSettings.buildTask(targetDirection, currentTarget.getPos(), currentTarget);
        AimManager.INSTANCE.execute(smoothTask);
    }

    private void performAttack() {
        if (!(currentTarget instanceof LivingEntity livingTarget)) {
            return;
        }

        AttackManager.AttackMode attackMode = pvpMode.is("1.8") ? AttackManager.AttackMode.LEGACY : AttackManager.AttackMode.MODERN;

        AttackManager.CriticalMode mode = switch (criticalMode.getValue()) {
            case "Jump" -> AttackManager.CriticalMode.JUMP;
            case "Adaptive" -> AttackManager.CriticalMode.ADAPTIVE;
            default -> AttackManager.CriticalMode.NONE;
        };

        AttackSettings settings = new AttackSettings(
                attackMode,
                mode,
                cps.getValue(),
                auraOptions.get("Отжимать щит").getValue(),
                auraOptions.get("Проверять еду").getValue(),
                aimRange.getValue(),
                auraOptions.get("Авто прыжок").getValue(),
                auraOptions.get("Сбрасывать спринт").getValue()
        );

        if (AttackManager.canAttack(livingTarget, settings)) {
            AttackManager.performAttack(livingTarget, settings);
        }
    }

    public Entity getCurrentTarget() {
        return currentTarget;
    }

    public double getDistanceToTarget() {
        return currentTarget != null ? mc.player.distanceTo(currentTarget) : -1;
    }
}