package win.blade.common.utils.attack;

import net.minecraft.entity.LivingEntity;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.util.Hand;
import win.blade.common.utils.minecraft.MinecraftInstance;
import win.blade.common.utils.player.PlayerUtility;

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

        if (mc.player.isBlocking() && settings.unpressShield()) {
            mc.interactionManager.stopUsingItem(mc.player);
        }

        settings.attackMode().handleSprintBeforeAttack(settings, state);

        sendAttackPackets(target);

        settings.attackMode().handleSprintAfterAttack(settings, state);

        state.setLastAttackTime(System.currentTimeMillis());
        state.setIsAttacking(false);
    }

    private static void sendAttackPackets(LivingEntity target) {
        if (mc.getNetworkHandler() != null) {
            mc.getNetworkHandler().sendPacket(PlayerInteractEntityC2SPacket.attack(target, mc.player.isSneaking()));
            mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
        }
        //mc.player.attack(target);
        mc.player.resetLastAttackedTicks();
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