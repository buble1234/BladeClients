package win.blade.common.utils.player;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.AxeItem;
import net.minecraft.item.TridentItem;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.Hand;
import win.blade.common.utils.minecraft.MinecraftInstance;

public class AttackUtility implements MinecraftInstance {

    public enum AttackMode {
        OLD("1.8"),
        NEW("1.9");

        private final String name;

        AttackMode(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public enum CriticalMode {
        NONE("None"),
        ALWAYS("Always"),
        JUMP_ONLY("Jump Only"),
        ADAPTIVE("Adaptive");

        private final String name;

        CriticalMode(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private static long lastAttackTime = 0;
    private static boolean isAttacking = false;

    public static boolean canAttack(AttackMode mode) {
        if (mc.player == null || mc.interactionManager == null) return false;

        long currentTime = System.currentTimeMillis();
        long timeSinceLastAttack = currentTime - lastAttackTime;

        return switch (mode) {
            case OLD -> timeSinceLastAttack >= 50;
            case NEW -> mc.player.getAttackCooldownProgress(0.5f) >= 0.9f;
        };
    }

    public static void performAttack(Entity target, AttackMode mode, CriticalMode criticalMode) {
        if (mc.player == null || mc.interactionManager == null || target == null) return;
        if (!canAttack(mode)) return;

        boolean shouldCritical = shouldPerformCritical(target, criticalMode);

        isAttacking = true;

        if (mc.getNetworkHandler() != null) {
            mc.getNetworkHandler().sendPacket(PlayerInteractEntityC2SPacket.attack(target, mc.player.isSneaking()));
        }

        mc.interactionManager.attackEntity(mc.player, target);

        mc.player.swingHand(getAttackHand());

        handleSprintReset(target);

        lastAttackTime = System.currentTimeMillis();
        isAttacking = false;
    }

    private static boolean shouldPerformCritical(Entity target, CriticalMode mode) {
        if (mc.player == null || !(target instanceof LivingEntity living)) return false;

        return switch (mode) {
            case NONE -> false;
            case ALWAYS -> canCritical();
            case JUMP_ONLY -> canCritical() && !mc.player.isOnGround(); // Только в прыжке!
            case ADAPTIVE -> {
                float targetHealth = living.getHealth();
                float damage = getAttackDamage();
                float criticalDamage = damage * 1.5f;
                yield canCritical() && (targetHealth <= criticalDamage || targetHealth > damage * 2);
            }
        };
    }

    private static boolean canCritical() {
        if (mc.player == null) return false;

        return !mc.player.isOnGround() &&
                !mc.player.isClimbing() &&
                !mc.player.isSwimming() &&
                !mc.player.hasVehicle() &&
                !mc.player.hasStatusEffect(StatusEffects.BLINDNESS) &&
                mc.player.getVelocity().y < 0; // Падаем вниз
    }

    private static Hand getAttackHand() {
        if (mc.player == null) return Hand.MAIN_HAND;

        ItemStack mainHand = mc.player.getMainHandStack();
        ItemStack offHand = mc.player.getOffHandStack();

        if (isWeapon(mainHand)) {
            return Hand.MAIN_HAND;
        } else if (isWeapon(offHand)) {
            return Hand.OFF_HAND;
        }

        return Hand.MAIN_HAND;
    }

    private static boolean isWeapon(ItemStack stack) {
        if (stack.isEmpty()) return false;
        return stack.getItem() instanceof SwordItem ||
                stack.getItem() instanceof AxeItem ||
                stack.getItem() instanceof TridentItem;
    }

    private static float getAttackDamage() {
        if (mc.player == null) return 1.0f;

        ItemStack weapon = mc.player.getMainHandStack();
        if (weapon.isEmpty()) return 1.0f;

        float damage = 1.0f;

        if (weapon.getItem() instanceof SwordItem) {
            damage = 4.0f;
        } else if (weapon.getItem() instanceof AxeItem) {
            damage = 6.0f;
        } else if (weapon.getItem() instanceof TridentItem) {
            damage = 9.0f;
        }

        return damage;
    }

    private static void handleSprintReset(Entity target) {
        if (mc.player == null) return;

        boolean wasSprinting = mc.player.isSprinting();

        if (wasSprinting) {
            mc.player.setSprinting(false);
        }

        if (wasSprinting && SprintUtility.isKeepSprint()) {
            mc.execute(() -> {
                if (mc.player != null && SprintUtility.canSprint()) {
                    mc.player.setSprinting(true);
                }
            });
        }
    }

    public static boolean isValidWeapon(ItemStack stack) {
        return isWeapon(stack);
    }

    public static boolean isAttacking() {
        return isAttacking;
    }

    public static long getLastAttackTime() {
        return lastAttackTime;
    }

    public static float getAttackCooldown() {
        return mc.player != null ? mc.player.getAttackCooldownProgress(0.5f) : 0.0f;
    }

    public static double getAttackRange() {
        if (mc.player == null) return 3.0;

        ItemStack weapon = mc.player.getMainHandStack();
        if (weapon.isEmpty()) return 3.0;

        if (weapon.getItem() instanceof TridentItem) {
            return 5.0;
        } else if (weapon.getItem() instanceof SwordItem) {
            return 3.5;
        } else if (weapon.getItem() instanceof AxeItem) {
            return 3.0;
        }

        return 3.0;
    }

    public static boolean canReachTarget(Entity target) {
        if (mc.player == null || target == null) return false;

        double distance = mc.player.distanceTo(target);
        double maxRange = getAttackRange();

        return distance <= maxRange;
    }
}