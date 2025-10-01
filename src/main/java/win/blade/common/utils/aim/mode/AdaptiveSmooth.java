package win.blade.common.utils.aim.mode;

import net.minecraft.util.math.MathHelper;
import win.blade.common.utils.aim.core.SmoothTransition;
import win.blade.common.utils.aim.core.ViewDirection;

/**
 * Автор: NoCap
 * Дата создания: 18.06.2025
 */

public record AdaptiveSmooth(float baseSpeed, float acceleration) implements SmoothTransition {

    public AdaptiveSmooth(float speed) {
        this(speed, 10);
    }

    @Override
    public ViewDirection interpolate(ViewDirection current, ViewDirection target) {
        float deltaYaw = MathHelper.wrapDegrees(target.yaw() - current.yaw());
        float deltaPitch = MathHelper.clamp(target.pitch(), -90.0f, 90.0f) - current.pitch();

        float interpolationStepYaw = baseSpeed * (1.0f + acceleration * Math.abs(deltaYaw) / 180.0f);
        float interpolationStepPitch = baseSpeed * (1.0f + acceleration * Math.abs(deltaPitch) / 180.0f);

        float newYaw = current.yaw() + MathHelper.clamp(deltaYaw, -interpolationStepYaw, interpolationStepYaw);

        float newPitch = current.pitch() + MathHelper.clamp(deltaPitch, -interpolationStepPitch, interpolationStepPitch);

//        newYaw = MathHelper.wrapDegrees(newYaw);
        newPitch = MathHelper.clamp(newPitch, -90.0f, 90.0f);

        return new ViewDirection(newYaw, newPitch);
    }
}