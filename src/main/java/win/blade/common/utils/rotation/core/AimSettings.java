package win.blade.common.utils.rotation.core;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import win.blade.common.utils.rotation.manager.TargetTask;
import win.blade.common.utils.rotation.mode.AdaptiveSmooth;

public record AimSettings(
        SmoothTransition transitionMode,
        boolean enableViewSync,
        boolean enableMovementFix,
        boolean enableSilentAim
) {
    public static final AimSettings SAIM = new AimSettings(
            new AdaptiveSmooth(12f), false, true, true
    );

    public static final AimSettings VSYNC = new AimSettings(
            new AdaptiveSmooth(12f), true, false, false
    );

    public TargetTask buildTask(ViewDirection direction, Vec3d position, Entity entity) {
        return new TargetTask(direction, position, entity, this);
    }

    public TargetTask buildTask(ViewDirection direction) {
        return new TargetTask(direction, null, null, this);
    }
}