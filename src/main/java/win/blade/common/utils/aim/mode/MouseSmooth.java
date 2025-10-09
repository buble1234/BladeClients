package win.blade.common.utils.aim.mode;

import net.minecraft.util.math.MathHelper;
import win.blade.common.utils.aim.core.SmoothTransition;
import win.blade.common.utils.aim.core.ViewDirection;
import win.blade.common.utils.minecraft.MinecraftInstance;

/**
 * Автор: NoCap
 * Дата создания: 08.10.2025
 */
public class MouseSmooth implements SmoothTransition, MinecraftInstance {
    
    private float lastRealYaw;
    private float lastRealPitch;
    private boolean initialized;

    public MouseSmooth() {
        this.initialized = false;
    }

    @Override
    public ViewDirection interpolate(ViewDirection current, ViewDirection target) {
        if (mc.player == null) {
            return current;
        }

        if (!initialized) {
            lastRealYaw = mc.gameRenderer.getCamera().getYaw();
            lastRealPitch = mc.gameRenderer.getCamera().getPitch();
            initialized = true;
        }

        float currentRealYaw =mc.gameRenderer.getCamera().getYaw();
        float currentRealPitch = mc.gameRenderer.getCamera().getPitch();

        float yawDeltaToTarget = MathHelper.wrapDegrees(target.yaw() - current.yaw());
        float pitchDeltaToTarget = target.pitch() - current.pitch();

        float realYawSpeed = Math.abs(MathHelper.wrapDegrees(currentRealYaw - lastRealYaw));
        float realPitchSpeed = Math.abs(currentRealPitch - lastRealPitch);

        float maxTurnYaw = Math.max(realYawSpeed, 1.0f);
        float maxTurnPitch = Math.max(realPitchSpeed, 1.0f);

        float clampedYaw = MathHelper.clamp(yawDeltaToTarget, -maxTurnYaw, maxTurnYaw);
        float clampedPitch = MathHelper.clamp(pitchDeltaToTarget, -maxTurnPitch, maxTurnPitch);

        float newYaw = current.yaw() + clampedYaw;
        float newPitch = MathHelper.clamp(current.pitch() + clampedPitch, -90.0f, 90.0f);

        lastRealYaw = currentRealYaw;
        lastRealPitch = currentRealPitch;

        return new ViewDirection(newYaw, newPitch);
    }

    public void reset() {
        initialized = false;
    }
}