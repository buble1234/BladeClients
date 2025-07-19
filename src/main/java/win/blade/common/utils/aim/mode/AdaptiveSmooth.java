package win.blade.common.utils.aim.mode;

import net.minecraft.util.math.MathHelper;
import win.blade.common.utils.aim.core.SmoothTransition;
import win.blade.common.utils.aim.core.ViewDirection;
import win.blade.common.utils.math.animation.Animation;
import win.blade.common.utils.math.animation.Easing;

import static win.blade.common.utils.minecraft.MinecraftInstance.mc;

/**
 * Автор: NoCap
 * Дата создания: 18.06.2025
 */

public class AdaptiveSmooth implements SmoothTransition {

    private float baseSpeed;
    private float acceleration;
    private Easing easing;
    private float threshold;
    private boolean useMouseSens;
    private int spinInterval;
    private boolean isAttacking;

    private long lastSpin = 0;
    private boolean inSpin = false;
    private Animation spinAnim = new Animation();
    private float lastSpinValue = 0;

    public AdaptiveSmooth(float baseSpeed, float acceleration) {
        this.baseSpeed = baseSpeed;
        this.acceleration = acceleration;
        this.easing = Easing.LINEAR;
        this.threshold = 0.1f;
        this.useMouseSens = true;
        this.spinInterval = 0;
    }

    public void setBaseSpeed(float baseSpeed) {
        this.baseSpeed = baseSpeed;
    }

    public void setAcceleration(float acceleration) {
        this.acceleration = acceleration;
    }

    public void setEasing(Easing easing) {
        this.easing = easing;
    }

    public void setThreshold(float threshold) {
        this.threshold = threshold;
    }

    public void setUseMouseSens(boolean useMouseSens) {
        this.useMouseSens = useMouseSens;
    }

    public void setSpinInterval(int spinInterval) {
        this.spinInterval = spinInterval;
    }

    public void setIsAttacking(boolean isAttacking) {
        this.isAttacking = isAttacking;
    }

    @Override
    public ViewDirection interpolate(ViewDirection current, ViewDirection target) {
        long now = System.currentTimeMillis();

        float yaw = current.yaw();
        float pitch = current.pitch();
        float targetYaw = target.yaw();
        float targetPitch = target.pitch();

        float deltaYaw = MathHelper.wrapDegrees(targetYaw - yaw);
        float deltaPitch = targetPitch - pitch;

        float absDeltaYaw = Math.abs(deltaYaw);
        float absDeltaPitch = Math.abs(deltaPitch);

        if (absDeltaYaw < threshold && absDeltaPitch < threshold) {
            return new ViewDirection(MathHelper.wrapDegrees(targetYaw), MathHelper.clamp(targetPitch, -90.0f, 90.0f));
        }

        float factor = baseSpeed / 100f;
        if (isAttacking) {
            factor *= acceleration;
        }

        if (useMouseSens) {
            double sens = mc.options.getMouseSensitivity().getValue();
            factor *= (float) (sens * 2);
        }

        float yawProgress = 1 - (absDeltaYaw / 180f);
        float pitchProgress = 1 - (absDeltaPitch / 90f);

        float yawFactor = factor * (float) easing.ease((double) yawProgress);
        float pitchFactor = factor * (float) easing.ease((double) pitchProgress);

        float newYaw = yaw + deltaYaw * yawFactor;
        float newPitch = pitch + deltaPitch * pitchFactor;

        // 360 maneuver
        if (spinInterval > 0 && now - lastSpin >= spinInterval * 1000L) {
            inSpin = true;
            int spinDir = Math.random() < 0.5 ? 1 : -1;
            spinAnim.run(360 * spinDir, 0.5, Easing.LINEAR);
            spinAnim.set(0);
            lastSpinValue = 0;
            lastSpin = now;
        }

        if (inSpin) {
            spinAnim.update();
            float currentSpin = spinAnim.get();
            float extra = currentSpin - lastSpinValue;
            newYaw += extra;
            lastSpinValue = currentSpin;
            if (spinAnim.isFinished()) {
                inSpin = false;
            }
        }

        return new ViewDirection(MathHelper.wrapDegrees(newYaw), MathHelper.clamp(newPitch, -90.0f, 90.0f));
    }
}