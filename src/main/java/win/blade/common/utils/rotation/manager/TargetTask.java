package win.blade.common.utils.rotation.manager;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import win.blade.common.utils.rotation.core.AimSettings;
import win.blade.common.utils.rotation.core.ViewDirection;

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
        return false;
    }
}