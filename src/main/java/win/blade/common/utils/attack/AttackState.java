package win.blade.common.utils.attack;

/**
 * Автор: NoCap
 * Дата создания: 09.07.2025
 */
class AttackState {
    private long lastAttackTime = 0;
    private boolean isAttacking = false;
    private long lastJumpTime = 0;
    private long lastSprintResetTime = 0;

    public long getLastAttackTime() {
        return lastAttackTime;
    }

    public void setLastAttackTime(long lastAttackTime) {
        this.lastAttackTime = lastAttackTime;
    }

    public boolean isAttacking() {
        return isAttacking;
    }

    public void setIsAttacking(boolean isAttacking) {
        this.isAttacking = isAttacking;
    }

    public long getLastJumpTime() {
        return lastJumpTime;
    }

    public void setLastJumpTime(long lastJumpTime) {
        this.lastJumpTime = lastJumpTime;
    }

    public long getLastSprintResetTime() {
        return lastSprintResetTime;
    }

    public void setLastSprintResetTime(long lastSprintResetTime) {
        this.lastSprintResetTime = lastSprintResetTime;
    }
}