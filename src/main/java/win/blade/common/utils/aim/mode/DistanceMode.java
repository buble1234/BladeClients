package win.blade.common.utils.aim.mode;

import net.minecraft.util.math.MathHelper;
import win.blade.common.utils.aim.core.SmoothTransition;
import win.blade.common.utils.aim.core.ViewDirection;

/**
 * Автор: NoCap
 * Дата создания: 29.06.2025
 */
public class DistanceMode implements SmoothTransition {

    private final float baseSpeed;

    private final float fovAcceleration;

    public DistanceMode(float baseSpeed, float fovAcceleration) {
        this.baseSpeed = MathHelper.clamp(baseSpeed, 0.1f, 1.0f);
        this.fovAcceleration = MathHelper.clamp(fovAcceleration, 1.0f, 3.0f);
    }

    public DistanceMode() {
        this(0.4f, 1.8f);
    }

    @Override
    public ViewDirection interpolate(ViewDirection current, ViewDirection target) {
        if (current == null || target == null) {
            return current != null ? current : ViewDirection.ORIGIN;
        }

        ViewDirection delta = ViewDirection.difference(current, target);
        float fovDistance = (float) delta.distanceTo(ViewDirection.ORIGIN);

        if (fovDistance < 0.05f) {
            return current;
        }

        float speedFactor = baseSpeed * (1.0f + (fovDistance / 90.0f) * (fovAcceleration - 1.0f));

        speedFactor = MathHelper.clamp(speedFactor, 0.01f, 1.0f);

        ViewDirection interpolated = ViewDirection.lerpAngles(current, target, speedFactor);

        return interpolated.clamp();
    }
}