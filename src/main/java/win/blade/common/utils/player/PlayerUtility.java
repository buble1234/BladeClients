package win.blade.common.utils.player;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.TridentItem;
import net.minecraft.util.Hand;
import win.blade.common.utils.minecraft.MinecraftInstance;

/**
 * Автор: NoCap
 * Дата создания: 28.06.2025
 */
public class PlayerUtility implements MinecraftInstance {

    public static boolean hasArmor(PlayerEntity player) {
        for (ItemStack item : player.getArmorItems()) {
            if (!item.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public static boolean isTeammate(PlayerEntity player) {
        if (mc.player == null) return false;
        int teamColorValue = mc.player.getTeamColorValue();
        return teamColorValue == player.getTeamColorValue() && teamColorValue != 16777215;
    }

    public static boolean isEating() {
        ItemStack mainHand = mc.player.getMainHandStack();
        ItemStack offHand = mc.player.getOffHandStack();

        return mc.player.isUsingItem() && ((mainHand.get(DataComponentTypes.FOOD) != null) || (offHand.get(DataComponentTypes.FOOD) != null));
    }

    public static boolean isWeapon(ItemStack stack) {
        if (stack.isEmpty()) return false;
        return stack.getItem() instanceof SwordItem || stack.getItem() instanceof AxeItem || stack.getItem() instanceof TridentItem;
    }

    public static Hand getAttackHand() {
        ItemStack mainHand = mc.player.getMainHandStack();
        ItemStack offHand = mc.player.getOffHandStack();

        if (PlayerUtility.isWeapon(mainHand)) return Hand.MAIN_HAND;
        if (PlayerUtility.isWeapon(offHand)) return Hand.OFF_HAND;
        return Hand.MAIN_HAND;
    }

    public static boolean isFalling() {
        return !mc.player.isOnGround() &&
             mc.player.fallDistance > 0 &&
             mc.player.getVelocity().y < 0 &&
             !mc.player.isClimbing() &&
             !mc.player.isSubmergedInWater() &&
             !mc.player.hasVehicle() &&
             !mc.player.hasStatusEffect(StatusEffects.BLINDNESS) &&
             !mc.player.hasStatusEffect(StatusEffects.LEVITATION);
    }

    public static boolean hasMovementRestrictions() {
        return mc.player.hasStatusEffect(StatusEffects.BLINDNESS)
                || mc.player.hasStatusEffect(StatusEffects.LEVITATION)
                || mc.player.isSubmergedInWater()
                || mc.player.isInLava()
                || mc.player.isClimbing()
                || mc.player.isTouchingWater();
    }
}