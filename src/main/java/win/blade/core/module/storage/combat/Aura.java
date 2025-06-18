package win.blade.core.module.storage.combat;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import org.lwjgl.glfw.GLFW;
import win.blade.common.utils.rotation.manager.AimManager;
import win.blade.common.utils.rotation.manager.TargetTask;
import win.blade.common.utils.rotation.base.AimCalculator;
import win.blade.common.utils.rotation.core.AimSettings;
import win.blade.common.utils.rotation.core.ViewDirection;
import win.blade.common.utils.rotation.mode.AdaptiveSmooth;
import win.blade.core.event.controllers.EventHandler;
import win.blade.core.event.impl.minecraft.UpdateEvent;
import win.blade.core.module.api.Category;
import win.blade.core.module.api.Module;
import win.blade.core.module.api.ModuleInfo;

import java.util.List;

@ModuleInfo(
        name = "Aura",
        category = Category.COMBAT,
        bind = GLFW.GLFW_KEY_R
)
public class Aura extends Module {

    private static final double ATTACK_RANGE = 4.5;
    private static final double SEARCH_RANGE = 6.0;
    private final MinecraftClient mc = MinecraftClient.getInstance();

    private Entity currentTarget;
    private long lastAttackTime = 0;
    private final long attackDelay = 500;

    private final AimSettings auraSettings = new AimSettings(
            new AdaptiveSmooth(13.5f, 1.5f),
            false,
            false,
            false
    );

    @Override
    public void onEnable() {
        super.onEnable();
        currentTarget = null;
        lastAttackTime = 0;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        // Важно: Отключаем систему наведения
        AimManager.INSTANCE.disable();
        currentTarget = null;
    }

    @EventHandler
    public void onUpdate(UpdateEvent event) {
        if (mc.player == null || mc.world == null) return;

        // Поиск ближайшей цели
        Entity target = findOptimalTarget();

        if (target != null) {
            currentTarget = target;
            performAiming(target);
            attemptAttack(target);
        } else {
            // Если цели нет - отключаем наведение
            if (currentTarget != null) {
                AimManager.INSTANCE.disable();
                currentTarget = null;
            }
        }
    }

    private void performAiming(Entity target) {
        ViewDirection targetDirection = AimCalculator.calculateToEntity(target);
        TargetTask aimTask = auraSettings.buildTask(targetDirection, target.getPos(), target);
        AimManager.INSTANCE.execute(aimTask);
    }

    private void attemptAttack(Entity target) {
        long currentTime = System.currentTimeMillis();

        // Проверяем кулдаун атаки
        if (currentTime - lastAttackTime < attackDelay) return;

        // Проверяем дистанцию для атаки
        if (mc.player.distanceTo(target) > ATTACK_RANGE) return;

        // Проверяем видимость цели
        if (!AimCalculator.hasLineOfSight(target, ATTACK_RANGE)) return;

        // Проверяем точность наведения
        ViewDirection currentAim = AimManager.INSTANCE.getCurrentDirection();
        if (currentAim != null) {
            ViewDirection perfectAim = AimCalculator.calculateToEntity(target);
            double aimError = currentAim.distanceTo(perfectAim);

            // Атакуем только если наведение достаточно точное
            if (aimError < 3.0) {
                performAttack(target);
                lastAttackTime = currentTime;
            }
        }
    }

    private void performAttack(Entity target) {
        if (mc.interactionManager != null) {
            mc.interactionManager.attackEntity(mc.player, target);
            mc.player.swingHand(mc.player.getActiveHand());
        }
    }

    private Entity findOptimalTarget() {
        if (mc.player == null || mc.world == null) return null;

        Box searchArea = mc.player.getBoundingBox().expand(SEARCH_RANGE);
        List<Entity> potentialTargets = mc.world.getOtherEntities(mc.player, searchArea);

        return potentialTargets.stream()
                .filter(this::isValidTarget)
                .filter(this::isInAttackRange)
                .min((e1, e2) -> {
                    // Приоритизируем по расстоянию и здоровью
                    double dist1 = mc.player.distanceTo(e1);
                    double dist2 = mc.player.distanceTo(e2);

                    // Если один из противников игрок - приоритет ему
                    if (e1 instanceof PlayerEntity && !(e2 instanceof PlayerEntity)) return -1;
                    if (e2 instanceof PlayerEntity && !(e1 instanceof PlayerEntity)) return 1;

                    // Иначе ближайший
                    return Double.compare(dist1, dist2);
                })
                .orElse(null);
    }

    private boolean isValidTarget(Entity entity) {
        // Базовые проверки
        if (!(entity instanceof LivingEntity living)) return false;
        if (living.isDead() || !living.isAlive()) return false;
        if (entity == mc.player) return false;

        // Проверка на командных союзников (если есть система команд)
        if (entity instanceof PlayerEntity player) {
            // Добавьте здесь логику проверки союзников если нужно
            // if (isTeammate(player)) return false;
        }

        // Разрешенные цели
        return entity instanceof PlayerEntity || (entity instanceof LivingEntity && !(entity instanceof PlayerEntity));
    }

    private boolean isInAttackRange(Entity entity) {
        return mc.player.distanceTo(entity) <= SEARCH_RANGE;
    }

    // Геттеры для дебага и статистики
    public Entity getCurrentTarget() {
        return currentTarget;
    }

    public boolean isAiming() {
        return AimManager.INSTANCE.isEnabled();
    }

    public double getDistanceToTarget() {
        return currentTarget != null ? mc.player.distanceTo(currentTarget) : -1;
    }
}