package win.blade.common.utils.player;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import win.blade.common.gui.impl.menu.settings.impl.MultiBooleanSetting;
import win.blade.common.utils.minecraft.MinecraftInstance;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

        if (entity instanceof VillagerEntity) {
            return selectedTypes.contains(TargetType.VILLAGERS);
        }

        if (entity instanceof PlayerEntity player) {
            boolean hasArmor = PlayerUtility.hasArmor(player);
            boolean isTeammate = PlayerUtility.isTeammate(player);
            boolean isInvisible = player.isInvisible();

            if (isTeammate) {
                return selectedTypes.contains(TargetType.TEAMMATES);
            }

            if (isInvisible && selectedTypes.contains(TargetType.INVISIBLE_PLAYERS)) {
                return true;
            }

            if (hasArmor && selectedTypes.contains(TargetType.ARMORED_PLAYERS)) {
                return true;
            }

            if (!hasArmor && selectedTypes.contains(TargetType.UNARMORED_PLAYERS)) {
                return true;
            }

            return false;
        }

        if (entity instanceof AnimalEntity) {
            return selectedTypes.contains(TargetType.ANIMALS);
        }

        if (entity instanceof MobEntity) {
            return selectedTypes.contains(TargetType.MOBS);
        }

        return false;
    }

    public static List<Entity> getValidTargets(double maxTargetDistance) {
        if (mc.player == null || mc.world == null) return List.of();
        Box searchArea = mc.player.getBoundingBox().expand(maxTargetDistance);
        return mc.world.getOtherEntities(mc.player, searchArea)
                .stream()
                .filter(TargetUtility::isValidTarget)
                .collect(Collectors.toList());
    }

    public static void updateTargetTypes(MultiBooleanSetting value) {
        Set<TargetType> newSelectedTypes = value.getValues().stream()
                .filter(setting -> setting.getValue())
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
                .filter(type -> type != null)
                .collect(Collectors.toSet());

        setSelectedTypes(newSelectedTypes);
    }

    public static Entity findBestTarget(float range) {
        if (mc.player == null || mc.world == null) return null;

        List<Entity> potentialTargets = getValidTargets(range);

        return potentialTargets.stream()
                .filter(entity -> entity.isAlive() && !entity.isSpectator())
                .min((e1, e2) -> Double.compare(mc.player.distanceTo(e1), mc.player.distanceTo(e2)))
                .orElse(null);
    }
}