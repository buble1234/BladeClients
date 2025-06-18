package win.blade.common.utils.player;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import win.blade.common.utils.minecraft.MinecraftInstance;
import win.blade.common.utils.rotation.base.AimCalculator;

import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Comparator;

public class TargetUtility implements MinecraftInstance {
    
    public static boolean targetPlayers = true;
    public static boolean targetMobs = false;
    public static boolean targetAnimals = false;
    public static boolean targetFriends = false;
    public static boolean targetTeammates = false;
    public static boolean targetInvisible = false;
    public static boolean targetNaked = false;
    
    public enum SortMode {
        DISTANCE("Distance"),
        HEALTH("Health"),
        ARMOR("Armor"),
        FOV("FOV"),
        HYBRID("Hybrid");
        
        private final String name;
        
        SortMode(String name) {
            this.name = name;
        }
        
        @Override
        public String toString() {
            return name;
        }
    }
    
    public static SortMode sortMode = SortMode.DISTANCE;
    
    private static final Set<String> friends = new HashSet<>();
    private static final Set<String> teammates = new HashSet<>();

    public static boolean isValidTarget(Entity entity) {
        if (mc.player == null || entity == null) return false;
        if (entity == mc.player) return false;
        if (!(entity instanceof LivingEntity living)) return false;
        if (living.isDead() || !living.isAlive()) return false;
        
        if (entity.isInvisible() && !targetInvisible) return false;
        
        if (entity instanceof PlayerEntity player) {
            if (!targetPlayers) return false;
            
            if (friends.contains(player.getName().getString()) && !targetFriends) return false;
            
            if (isTeammate(player) && !targetTeammates) return false;
            
            if (targetNaked && !isNaked(player)) return false;
            
            return true;
        } else if (entity instanceof AnimalEntity) {
            return targetAnimals;
        } else if (entity instanceof MobEntity) {
            return targetMobs;
        }
        
        return false;
    }

    public static boolean isTeammate(PlayerEntity player) {
        if (mc.player == null) return false;
        
        if (teammates.contains(player.getName().getString())) return true;
        
        return mc.player.getScoreboardTeam() != null && mc.player.getScoreboardTeam().equals(player.getScoreboardTeam());
    }

    public static boolean isNaked(PlayerEntity player) {
        for (ItemStack armor : player.getArmorItems()) {
            if (!armor.isEmpty()) return false;
        }
        return true;
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
        }
    }

    private static double calculateHybridScore(Entity entity) {
        double distance = getDistanceTo(entity);
        double health = getHealth(entity);
        double armor = getArmorValue(entity);
        double fov = getFovAngle(entity);
        
        double distanceScore = distance / 10.0;
        double healthScore = health / 20.0;
        double armorScore = armor / 20.0;
        double fovScore = fov / 180.0;
        
        return distanceScore * 0.4 + healthScore * 0.2 + armorScore * 0.2 + fovScore * 0.2;
    }
}