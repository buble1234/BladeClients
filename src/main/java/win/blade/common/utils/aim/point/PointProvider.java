package win.blade.common.utils.aim.point;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

/**
 * Автор: NoCap
 * Дата создания: 05.07.2025
 */
public interface PointProvider {
    Vec3d getPoint(Entity entity);
    double SHRINK_FACTOR = 0.05;
}