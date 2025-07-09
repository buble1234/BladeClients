package win.blade.common.utils.aim.point;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import win.blade.common.utils.aim.point.mode.*;

import static win.blade.common.utils.minecraft.MinecraftInstance.mc;

/**
 * Автор: NoCap
 * Дата создания: 05.07.2025
 */
public class PointCalculator {
    private static final PointProvider centerProvider = new CenterPointProvider();
    private static final PointProvider smartProvider = new SmartPointProvider();
    private static final PointProvider multiProvider = new MultiPointProvider();

    public static Vec3d getPoint(Entity entity, PointMode mode) {
        if (mc.player == null) return entity.getBoundingBox().getCenter();

        return switch (mode) {
            case CENTER -> centerProvider.getPoint(entity);
            case SMART -> smartProvider.getPoint(entity);
            case MULTI -> multiProvider.getPoint(entity);
        };
    }
}