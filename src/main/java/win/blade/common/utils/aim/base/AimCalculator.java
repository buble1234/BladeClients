package win.blade.common.utils.aim.base;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import win.blade.common.utils.aim.core.MultiViewDirection;
import win.blade.common.utils.aim.manager.AimManager;
import win.blade.common.utils.aim.core.ViewDirection;
import win.blade.common.utils.minecraft.MinecraftInstance;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Автор: NoCap
 * Дата создания: 18.06.2025
 */

public class AimCalculator implements MinecraftInstance {

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

    public static MultiViewDirection calculateMultiTarget(List<Entity> entities, MultiViewDirection.Strategy strategy) {
        MultiViewDirection multiView = new MultiViewDirection(strategy);

        for (int i = 0; i < entities.size(); i++) {
            Entity entity = entities.get(i);
            ViewDirection direction = calculateToEntity(entity);

            var player = mc.player;
            if (player != null) {
                double distance = player.distanceTo(entity);
                float priority = (float) (100.0 / Math.max(distance, 1.0));

                multiView.addPoint(direction, priority, 0);
            }
        }

        return multiView;
    }

    public static MultiViewDirection calculateMultiPosition(List<Vec3d> positions, MultiViewDirection.Strategy strategy) {
        var player = mc.player;
        if (player == null) return new MultiViewDirection(strategy);

        Vec3d playerPos = player.getCameraPosVec(1.0f);
        MultiViewDirection multiView = new MultiViewDirection(strategy);

        for (Vec3d position : positions) {
            ViewDirection direction = calculateToPosition(playerPos, position);
            multiView.addPoint(direction, 1.0f, 0); // Одинаковый приоритет для всех
        }

        return multiView;
    }

    public static MultiViewDirection createScanPattern(Vec3d center, double radius, int points, MultiViewDirection.Strategy strategy) {
        var player = mc.player;
        if (player == null) return new MultiViewDirection(strategy);

        Vec3d playerPos = player.getCameraPosVec(1.0f);
        MultiViewDirection multiView = new MultiViewDirection(strategy);

        for (int i = 0; i < points; i++) {
            double angle = (2 * Math.PI * i) / points;
            double x = center.x + radius * Math.cos(angle);
            double z = center.z + radius * Math.sin(angle);

            Vec3d scanPoint = new Vec3d(x, center.y, z);
            ViewDirection direction = calculateToPosition(playerPos, scanPoint);

            float priority = (float) (points - i);
            multiView.addPoint(direction, priority, 5000);
        }

        return multiView;
    }

    public static MultiViewDirection calculatePredictiveMultiTarget(List<Entity> entities, float velocity, MultiViewDirection.Strategy strategy) {
        MultiViewDirection multiView = new MultiViewDirection(strategy);

        for (Entity entity : entities) {
            ViewDirection predictive = calculatePredictiveAim(entity, velocity);
            ViewDirection current = calculateToEntity(entity);

            var player = mc.player;
            if (player != null) {
                double distance = player.distanceTo(entity);
                float priority = (float) (100.0 / Math.max(distance, 1.0));

                multiView.addPoint(current, priority, 2000);
                multiView.addPoint(predictive, priority * 1.5f, 3000);
            }
        }

        return multiView;
    }

    public static MultiViewDirection createAreaControl(Vec3d[] corners, long scanDuration, MultiViewDirection.Strategy strategy) {
        var player = mc.player;
        if (player == null) return new MultiViewDirection(strategy);

        Vec3d playerPos = player.getCameraPosVec(1.0f);
        MultiViewDirection multiView = new MultiViewDirection(strategy);

        for (int i = 0; i < corners.length; i++) {
            ViewDirection direction = calculateToPosition(playerPos, corners[i]);
            float priority = corners.length - i;

            multiView.addPoint(direction, priority, scanDuration);
        }

        return multiView;
    }

    public static MultiViewDirection createQuickSwitch(Entity primary, Entity secondary, long switchInterval) {
        MultiViewDirection multiView = new MultiViewDirection(MultiViewDirection.Strategy.SEQUENTIAL);

        ViewDirection primaryDir = calculateToEntity(primary);
        ViewDirection secondaryDir = calculateToEntity(secondary);

        multiView.addPoint(primaryDir, 2.0f, switchInterval);
        multiView.addPoint(secondaryDir, 1.0f, switchInterval);

        return multiView;
    }

    public static Map<Integer, Boolean> checkMultiLineOfSight(MultiViewDirection multiView, double range) {
        Map<Integer, Boolean> visibilityMap = new HashMap<>();
        List<MultiViewDirection.ViewPoint> points = multiView.getActivePoints();

        for (int i = 0; i < points.size(); i++) {
            MultiViewDirection.ViewPoint point = points.get(i);

            var manager = AimManager.INSTANCE;
            ViewDirection currentDir = manager.getCurrentDirection();

            boolean hasLOS = ViewTracer.traceClosest(range, point.direction()) != null;
            visibilityMap.put(i, hasLOS);
        }

        return visibilityMap;
    }
}