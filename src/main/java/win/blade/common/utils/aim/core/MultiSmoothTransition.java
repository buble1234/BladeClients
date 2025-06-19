package win.blade.common.utils.aim.core;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import win.blade.common.utils.minecraft.MinecraftInstance;

/**
 * Автор: NoCap
 * Дата создания: 19.06.2025
 */

public class MultiSmoothTransition implements SmoothTransition, MinecraftInstance {

    private final float baseSpeed;
    private final float acceleration;
    private final float dampening;
    private final boolean useInertia;

    private ViewDirection lastDirection;
    private ViewDirection velocity;
    private long lastUpdateTime;
    private MultiViewDirection activeMultiTarget;

    public MultiSmoothTransition(float baseSpeed, float acceleration, float dampening, boolean useInertia) {
        this.baseSpeed = baseSpeed;
        this.acceleration = acceleration;
        this.dampening = dampening;
        this.useInertia = useInertia;
        this.velocity = ViewDirection.ORIGIN;
        this.lastUpdateTime = System.currentTimeMillis();
    }

    public MultiSmoothTransition(float baseSpeed) {
        this(baseSpeed, 1.5f, 0.8f, true);
    }

    @Override
    public ViewDirection interpolate(ViewDirection current, ViewDirection target) {
        return interpolate(current, target, null, null);
    }

    @Override
    public ViewDirection interpolate(ViewDirection current, ViewDirection target, Vec3d targetPos, Entity targetEntity) {
        long currentTime = System.currentTimeMillis();
        float deltaTime = (currentTime - lastUpdateTime) / 1000.0f;
        lastUpdateTime = currentTime;

        if (lastDirection == null) {
            lastDirection = current;
        }

        ViewDirection result;

        if (activeMultiTarget != null && activeMultiTarget.hasActivePoints()) {
            result = handleMultiTarget(current, deltaTime);
        } else {
            result = handleSingleTarget(current, target, deltaTime);
        }

        lastDirection = result;
        return result;
    }

    public void setActiveMultiTarget(MultiViewDirection multiTarget) {
        this.activeMultiTarget = multiTarget;
    }

    public void clearMultiTarget() {
        this.activeMultiTarget = null;
    }

    private ViewDirection handleSingleTarget(ViewDirection current, ViewDirection target, float deltaTime) {
        ViewDirection delta = ViewDirection.difference(current, target);

        if (useInertia) {
            return applyInertiaBasedMovement(current, target, delta, deltaTime);
        } else {
            return applyDirectMovement(current, target, delta, deltaTime);
        }
    }

    private ViewDirection handleMultiTarget(ViewDirection current, float deltaTime) {
        ViewDirection nextTarget = activeMultiTarget.getCurrentDirection();

        if (nextTarget == null || nextTarget == ViewDirection.ORIGIN) {
            return current;
        }

        ViewDirection delta = ViewDirection.difference(current, nextTarget);

        float multiSpeed = baseSpeed * 1.3f;

        if (useInertia) {
            return applyInertiaBasedMovement(current, nextTarget, delta, deltaTime, multiSpeed);
        } else {
            return applyDirectMovement(current, nextTarget, delta, deltaTime, multiSpeed);
        }
    }

    private ViewDirection applyInertiaBasedMovement(ViewDirection current, ViewDirection target, ViewDirection delta, float deltaTime) {
        return applyInertiaBasedMovement(current, target, delta, deltaTime, baseSpeed);
    }

    private ViewDirection applyInertiaBasedMovement(ViewDirection current, ViewDirection target, ViewDirection delta, float deltaTime, float speed) {
        ViewDirection targetVelocity = delta.scale(speed * deltaTime);

        velocity = velocity.combine(targetVelocity.scale(acceleration * deltaTime));

        velocity = velocity.scale(dampening);

        float maxSpeed = speed * 2.0f;
        float currentSpeed = (float) Math.sqrt(velocity.yaw() * velocity.yaw() + velocity.pitch() * velocity.pitch());

        if (currentSpeed > maxSpeed) {
            velocity = velocity.scale(maxSpeed / currentSpeed);
        }

        ViewDirection result = current.combine(velocity.scale(deltaTime));

        float variance = 0.1f + (float) (Math.random() * 0.05f);
        ViewDirection randomOffset = new ViewDirection(
                (float) (Math.random() - 0.5) * variance,
                (float) (Math.random() - 0.5) * variance * 0.5f
        );

        return result.combine(randomOffset).clamp();
    }

    private ViewDirection applyDirectMovement(ViewDirection current, ViewDirection target, ViewDirection delta, float deltaTime) {
        return applyDirectMovement(current, target, delta, deltaTime, baseSpeed);
    }

    private ViewDirection applyDirectMovement(ViewDirection current, ViewDirection target, ViewDirection delta, float deltaTime, float speed) {
        double distance = current.distanceTo(target);

        float dynamicSpeed = speed;
        if (distance > 15.0) {
            dynamicSpeed *= acceleration;
        } else if (distance < 5.0) {
            dynamicSpeed *= 0.7f;
        }

        float variance = 0.15f + (float) (Math.random() * 0.1f);
        dynamicSpeed *= (1.0f + variance);

        float maxMovement = dynamicSpeed * deltaTime * 60.0f;

        float maxYawDelta = Math.min(maxMovement, Math.abs(delta.yaw()));
        float maxPitchDelta = Math.min(maxMovement * 0.8f, Math.abs(delta.pitch()));

        float newYaw = current.yaw() + Math.signum(delta.yaw()) * maxYawDelta;
        float newPitch = current.pitch() + Math.signum(delta.pitch()) * maxPitchDelta;

        return new ViewDirection(newYaw, newPitch).clamp();
    }

    public boolean isMovementComplete(ViewDirection current, ViewDirection target) {
        double distance = current.distanceTo(target);
        return distance < 1.0;
    }

    public void reset() {
        lastDirection = null;
        velocity = ViewDirection.ORIGIN;
        activeMultiTarget = null;
        lastUpdateTime = System.currentTimeMillis();
    }

    public ViewDirection getCurrentVelocity() {
        return velocity;
    }

    public boolean hasActiveMultiTarget() {
        return activeMultiTarget != null && activeMultiTarget.hasActivePoints();
    }
}