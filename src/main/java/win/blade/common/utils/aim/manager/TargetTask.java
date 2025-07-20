package win.blade.common.utils.aim.manager;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import win.blade.common.utils.aim.core.AimSettings;
import win.blade.common.utils.aim.core.ViewDirection;
import win.blade.common.utils.aim.base.AimCalculator;

/**
 * Автор: NoCap
 * Дата создания: 18.06.2025
 */

public record TargetTask(
        ViewDirection targetDirection,
        Vec3d targetPosition,
        Entity targetEntity,
        AimSettings settings
) {
    public ViewDirection calculateNext(ViewDirection currentDirection) {
        return settings.transitionMode().interpolate(currentDirection, targetDirection, targetPosition, targetEntity);
    }

    public boolean isCompleted(ViewDirection currentDirection) {
        if (targetEntity != null || targetPosition != null) {
            return false;
        }
        return AimCalculator.getAngleDifference(currentDirection, targetDirection) < 0.5;
    }
}