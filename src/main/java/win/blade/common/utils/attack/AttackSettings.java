package win.blade.common.utils.attack;

/**
 * Автор: NoCap
 * Дата создания: 28.06.2025
 */
public record AttackSettings(
        AttackMode attackMode,
        CriticalMode criticalMode,
        float cps,
        boolean unpressShield,
        boolean checkEating,
        double attackRange,
        boolean resetSprint
) {}