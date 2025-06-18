package win.blade.common.utils.rotation.core;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public record ViewDirection(float yaw, float pitch) {
    public static final ViewDirection ORIGIN = new ViewDirection(0, 0);

    public ViewDirection clamp() {
        return new ViewDirection(
                MathHelper.wrapDegrees(yaw),
                MathHelper.clamp(pitch, -90f, 90f)
        );
    }

    public Vec3d asVector() {
        float pitchRad = pitch * 0.017453292F;
        float yawRad = -yaw * 0.017453292F;
        float cosYaw = MathHelper.cos(yawRad);
        float sinYaw = MathHelper.sin(yawRad);
        float cosPitch = MathHelper.cos(pitchRad);
        float sinPitch = MathHelper.sin(pitchRad);
        return new Vec3d(sinYaw * cosPitch, -sinPitch, cosYaw * cosPitch);
    }

    public ViewDirection combine(ViewDirection other) {
        return new ViewDirection(yaw + other.yaw, pitch + other.pitch);
    }

    public ViewDirection subtract(ViewDirection other) {
        return new ViewDirection(yaw - other.yaw, pitch - other.pitch);
    }

    public ViewDirection scale(float multiplier) {
        return new ViewDirection(yaw * multiplier, pitch * multiplier);
    }

    public static ViewDirection difference(ViewDirection start, ViewDirection end) {
        return new ViewDirection(
                MathHelper.wrapDegrees(end.yaw - start.yaw),
                MathHelper.wrapDegrees(end.pitch - start.pitch)
        );
    }

    public double distanceTo(ViewDirection other) {
        ViewDirection delta = difference(this, other);
        return Math.sqrt(delta.yaw * delta.yaw + delta.pitch * delta.pitch);
    }
}