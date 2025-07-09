package win.blade.common.utils.aim.point.mode;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import win.blade.common.utils.aim.point.PointProvider;
import win.blade.common.utils.math.BoxUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;

import static win.blade.common.utils.minecraft.MinecraftInstance.mc;

/**
 * Автор: NoCap
 * Дата создания: 09.07.2025
 */
public class MultiPointProvider implements PointProvider {

    @Override
    public Vec3d getPoint(Entity entity) {
        Vec3d eyePos = mc.player.getCameraPosVec(1.0f);
        Box box = entity.getBoundingBox();

        double boxSize = Math.max(Math.max(box.getLengthX(), box.getLengthY()), box.getLengthZ());
        double step = Math.min(0.5, Math.max(0.1, boxSize / 5.0));

        List<Vec3d> points = generatePointsWithinBox(box, step);

        points.add(box.getCenter());
        points.add(new Vec3d(box.minX, box.minY, box.minZ));
        points.add(new Vec3d(box.maxX, box.minY, box.minZ));
        points.add(new Vec3d(box.minX, box.maxY, box.minZ));
        points.add(new Vec3d(box.minX, box.minY, box.maxZ));
        points.add(new Vec3d(box.maxX, box.maxY, box.minZ));
        points.add(new Vec3d(box.maxX, box.minY, box.maxZ));
        points.add(new Vec3d(box.minX, box.maxY, box.maxZ));
        points.add(new Vec3d(box.maxX, box.maxY, box.maxZ));


        List<Vec3d> visiblePoints = new ArrayList<>();
        for (Vec3d point : points) {
            if (BoxUtils.isPointVisibleFrom(eyePos, point)) {
                visiblePoints.add(point);
            }
        }

        if (!visiblePoints.isEmpty()) {
            return visiblePoints.stream()
                    .min(Comparator.comparingDouble(p -> eyePos.squaredDistanceTo(p)))
                    .orElse(box.getCenter());
        }
        return box.getCenter();
    }

    private List<Vec3d> generatePointsWithinBox(Box box, double step) {
        List<Vec3d> points = new ArrayList<>();
        int count = 0;

        for (double x = box.minX; x <= box.maxX + 0.001; x += step) {
            for (double y = box.minY; y <= box.maxY + 0.001; y += step) {
                for (double z = box.minZ; z <= box.maxZ + 0.001; z += step) {
                    points.add(new Vec3d(x, y, z));
                    count++;
                    if (count >= 200) {
                        return points;
                    }
                }
            }
        }
        return points;
    }
}