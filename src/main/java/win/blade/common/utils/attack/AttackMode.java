package win.blade.common.utils.attack;

import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import win.blade.common.utils.minecraft.MinecraftInstance;

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
            return mc.player.getAttackCooldownProgress(0.5f) >= 0.91f;
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
            if (settings.resetSprint() && !mc.player.isSprinting()) {
                long currentTime = System.currentTimeMillis();
                //if (currentTime - state.getLastSprintResetTime() >= 400) {
                    enableSprint();
                //}
            }
        }

        private void disableSprint() {
            mc.player.setSprinting(false);
            ClientCommandC2SPacket.Mode mode = ClientCommandC2SPacket.Mode.STOP_SPRINTING;
            mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, mode));
        }

        private void enableSprint() {
            mc.player.setSprinting(true);
            ClientCommandC2SPacket.Mode mode = ClientCommandC2SPacket.Mode.START_SPRINTING;
            mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, mode));
        }
    };

    public abstract boolean canAttackTiming(AttackSettings settings, AttackState state);
    public abstract void handleSprintBeforeAttack(AttackSettings settings, AttackState state);
    public abstract void handleSprintAfterAttack(AttackSettings settings, AttackState state);
}