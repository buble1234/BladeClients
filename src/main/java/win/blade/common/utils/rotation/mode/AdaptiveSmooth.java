package win.blade.common.utils.rotation.mode;

import win.blade.common.utils.rotation.core.SmoothTransition;
import win.blade.common.utils.rotation.core.ViewDirection;

public record AdaptiveSmooth(float baseSpeed, float acceleration) implements SmoothTransition {

    public AdaptiveSmooth(float speed) {
        this(speed, 1.5f);
    }

    @Override
    public ViewDirection interpolate(ViewDirection current, ViewDirection target) {
        ViewDirection delta = ViewDirection.difference(current, target);
        
        float variance = 0.15f + (float)(Math.random() * 0.1f);
        float dynamicSpeed = baseSpeed * (1.0f + variance);
        
        double distance = current.distanceTo(target);
        float speedMultiplier = distance > 15.0 ? acceleration : 1.0f;
        float finalSpeed = dynamicSpeed * speedMultiplier;
        
        float maxYawDelta = Math.min(finalSpeed, Math.abs(delta.yaw()));
        float maxPitchDelta = Math.min(finalSpeed * 0.8f, Math.abs(delta.pitch()));
        
        float newYaw = current.yaw() + Math.signum(delta.yaw()) * maxYawDelta;
        float newPitch = current.pitch() + Math.signum(delta.pitch()) * maxPitchDelta;
        
        return new ViewDirection(newYaw, newPitch).clamp();
    }
}