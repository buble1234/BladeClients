package win.blade.common.utils.math;

import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

/**
 * Автор: NoCap
 * Дата создания: 05.07.2025
 */
public class BoxUtils {
    private static final double SHRINK_FACTOR = 0.95;
    private static final double MIN_SHRINK_FACTOR = 0.02;

    public static Box getSafeHitbox(Box originalBox) {
        double width = originalBox.getLengthX();
        double height = originalBox.getLengthY();
        double depth = originalBox.getLengthZ();

        double shrinkX = Math.max(width * (1.0 - SHRINK_FACTOR) / 2.0, MIN_SHRINK_FACTOR);
        double shrinkY = Math.max(height * (1.0 - SHRINK_FACTOR) / 2.0, MIN_SHRINK_FACTOR);
        double shrinkZ = Math.max(depth * (1.0 - SHRINK_FACTOR) / 2.0, MIN_SHRINK_FACTOR);

        shrinkX = Math.min(shrinkX, width * 0.4);
        shrinkY = Math.min(shrinkY, height * 0.4);
        shrinkZ = Math.min(shrinkZ, depth * 0.4);

        return new Box(
                originalBox.minX + shrinkX,
                originalBox.minY + shrinkY,
                originalBox.minZ + shrinkZ,
                originalBox.maxX - shrinkX,
                originalBox.maxY - shrinkY,
                originalBox.maxZ - shrinkZ
        );
    }

    public static boolean isPointInBox(Vec3d point, Box box) {
        return point.x >= box.minX && point.x <= box.maxX &&
                point.y >= box.minY && point.y <= box.maxY &&
                point.z >= box.minZ && point.z <= box.maxZ;
    }
}