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
        return new ViewDirection(
                MathHelper.wrapDegrees(target.yaw()),
                MathHelper.clamp(target.pitch(), -90.0f, 90.0f)
        );
    }
}