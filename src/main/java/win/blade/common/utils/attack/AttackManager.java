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

    public enum AttackMode {
        LEGACY,
        MODERN
    }

    public enum CriticalMode {
        NONE,
        JUMP,
        ADAPTIVE
    }

    private static long lastAttackTime = 0;
    private static boolean isAttacking = false;
    private static long lastJumpTime = 0;
    private static long lastSprintResetTime = 0;

    public static boolean canAttack(LivingEntity target, AttackSettings settings) {
        if (mc.player == null || mc.interactionManager == null || target == null) {
            return false;
        }

        if (mc.player.distanceTo(target) > settings.attackRange()) {
            return false;
        }

        long currentTime = System.currentTimeMillis();
        long timeSinceLastAttack = currentTime - lastAttackTime;
        if (settings.attackMode() == AttackMode.LEGACY && timeSinceLastAttack < (1000L / settings.cps())) {
            return false;
        }
        if (settings.attackMode() == AttackMode.MODERN && mc.player.getAttackCooldownProgress(0.5f) < 1.0f) {
            return false;
        }

        if (settings.criticalMode() != CriticalMode.NONE && !canCritical(settings)) {
            return false;
        }

        if (settings.checkEating() && PlayerUtility.isEating()) {
            return false;
        }

        return true;
    }

    public static void performAttack(LivingEntity target, AttackSettings settings) {
        if (!canAttack(target, settings)) return;

        isAttacking = true;

        if (mc.player.isBlocking() && settings.unpressShield()) {
            mc.interactionManager.stopUsingItem(mc.player);
        }

        boolean wasSprinting = mc.player.isSprinting();
        if (settings.attackMode() == AttackMode.MODERN && settings.resetSprint() && wasSprinting) {
            mc.player.setSprinting(false);
            lastSprintResetTime = System.currentTimeMillis();
        }

        if (mc.getNetworkHandler() != null) {
            mc.getNetworkHandler().sendPacket(PlayerInteractEntityC2SPacket.attack(target, mc.player.isSneaking()));
            mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
        }
        mc.player.resetLastAttackedTicks();
        mc.player.swingHand(PlayerUtility.getAttackHand());

        if (wasSprinting && settings.attackMode() == AttackMode.MODERN && settings.resetSprint()) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastSprintResetTime >= 400) {
                mc.options.sprintKey.setPressed(true);
                mc.player.setSprinting(true);
            }
        }

        lastAttackTime = System.currentTimeMillis();
        isAttacking = false;
    }

    private static boolean canCritical(AttackSettings settings) {
        if (mc.player == null) {
            return false;
        }

        if (settings.attackMode() == AttackMode.MODERN && mc.player.getAttackCooldownProgress(0.5f) < 1.0f) {
            return false;
        }

        if (PlayerUtility.hasMovementRestrictions()) {
            return false;
        }

        CriticalMode mode = settings.criticalMode();
        long timeSinceJump = System.currentTimeMillis() - lastJumpTime;
        boolean recentlyJumped = timeSinceJump < 200;

        if (mode == CriticalMode.JUMP) {
            return isPlayerInCriticalState() || recentlyJumped;
        } else if (mode == CriticalMode.ADAPTIVE) {
            if (mc.player.input.playerInput.jump() || mc.player.getHealth() < 12.0f) {
                return isPlayerInCriticalState() || recentlyJumped;
            }
            return true;
        }

        return false;
    }


    private static boolean isPlayerInCriticalState() {
        if (mc.player.isGliding()) {
            return true;
        }
        return !mc.player.isOnGround() && mc.player.getVelocity().y <= 0;
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

    public static void setLastJumpTime(long time) {
        lastJumpTime = time;
    }
}