package win.blade.common.utils.aim.core;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import win.blade.common.utils.aim.base.AimCalculator;

import java.util.*;

import static win.blade.common.utils.minecraft.MinecraftInstance.mc;

/**
 * Автор: NoCap
 * Дата создания: 19.06.2025
 */
public class MultiViewDirection {

    private final List<ViewPoint> viewPoints;
    private int currentIndex;
    private final Strategy strategy;

    public MultiViewDirection(Strategy strategy) {
        this.viewPoints = new ArrayList<>();
        this.currentIndex = 0;
        this.strategy = strategy;
    }

    public MultiViewDirection(List<ViewPoint> points, Strategy strategy) {
        this.viewPoints = new ArrayList<>(points);
        this.currentIndex = 0;
        this.strategy = strategy;
    }

    public MultiViewDirection addPoint(ViewDirection direction, float priority, long duration) {
        viewPoints.add(new ViewPoint(direction, priority, duration, System.currentTimeMillis()));
        return this;
    }

    public MultiViewDirection addPoint(Vec3d position, float priority, long duration) {
        var player = mc.player;
        if (player != null) {
            ViewDirection direction = AimCalculator.calculateToPosition(
                    player.getCameraPosVec(1.0f), position
            );
            addPoint(direction, priority, duration);
        }
        return this;
    }

    public MultiViewDirection addPoint(Entity entity, float priority, long duration) {
        ViewDirection direction = AimCalculator.calculateToEntity(entity);
        addPoint(direction, priority, duration);
        return this;
    }

    public ViewDirection getCurrentDirection() {
        if (viewPoints.isEmpty()) return ViewDirection.ORIGIN;

        cleanupExpiredPoints();

        if (viewPoints.isEmpty()) return ViewDirection.ORIGIN;

        return switch (strategy) {
            case RANDOM -> getRandomDirection();
            case SEQUENTIAL -> getSequentialDirection();
            case CLOSEST -> getClosestDirection();
            case PRIORITY -> getPriorityDirection();
        };
    }

    public ViewDirection getNextDirection() {
        if (viewPoints.isEmpty()) return ViewDirection.ORIGIN;

        currentIndex = (currentIndex + 1) % viewPoints.size();
        return viewPoints.get(currentIndex).direction();
    }

    public List<ViewPoint> getActivePoints() {
        cleanupExpiredPoints();
        return new ArrayList<>(viewPoints);
    }

    public int getPointCount() {
        cleanupExpiredPoints();
        return viewPoints.size();
    }

    public void clear() {
        viewPoints.clear();
        currentIndex = 0;
    }

    public boolean hasActivePoints() {
        cleanupExpiredPoints();
        return !viewPoints.isEmpty();
    }

    public boolean removePoint(int index) {
        if (index >= 0 && index < viewPoints.size()) {
            viewPoints.remove(index);
            if (currentIndex >= viewPoints.size()) {
                currentIndex = Math.max(0, viewPoints.size() - 1);
            }
            return true;
        }
        return false;
    }

    private ViewDirection getSequentialDirection() {
        if (currentIndex >= viewPoints.size()) {
            currentIndex = 0;
        }
        return viewPoints.get(currentIndex).direction();
    }

    private ViewDirection getPriorityDirection() {
        return viewPoints.stream()
                .max(Comparator.comparing(ViewPoint::priority))
                .map(ViewPoint::direction)
                .orElse(ViewDirection.ORIGIN);
    }

    private ViewDirection getClosestDirection() {
        var player = mc.player;
        if (player == null) return ViewDirection.ORIGIN;

        ViewDirection currentPlayerDir = new ViewDirection(player.getYaw(), player.getPitch());

        return viewPoints.stream()
                .min(Comparator.comparing(point ->
                        currentPlayerDir.distanceTo(point.direction())))
                .map(ViewPoint::direction)
                .orElse(ViewDirection.ORIGIN);
    }

    private ViewDirection getRandomDirection() {
        if (viewPoints.isEmpty()) return ViewDirection.ORIGIN;

        Random random = new Random();
        return viewPoints.get(random.nextInt(viewPoints.size())).direction();
    }

    private void cleanupExpiredPoints() {
        long currentTime = System.currentTimeMillis();

        viewPoints.removeIf(point -> {
            long elapsedTime = currentTime - point.createTime();
            return point.duration() > 0 && elapsedTime > point.duration();
        });

        if (currentIndex >= viewPoints.size()) {
            currentIndex = Math.max(0, viewPoints.size() - 1);
        }
    }

    public record ViewPoint(
            ViewDirection direction,
            float priority,
            long duration,
            long createTime
    ) {}

    public enum Strategy {
        RANDOM,
        SEQUENTIAL,
        CLOSEST,
        PRIORITY
    }
}