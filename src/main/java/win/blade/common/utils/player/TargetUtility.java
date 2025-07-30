package win.blade.common.utils.player;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import win.blade.common.gui.impl.gui.setting.implement.BooleanSetting;
import win.blade.common.gui.impl.gui.setting.implement.GroupSetting;
import win.blade.common.utils.minecraft.MinecraftInstance;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Автор: NoCap
 * Дата создания: 28.06.2025
 */
public class TargetUtility implements MinecraftInstance {

    public enum TargetType {
        UNARMORED_PLAYERS,
        ARMORED_PLAYERS,
        INVISIBLE_PLAYERS,
        TEAMMATES,
        MOBS,
        ANIMALS,
        VILLAGERS
    }

    private static final Set<TargetType> selectedTypes = new HashSet<>();

    public static void setSelectedTypes(Set<TargetType> types) {
        selectedTypes.clear();
        selectedTypes.addAll(types);
    }

    public static boolean isValidTarget(Entity entity) {
        if (mc.player == null || entity == null) return false;
        if (entity == mc.player) return false;
        if (!(entity instanceof LivingEntity living)) return false;
        if (living.isDead() || !living.isAlive()) return false;

        if (entity instanceof PlayerEntity player) {
            boolean isTeammate = PlayerUtility.isTeammate(player);
            if (isTeammate) {
                return selectedTypes.contains(TargetType.TEAMMATES);
            }
            if (player.isInvisible()) {
                return selectedTypes.contains(TargetType.INVISIBLE_PLAYERS);
            }
            boolean hasArmor = PlayerUtility.hasArmor(player);
            if (hasArmor) {
                return selectedTypes.contains(TargetType.ARMORED_PLAYERS);
            } else {
                return selectedTypes.contains(TargetType.UNARMORED_PLAYERS);
            }
        }

        if (entity instanceof MobEntity) {
            return selectedTypes.contains(TargetType.MOBS);
        }
        if (entity instanceof AnimalEntity) {
            return selectedTypes.contains(TargetType.ANIMALS);
        }
        if (entity instanceof VillagerEntity) {
            return selectedTypes.contains(TargetType.VILLAGERS);
        }

        return false;
    }

    public static List<LivingEntity> getValidTargets(double maxTargetDistance) {
        if (mc.player == null || mc.world == null) return List.of();
        Box searchArea = mc.player.getBoundingBox().expand(maxTargetDistance);
        return mc.world.getOtherEntities(mc.player, searchArea)
                .stream()
                .filter(TargetUtility::isValidTarget)
                .map(entity -> (LivingEntity) entity)
                .collect(Collectors.toList());
    }

    public static void updateTargetTypes(GroupSetting targetGroup) {
        Set<TargetType> newSelectedTypes = targetGroup.getSubSettings().stream()
                .filter(setting -> setting instanceof BooleanSetting && ((BooleanSetting) setting).getValue())
                .map(setting -> switch (setting.getName()) {
                    case "Игроки без брони" -> TargetType.UNARMORED_PLAYERS;
                    case "Игроки с бронёй" -> TargetType.ARMORED_PLAYERS;
                    case "Невидимые игроки" -> TargetType.INVISIBLE_PLAYERS;
                    case "Тиммейты" -> TargetType.TEAMMATES;
                    case "Мобы" -> TargetType.MOBS;
                    case "Животные" -> TargetType.ANIMALS;
                    case "Жители" -> TargetType.VILLAGERS;
                    default -> null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        setSelectedTypes(newSelectedTypes);
    }

    public static LivingEntity findBestTarget(float range, String sortMode) {
        if (mc.player == null || mc.world == null) return null;

        List<LivingEntity> potentialTargets = getValidTargets(range);
        if (potentialTargets.isEmpty()) {
            return null;
        }

        Comparator<LivingEntity> comparator;
        switch (sortMode) {
            case "Здоровье":
                comparator = Comparator.comparing(LivingEntity::getHealth);
                break;
            case "Броня":
                comparator = Comparator.comparing(LivingEntity::getArmor);
                break;
            case "Поле зрения":
                comparator = Comparator.comparing(TargetUtility::getFOVAngle);
                break;
            case "Общая":
                comparator = Comparator.comparing(LivingEntity::getHealth)
                        .thenComparing(LivingEntity::getArmor)
                        .thenComparing(e -> mc.player.distanceTo(e));
                break;
            default:
                comparator = Comparator.comparing(e -> mc.player.distanceTo(e));
        }

        return potentialTargets.stream()
                .min(comparator)
                .orElse(null);
    }

    private static float getFOVAngle(LivingEntity e) {
        double difX = e.getX() - mc.player.getX();
        double difZ = e.getZ() - mc.player.getZ();
        float yaw = (float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(difZ, difX)) - 90.0);
        return Math.abs(yaw - MathHelper.wrapDegrees(mc.player.getYaw()));
    }
}