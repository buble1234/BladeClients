package win.blade.common.utils.aim.base;

import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.MinecraftClient;
import win.blade.common.utils.aim.core.ViewDirection;

import java.util.function.Predicate;

/**
 * Автор: NoCap
 * Дата создания: 18.06.2025
 */

public class ViewTracer {

    public static EntityHitResult traceEntity(double range, ViewDirection direction, Predicate<Entity> filter) {
        var mc = MinecraftClient.getInstance();
        Entity camera = mc.cameraEntity;
        if (camera == null) return null;

        Vec3d start = camera.getCameraPosVec(1.0f);
        Vec3d directionVec = direction.asVector();
        Vec3d end = start.add(directionVec.multiply(range));

        Box searchArea = camera.getBoundingBox()
                .stretch(directionVec.multiply(range))
                .expand(1.0);

        return ProjectileUtil.raycast(
                camera, start, end, searchArea,
                entity -> !entity.isSpectator() && entity.isAlive() && filter.test(entity),
                range * range
        );
    }

    public static EntityHitResult traceClosest(double range, ViewDirection direction) {
        return traceEntity(range, direction, entity -> true);
    }

    public static boolean canReach(Entity target, double range, ViewDirection direction) {
        EntityHitResult result = traceEntity(range, direction, entity -> entity == target);
        return result != null && result.getEntity() == target;
    }
}