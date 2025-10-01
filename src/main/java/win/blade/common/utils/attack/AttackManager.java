package win.blade.common.utils.attack;

import net.minecraft.entity.LivingEntity;
import win.blade.common.utils.math.MathUtility;
import win.blade.common.utils.minecraft.MinecraftInstance;
import win.blade.common.utils.player.PlayerUtility;
import win.blade.core.Manager;
import win.blade.core.module.storage.move.AutoSprintModule;

/**
 * Автор: NoCap
 * Дата создания: 28.06.2025
 */
public class AttackManager implements MinecraftInstance {

    private static AttackState state = new AttackState();

    public static void attack(LivingEntity target, AttackSettings settings) {
        if (AttackManager.canAttack(target, settings)) {
            AttackManager.performAttack(target, settings);
        }
    }
    public static boolean canAttack(LivingEntity target, AttackSettings settings) {
        if (mc.player == null || mc.interactionManager == null || target == null) {
            return false;
        }

        if (mc.player.distanceTo(target) > settings.attackRange()) {
            return false;
        }

        if (!settings.attackMode().canAttackTiming(settings, state)) {
            return false;
        }

        if (settings.criticalMode() != CriticalMode.NONE && !settings.criticalMode().canCritical(settings, state)) {
            return false;
        }

        if (settings.checkEating() && PlayerUtility.isEating()) {
            return false;
        }

        return true;
    }

    public static void performAttack(LivingEntity target, AttackSettings settings) {
        if (!canAttack(target, settings)) return;

        state.setIsAttacking(true);

        if (settings.shieldBreaker() && ShieldBreaker.breakShield(target, settings)) {
            state.setIsAttacking(false);
            return;
        }

        if (mc.player.isBlocking() && settings.unpressShield()) {
            mc.interactionManager.stopUsingItem(mc.player);
        }
        boolean wasSprint = mc.player.isSprinting();
        if(!mc.player.isGliding())
            settings.attackMode().handleSprintBeforeAttack(settings, state);

        sendAttackPackets(target);

        if (wasSprint != mc.player.isSprinting()) {
            settings.attackMode().handleSprintAfterAttack(settings, state);
        }

        state.setLastAttackTime(System.currentTimeMillis());
        state.setIsAttacking(false);
    }

    private static void sendAttackPackets(LivingEntity target) {
        if (mc.getNetworkHandler() != null) {
            mc.interactionManager.attackEntity(mc.player, target);
        }
        mc.player.swingHand(PlayerUtility.getAttackHand());
    }

    public static boolean isAttacking() {
        return state.isAttacking();
    }

    public static long getLastAttackTime() {
        return state.getLastAttackTime();
    }

    public static float getAttackCooldown() {
        return mc.player != null ? mc.player.getAttackCooldownProgress(0.5f) : 0.0f;
    }

    public static void setLastJumpTime(long time) {
        state.setLastJumpTime(time);
    }
}