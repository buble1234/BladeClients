package win.blade.common.utils.attack;

import net.minecraft.entity.effect.StatusEffects;
import win.blade.common.utils.minecraft.MinecraftInstance;
import win.blade.common.utils.player.PlayerUtility;

/**
 * Автор: NoCap
 * Дата создания: 09.07.2025
 */
public enum CriticalMode implements MinecraftInstance {
    NONE {
        @Override
        public boolean canCritical(AttackSettings settings, AttackState state) {
            return true;
        }
    },
    ALWAYS {
        @Override
        public boolean canCritical(AttackSettings settings, AttackState state) {
            return isPlayerInCriticalState() || (System.currentTimeMillis() - state.getLastJumpTime() < 200);
        }
    },
    ADAPTIVE {
        @Override
        public boolean canCritical(AttackSettings settings, AttackState state) {
            if (mc.player.input.playerInput.jump() || mc.player.getHealth() < 12.0f) {
                return isPlayerInCriticalState() || (System.currentTimeMillis() - state.getLastJumpTime() < 200);
            }
            return true;
        }
    };

    public abstract boolean canCritical(AttackSettings settings, AttackState state);

    private static boolean isPlayerInCriticalState() {
        if (mc.player.isGliding()) {
            return true;
        }
        return !mc.player.isOnGround() && !mc.player.isInLava() && !mc.player.isSubmergedInWater() && !mc.player.hasStatusEffect(StatusEffects.BLINDNESS) && mc.player.fallDistance > 0;
    }
}