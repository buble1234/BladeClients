package win.blade.common.utils.player;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Box;
import net.minecraft.world.RaycastContext;
import net.minecraft.util.hit.HitResult;
import win.blade.common.utils.minecraft.MinecraftInstance;

import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Comparator;
import java.util.stream.Collectors;

/**
 * Автор: NoCap && Claude
 * Дата создания: 18.06.2025
 */

public class TargetUtility implements MinecraftInstance {

    public static boolean targetPlayers = true;
    public static boolean targetMobs = false;
    public static boolean targetAnimals = false;
    public static boolean targetFriends = false;
    public static boolean targetTeammates = false;
    public static boolean targetInvisible = false;
    public static boolean targetNaked = false;
    public static boolean throughWalls = false;
    public static boolean prioritizeLowHealth = true;
    public static boolean prioritizeClosest = true;
    public static double maxTargetDistance = 6.0;
    public static double fovLimit = 180.0; // Лимит FOV для таргетинга

    public enum SortMode {
        DISTANCE("Distance"),
        HEALTH("Health"),
        ARMOR("Armor"),
        FOV("FOV"),
        HYBRID("Hybrid"),
        SMART("Smart");

        private final String name;

        SortMode(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public static SortMode sortMode = SortMode.SMART;

    private static final Set<String> friends = new HashSet<>();
    private static final Set<String> teammates = new HashSet<>();
    private static Entity lastTarget = null;
    private static long lastTargetTime = 0;

    public static boolean isValidTarget(Entity entity) {
        if (mc.player == null || entity == null) return false;
        if (entity == mc.player) return false;
        if (!(entity instanceof LivingEntity living)) return false;
        if (living.isDead() || !living.isAlive()) return false;

        // Проверка дистанции
        if (getDistanceTo(entity) > maxTargetDistance) return false;

        // Проверка FOV
        if (getFovAngle(entity) > fovLimit) return false;

        // Проверка видимости через стены
        if (!throughWalls && !hasLineOfSight(entity)) return false;

        // Проверка невидимости
        if (entity.isInvisible() && !targetInvisible) return false;

        if (entity instanceof PlayerEntity player) {
            if (!targetPlayers) return false;

            // Проверка друзей
            if (friends.contains(player.getName().getString()) && !targetFriends) return false;

            // Проверка тиммейтов
            if (isTeammate(player) && !targetTeammates) return false;

            // Проверка на голых игроков
            if (targetNaked && !isNaked(player)) return false;

            // Проверка на бота (статичные игроки)
            if (isBot(player)) return false;

            return true;
        } else if (entity instanceof AnimalEntity) {
            return targetAnimals;
        } else if (entity instanceof MobEntity) {
            return targetMobs;
        }

        return false;
    }

    public static boolean hasLineOfSight(Entity entity) {
        if (mc.player == null || mc.world == null) return false;

        Vec3d eyePos = mc.player.getEyePos();
        Vec3d targetPos = entity.getEyePos();

        RaycastContext context = new RaycastContext(
                eyePos,
                targetPos,
                RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE,
                mc.player
        );

        HitResult result = mc.world.raycast(context);
        return result.getType() == HitResult.Type.MISS;
    }

    public static boolean isTeammate(PlayerEntity player) {
        if (mc.player == null) return false;

        if (teammates.contains(player.getName().getString())) return true;

        return mc.player.getScoreboardTeam() != null &&
                mc.player.getScoreboardTeam().equals(player.getScoreboardTeam());
    }

    public static boolean isNaked(PlayerEntity player) {
        for (ItemStack armor : player.getArmorItems()) {
            if (!armor.isEmpty()) return false;
        }
        return true;
    }

    public static boolean isBot(PlayerEntity player) {
        // Простая проверка на ботов - проверяем движение
        Vec3d velocity = player.getVelocity();
        return velocity.lengthSquared() < 0.001 &&
                player.age > 100 && // Игрок существует более 5 секунд
                player.getPitch() == 0.0f && player.getYaw() == 0.0f;
    }

    public static double getDistanceTo(Entity entity) {
        return mc.player != null ? mc.player.distanceTo(entity) : Double.MAX_VALUE;
    }

    public static float getHealth(Entity entity) {
        if (entity instanceof LivingEntity living) {
            return living.getHealth() + living.getAbsorptionAmount();
        }
        return 0;
    }

    public static float getMaxHealth(Entity entity) {
        if (entity instanceof LivingEntity living) {
            return living.getMaxHealth();
        }
        return 0;
    }

    public static float getHealthPercentage(Entity entity) {
        if (entity instanceof LivingEntity living) {
            return (living.getHealth() / living.getMaxHealth()) * 100.0f;
        }
        return 0;
    }

    public static int getArmorValue(Entity entity) {
        if (entity instanceof LivingEntity living) {
            return living.getArmor();
        }
        return 0;
    }

    public static double getFovAngle(Entity entity) {
        if (mc.player == null) return Double.MAX_VALUE;

        Vec3d toEntity = entity.getPos().subtract(mc.player.getPos()).normalize();
        Vec3d lookDirection = Vec3d.fromPolar(mc.player.getPitch(), mc.player.getYaw()).normalize();

        double dot = toEntity.dotProduct(lookDirection);
        return Math.toDegrees(Math.acos(MathHelper.clamp(dot, -1.0, 1.0)));
    }

    public static double getThreatLevel(Entity entity) {
        if (!(entity instanceof LivingEntity living)) return 0;

        double threat = 0;

        // Базовая угроза по здоровью
        threat += living.getHealth() / 20.0;

        // Угроза по броне
        threat += living.getArmor() / 20.0;

        // Угроза по оружию (для игроков)
        if (entity instanceof PlayerEntity player) {
            ItemStack weapon = player.getMainHandStack();
            if (weapon.getItem() instanceof net.minecraft.item.SwordItem) {
                threat += 2.0;
            } else if (weapon.getItem() instanceof net.minecraft.item.AxeItem) {
                threat += 1.5;
            }
        }

        // Угроза по дистанции (ближе = опаснее)
        double distance = getDistanceTo(entity);
        threat += (10.0 - distance) / 10.0;

        return threat;
    }

    public static void sortTargets(List<Entity> targets) {
        switch (sortMode) {
            case DISTANCE:
                targets.sort(Comparator.comparingDouble(TargetUtility::getDistanceTo));
                break;
            case HEALTH:
                targets.sort(Comparator.comparingDouble(TargetUtility::getHealth));
                break;
            case ARMOR:
                targets.sort(Comparator.comparingInt(TargetUtility::getArmorValue));
                break;
            case FOV:
                targets.sort(Comparator.comparingDouble(TargetUtility::getFovAngle));
                break;
            case HYBRID:
                targets.sort((e1, e2) -> {
                    double score1 = calculateHybridScore(e1);
                    double score2 = calculateHybridScore(e2);
                    return Double.compare(score1, score2);
                });
                break;
            case SMART:
                targets.sort((e1, e2) -> {
                    double score1 = calculateSmartScore(e1);
                    double score2 = calculateSmartScore(e2);
                    return Double.compare(score1, score2);
                });
                break;
        }
    }

    private static double calculateHybridScore(Entity entity) {
        double distance = getDistanceTo(entity);
        double health = getHealth(entity);
        double armor = getArmorValue(entity);
        double fov = getFovAngle(entity);

        double distanceScore = distance / maxTargetDistance;
        double healthScore = health / 40.0; // Максимум ~40 HP
        double armorScore = armor / 20.0; // Максимум 20 брони
        double fovScore = fov / 180.0;

        return distanceScore * 0.3 + healthScore * 0.3 + armorScore * 0.2 + fovScore * 0.2;
    }

    private static double calculateSmartScore(Entity entity) {
        double distance = getDistanceTo(entity);
        double health = getHealthPercentage(entity);
        double threat = getThreatLevel(entity);
        double fov = getFovAngle(entity);

        // Умная система приоритетов
        double score = 0;

        // Приоритет игрокам
        if (entity instanceof PlayerEntity) {
            score -= 10; // Негативная оценка = высокий приоритет
        }

        // Приоритет близким целям
        score += distance * 2;

        // Приоритет слабым целям
        score += health / 10.0;

        // Учитываем угрозу
        score -= threat;

        // Приоритет целям в поле зрения
        score += fov / 45.0;

        // Sticky targeting - приоритет последней цели
        if (entity == lastTarget && System.currentTimeMillis() - lastTargetTime < 2000) {
            score -= 5;
        }

        return score;
    }

    public static List<Entity> getValidTargets() {
        if (mc.player == null || mc.world == null) return List.of();

        Box searchArea = mc.player.getBoundingBox().expand(maxTargetDistance);

        return mc.world.getOtherEntities(mc.player, searchArea)
                .stream()
                .filter(TargetUtility::isValidTarget)
                .collect(Collectors.toList());
    }

    public static Entity getBestTarget() {
        List<Entity> targets = getValidTargets();
        if (targets.isEmpty()) return null;

        sortTargets(targets);
        Entity bestTarget = targets.get(0);

        if (bestTarget != lastTarget) {
            lastTarget = bestTarget;
            lastTargetTime = System.currentTimeMillis();
        }

        return bestTarget;
    }

    public static void addFriend(String name) {
        friends.add(name);
    }

    public static void removeFriend(String name) {
        friends.remove(name);
    }

    public static boolean isFriend(String name) {
        return friends.contains(name);
    }

    public static void clearFriends() {
        friends.clear();
    }

    public static void addTeammate(String name) {
        teammates.add(name);
    }

    public static void removeTeammate(String name) {
        teammates.remove(name);
    }

    public static void clearTeammates() {
        teammates.clear();
    }
}