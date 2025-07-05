package win.blade.common.utils.aim.point.mode;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import win.blade.common.utils.aim.point.PointProvider;

/**
 * Автор: NoCap
 * Дата создания: 05.07.2025
 */
public class CenterPointProvider implements PointProvider {
    @Override
    public Vec3d getPoint(Entity entity) {
        return entity.getBoundingBox().getCenter();
    }
}