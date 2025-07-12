package win.blade.common.utils.attack;

import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
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
    },
    SMART {
        @Override
        public boolean canCritical(AttackSettings settings, AttackState state) {

            boolean reasonForSkipCrit = mc.player.getAbilities().flying || mc.player.isGliding() || mc.player.hasStatusEffect(StatusEffects.BLINDNESS) || mc.player.hasStatusEffect(StatusEffects.SLOW_FALLING);

            if (reasonForSkipCrit) {
                return true;
            }

            if (!mc.options.jumpKey.isPressed() && mc.player.isOnGround()) {
                return true;
            }

            if (mc.player.isInLava() || mc.player.isSubmergedInWater()) {
                return true;
            }

            if (!mc.options.jumpKey.isPressed() && isAboveWater()) {
                return true;
            }

            if (mc.player.fallDistance > 1 && mc.player.fallDistance < 1.14) {
                return false;
            }

            float fdThreshold = (float) (Math.random() * (0.7 - 0.15) + 0.15);
            return !mc.player.isOnGround() && mc.player.fallDistance > fdThreshold;
        }

        private boolean isAboveWater() {
            return mc.player.isSubmergedInWater() || mc.world.getBlockState(BlockPos.ofFloored(mc.player.getPos().add(0, -0.4, 0))).getBlock() == Blocks.WATER;
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