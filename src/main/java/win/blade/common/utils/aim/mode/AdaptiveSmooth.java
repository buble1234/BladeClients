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
        this(speed, 1.5f);
    }

    @Override
    public ViewDirection interpolate(ViewDirection current, ViewDirection target) {
        float yawDelta = MathHelper.wrapDegrees(target.yaw() - current.yaw());
        float pitchDelta = MathHelper.wrapDegrees(target.pitch() - current.pitch());

        double distance = Math.sqrt(yawDelta * yawDelta + pitchDelta * pitchDelta);

        float speedMultiplier = 1.0f;
        if (distance > 30.0) {
            speedMultiplier = acceleration;
        } else if (distance > 15.0) {
            float t = (float) ((distance - 15.0) / 15.0);
            speedMultiplier = 1.0f + (acceleration - 1.0f) * t;
        }

        float currentSpeed = baseSpeed * speedMultiplier;

        float smoothingFactor = Math.min(currentSpeed / 100.0f, 0.3f);

        float yawStep = yawDelta * smoothingFactor;
        float pitchStep = pitchDelta * smoothingFactor;

        float maxStep = currentSpeed;
        yawStep = MathHelper.clamp(yawStep, -maxStep, maxStep);
        pitchStep = MathHelper.clamp(pitchStep, -maxStep * 0.8f, maxStep * 0.8f);

        float newYaw = current.yaw() + yawStep;
        float newPitch = current.pitch() + pitchStep;

        return new ViewDirection(newYaw, newPitch).clamp();
    }
}