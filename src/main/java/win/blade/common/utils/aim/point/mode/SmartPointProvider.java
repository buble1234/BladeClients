package win.blade.common.utils.aim.point.mode;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import win.blade.common.utils.aim.point.PointProvider;
import win.blade.common.utils.math.BoxUtils;

import java.util.ArrayList;
import java.util.List;

import static win.blade.common.utils.minecraft.MinecraftInstance.mc;

/**
 * Автор: NoCap
 * Дата создания: 05.07.2025
 */
public class SmartPointProvider implements PointProvider {
    private static final int SMART_STEPS = 8;
    private static final double CORNER_OFFSET = 0.1;
    private static final double EDGE_OFFSET = 0.05;

    @Override
    public Vec3d getPoint(Entity entity) {
        Vec3d eyePos = mc.player.getCameraPosVec(1.0f);
        Box originalBox = entity.getBoundingBox();
        Box safeBox = BoxUtils.getSafeHitbox(originalBox);

        List<Vec3d> points = generatePointsInBox(safeBox);
        sortPointsByDistanceToCenter(points, originalBox);

        Vec3d bestPoint = findBestVisiblePoint(points, originalBox, eyePos);
        return bestPoint != null ? bestPoint : originalBox.getCenter();
    }

    private List<Vec3d> generatePointsInBox(Box box) {
        List<Vec3d> points = new ArrayList<>();

        points.add(box.getCenter());
        
        addSurfacePoints(points, box);
        
        addCornerPoints(points, box);
        
        addEdgePoints(points, box);

        return points;
    }

    private void addSurfacePoints(List<Vec3d> points, Box box) {
        double stepX = (box.maxX - box.minX) / (SMART_STEPS + 1);
        double stepY = (box.maxY - box.minY) / (SMART_STEPS + 1);
        double stepZ = (box.maxZ - box.minZ) / (SMART_STEPS + 1);

        for (int i = 1; i <= SMART_STEPS; i++) {
            for (int j = 1; j <= SMART_STEPS; j++) {
                for (int k = 1; k <= SMART_STEPS; k++) {
                    boolean isOnSurface = (i == 1 || i == SMART_STEPS ||
                            j == 1 || j == SMART_STEPS ||
                            k == 1 || k == SMART_STEPS);

                    if (isOnSurface) {
                        double x = box.minX + stepX * i;
                        double y = box.minY + stepY * j;
                        double z = box.minZ + stepZ * k;

                        points.add(new Vec3d(x, y, z));
                    }
                }
            }
        }
    }

    private void addCornerPoints(List<Vec3d> points, Box box) {
        double[] xCoords = {box.minX + CORNER_OFFSET, box.maxX - CORNER_OFFSET};
        double[] yCoords = {box.minY + CORNER_OFFSET, box.maxY - CORNER_OFFSET};
        double[] zCoords = {box.minZ + CORNER_OFFSET, box.maxZ - CORNER_OFFSET};

        for (double x : xCoords) {
            for (double y : yCoords) {
                for (double z : zCoords) {
                    points.add(new Vec3d(x, y, z));
                }
            }
        }
    }

    private void addEdgePoints(List<Vec3d> points, Box box) {
        Vec3d center = box.getCenter();

        points.add(new Vec3d(center.x, box.minY + EDGE_OFFSET, center.z));
        points.add(new Vec3d(center.x, box.maxY - EDGE_OFFSET, center.z));
        points.add(new Vec3d(box.minX + EDGE_OFFSET, center.y, center.z));
        points.add(new Vec3d(box.maxX - EDGE_OFFSET, center.y, center.z));
        points.add(new Vec3d(center.x, center.y, box.minZ + EDGE_OFFSET));
        points.add(new Vec3d(center.x, center.y, box.maxZ - EDGE_OFFSET));
    }

    private void sortPointsByDistanceToCenter(List<Vec3d> points, Box originalBox) {
        Vec3d center = originalBox.getCenter();
        points.sort((p1, p2) -> {
            double dist1 = p1.distanceTo(center);
            double dist2 = p2.distanceTo(center);
            return Double.compare(dist1, dist2);
        });
    }

    private Vec3d findBestVisiblePoint(List<Vec3d> points, Box originalBox, Vec3d eyePos) {
        for (Vec3d point : points) {
            if (BoxUtils.isPointInBox(point, originalBox)) {
                if (BoxUtils.isPointVisible(eyePos, point)) {
                    return point;
                }
            }
        }
        return null;
    }
}