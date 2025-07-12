package win.blade.common.utils.attack;

import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import win.blade.common.utils.minecraft.MinecraftInstance;

import static win.blade.common.utils.network.PacketUtility.sendPacket;

/**
 * Автор: NoCap
 * Дата создания: 09.07.2025
 */
public enum AttackMode implements MinecraftInstance {
    LEGACY {
        @Override
        public boolean canAttackTiming(AttackSettings settings, AttackState state) {
            long currentTime = System.currentTimeMillis();
            long timeSinceLastAttack = currentTime - state.getLastAttackTime();
            return timeSinceLastAttack >= (1000L / settings.cps());
        }

        @Override
        public void handleSprintBeforeAttack(AttackSettings settings, AttackState state) {
        }

        @Override
        public void handleSprintAfterAttack(AttackSettings settings, AttackState state) {
        }
    },
    MODERN {
        @Override
        public boolean canAttackTiming(AttackSettings settings, AttackState state) {
            return mc.player.getAttackCooldownProgress(0.5f) >= 1.0f;
        }

        @Override
        public void handleSprintBeforeAttack(AttackSettings settings, AttackState state) {
            if (settings.resetSprint() && mc.player.isSprinting()) {
                disableSprint();
                state.setLastSprintResetTime(System.currentTimeMillis());
            }
        }

        @Override
        public void handleSprintAfterAttack(AttackSettings settings, AttackState state) {
            if (settings.resetSprint()) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - state.getLastSprintResetTime() >= 400) {
                    enableSprint();
                }
            }
        }

        private void disableSprint() {
            mc.player.setSprinting(false);
            mc.options.sprintKey.setPressed(false);
            //sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.STOP_SPRINTING));
        }

        private void enableSprint() {
            mc.player.setSprinting(true);
            mc.options.sprintKey.setPressed(true);
            //sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_SPRINTING));
        }
    };

    public abstract boolean canAttackTiming(AttackSettings settings, AttackState state);
    public abstract void handleSprintBeforeAttack(AttackSettings settings, AttackState state);
    public abstract void handleSprintAfterAttack(AttackSettings settings, AttackState state);
}