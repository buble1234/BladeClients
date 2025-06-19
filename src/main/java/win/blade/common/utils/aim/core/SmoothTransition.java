package win.blade.common.utils.aim.core;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

/**
 * Автор: NoCap
 * Дата создания: 18.06.2025
 */

public interface SmoothTransition {
    ViewDirection interpolate(ViewDirection current, ViewDirection target);

    default ViewDirection interpolate(ViewDirection current, ViewDirection target, Vec3d targetPos, Entity targetEntity) {
        return interpolate(current, target);
    }
}