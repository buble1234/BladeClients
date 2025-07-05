package win.blade.common.utils.aim.point;

import net.minecraft.util.math.Vec3d;

/**
 * Автор: NoCap
 * Дата создания: 05.07.2025
 */
public class Point {
    private final Vec3d position;

    public Point(Vec3d position) {
        this.position = position;
    }

    public Vec3d getPosition() {
        return position;
    }
}