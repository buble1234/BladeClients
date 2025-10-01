package win.blade.common.utils.aim.point.mode;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import win.blade.common.utils.aim.point.PointProvider;
import win.blade.common.utils.math.BoxUtils;

import java.util.ArrayList;
import java.util.List;

import static win.blade.common.utils.minecraft.MinecraftInstance.mc;

/**
 * Автор: NoCap
 * Дата создания: 01.10.2025
 */
public class FeetPointProvider implements PointProvider {

    @Override
    public Vec3d getPoint(Entity entity) {
        if (!(entity instanceof LivingEntity livingEntity)) {
            return entity.getBoundingBox().getCenter();
        }

        Vec3d eyePos = mc.player.getCameraPosVec(1.0f);
        Box originalBox = entity.getBoundingBox();
        Box safeBox = BoxUtils.getSafeHitbox(originalBox);

        double lowestY = safeBox.minY;
        Box feetBox = safeBox.withMinY(lowestY).withMaxY(lowestY + 0.4);
        feetBox = feetBox.contract(SHRINK_FACTOR, 0.0, SHRINK_FACTOR);

        Vec3d targetVelocity = livingEntity.getPos().subtract(livingEntity.getPos());
        feetBox = feetBox.offset(targetVelocity.multiply(0.4));

        Vec3d bestPoint = findBestVisiblePoint(feetBox, eyePos);
        return bestPoint != null ? bestPoint : feetBox.getCenter();
    }

    private Vec3d findBestVisiblePoint(Box box, Vec3d eyePos) {
        List<Vec3d> points = generatePointsInBox(box);
        for (Vec3d point : points) {
            if (BoxUtils.isPointInBox(point, box) && BoxUtils.isPointVisible(eyePos, point)) {
                return point;
            }
        }
        return null;
    }

    private List<Vec3d> generatePointsInBox(Box box) {
        List<Vec3d> points = new ArrayList<>();
        double stepX = (box.maxX - box.minX) / 4.0;
        double stepY = (box.maxY - box.minY) / 4.0;
        double stepZ = (box.maxZ - box.minZ) / 4.0;

        for (double x = box.minX + stepX; x < box.maxX; x += stepX) {
            for (double y = box.minY + stepY; y < box.maxY; y += stepY) {
                for (double z = box.minZ + stepZ; z < box.maxZ; z += stepZ) {
                    points.add(new Vec3d(x, y, z));
                }
            }
        }
        return points;
    }
}