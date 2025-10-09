package win.blade.common.utils.aim.core;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import win.blade.common.utils.aim.manager.TargetTask;
import win.blade.common.utils.aim.mode.AdaptiveSmooth;

/**
 * Автор: NoCap
 * Дата создания: 18.06.2025
 */

public record AimSettings(
        SmoothTransition transitionMode,
        boolean enableViewSync,
        boolean enableMovementFix,
        boolean enableSilentAim
) {

    public TargetTask buildTask(ViewDirection direction, Vec3d position, Entity entity) {
        return new TargetTask(direction, position, entity, this);
    }

    public TargetTask buildTask(ViewDirection direction) {
        return new TargetTask(direction, null, null, this);
    }
}