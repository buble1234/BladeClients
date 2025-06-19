package win.blade.common.utils.aim.base;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import win.blade.common.utils.aim.manager.AimManager;
import win.blade.common.utils.aim.core.ViewDirection;

import static win.blade.common.utils.minecraft.MinecraftInstance.mc;

/**
 * Автор: NoCap
 * Дата создания: 18.06.2025
 */

public class AimCalculator {

    public static ViewDirection calculateToEntity(Entity entity) {
        var player = mc.player;
        if (player == null) return ViewDirection.ORIGIN;

        Vec3d playerPos = player.getCameraPosVec(1.0f);
        Vec3d entityCenter = entity.getBoundingBox().getCenter();

        return calculateToPosition(playerPos, entityCenter);
    }

    public static ViewDirection calculateToPosition(Vec3d from, Vec3d to) {
        Vec3d direction = to.subtract(from);

        double horizontalDistance = Math.sqrt(direction.x * direction.x + direction.z * direction.z);
        float yaw = (float) Math.toDegrees(Math.atan2(-direction.x, direction.z));
        float pitch = (float) Math.toDegrees(Math.atan2(-direction.y, horizontalDistance));

        return new ViewDirection(yaw, pitch);
    }

    public static ViewDirection calculatePredictiveAim(Entity entity, float velocity) {
        var player = mc.player;
        if (player == null) return ViewDirection.ORIGIN;

        Vec3d playerPos = player.getCameraPosVec(1.0f);
        Vec3d entityPos = entity.getBoundingBox().getCenter();
        Vec3d entityVelocity = entity.getVelocity();

        double distance = playerPos.distanceTo(entityPos);
        double timeToHit = distance / velocity;

        Vec3d predictedPos = entityPos.add(entityVelocity.multiply(timeToHit));
        return calculateToPosition(playerPos, predictedPos);
    }

    public static boolean hasLineOfSight(Entity entity, double range) {
        var manager = AimManager.INSTANCE;
        ViewDirection direction = manager.getCurrentDirection();

        if (direction == null) return false;

        return ViewTracer.traceEntity(range, direction, e -> e == entity) != null;
    }

    public static double getAngleDifference(ViewDirection from, ViewDirection to) {
        return from.distanceTo(to);
    }
}