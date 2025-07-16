package win.blade.common.utils.aim.mode;

import net.minecraft.util.math.MathHelper;
import win.blade.common.utils.aim.core.SmoothTransition;
import win.blade.common.utils.aim.core.ViewDirection;
import win.blade.common.utils.math.animation.Animation;
import win.blade.common.utils.math.animation.Easing;

import java.util.concurrent.ThreadLocalRandom;

import static win.blade.common.utils.minecraft.MinecraftInstance.mc;

/**
 * Автор: NoCap
 * Дата создания: 18.06.2025
 */

public record AdaptiveSmooth(float baseSpeed, float acceleration) implements SmoothTransition {

    private static Animation animation = new Animation();

    private static float animTarget = 25f;

    private static long lastAttackTime;

    private static boolean canAttack;

    private static float yawVel = 0f;

    private static float pitchVel = 0f;

    public AdaptiveSmooth(float baseSpeed, float acceleration) {
        this.baseSpeed = baseSpeed;
        this.acceleration = acceleration;
        animation.run(animTarget, 0.2, Easing.EASE_IN_BACK);
    }

    public void setLastAttackTime(long time) {
        this.lastAttackTime = time;
    }

    public void setCanAttack(boolean canAttack) {
        this.canAttack = canAttack;
    }

    @Override
    public ViewDirection interpolate(ViewDirection current, ViewDirection target) {
        if (animation.isFinished()) {
            animTarget = -animTarget;
            animation.run(animTarget, 0.2, Easing.EASE_IN_BACK);
        }

        animation.update();

        float animValue = animation.get();

        boolean shouldRotate = (System.currentTimeMillis() - lastAttackTime > 200 || mc.player.hurtTime > 0);
        if (!shouldRotate) {
            yawVel *= 0.8f;
            pitchVel *= 0.8f;
            float finalYaw = current.yaw() + yawVel;
            float finalPitch = current.pitch() + pitchVel;
            finalPitch = MathHelper.clamp(finalPitch, -90.0f, 90.0f);
            return new ViewDirection(finalYaw, finalPitch);
        }

        float targetYaw = MathHelper.wrapDegrees(target.yaw());
        float targetPitch = MathHelper.clamp(target.pitch(), -90.0f, 90.0f);

        float currentYaw = current.yaw();
        float currentPitch = current.pitch();

        float deltaYaw = MathHelper.wrapDegrees(targetYaw - currentYaw);
        float deltaPitch = targetPitch - currentPitch;

        float maxYawSpeed = ThreadLocalRandom.current().nextFloat(baseSpeed * 8f, baseSpeed * 20f);
        float maxPitchSpeed = ThreadLocalRandom.current().nextFloat(acceleration * 5f, acceleration * 7f);

        float factor = canAttack ? 0.1f : 1f;

        float tempYawStep = MathHelper.clamp(deltaYaw, -maxYawSpeed, maxYawSpeed) + (animValue * factor);
        float tempPitchStep = MathHelper.clamp(deltaPitch, -maxPitchSpeed, maxPitchSpeed);

        yawVel = yawVel * 0.7f + tempYawStep * 0.3f;
        pitchVel = pitchVel * 0.7f + tempPitchStep * 0.3f;

        yawVel = MathHelper.clamp(yawVel, -120.0f, 120.0f);
        pitchVel = MathHelper.clamp(pitchVel, -30.0f, 30.0f);

        yawVel += ThreadLocalRandom.current().nextFloat(-3.0f, 3.0f);
        pitchVel += ThreadLocalRandom.current().nextFloat(-1.5f, 1.5f);

        double sens = mc.options.getMouseSensitivity().getValue();
        float clampedYawStep = (float) (yawVel * sens);
        float clampedPitchStep = (float) (pitchVel * sens);

        float finalYaw = currentYaw + clampedYawStep;
        float finalPitch = currentPitch + clampedPitchStep;
        finalPitch = MathHelper.clamp(finalPitch, -90.0f, 90.0f);

        return new ViewDirection(
                finalYaw,
                finalPitch
        );
    }
}