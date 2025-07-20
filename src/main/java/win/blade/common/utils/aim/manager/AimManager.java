package win.blade.common.utils.aim.manager;

import win.blade.common.utils.aim.core.AimSettings;
import win.blade.common.utils.aim.core.ViewDirection;
import win.blade.common.utils.aim.mode.AdaptiveSmooth;

import static win.blade.common.utils.minecraft.MinecraftInstance.mc;

/**
 * Автор: NoCap
 * Дата создания: 18.06.2025
 */

public class AimManager {
    public static final AimManager INSTANCE = new AimManager();

    private ViewDirection currentDirection;
    private ViewDirection previousDirection;
    private ViewDirection serverDirection;
    private TargetTask activeTask;
    private boolean isActive = false;
    private long lastTickTime = 0;
    private int tickCounter = 0;

    private AimManager() {}

    public void tick() {
        long currentTime = System.currentTimeMillis();

        if (currentTime - lastTickTime < mc.getRenderTickCounter().getTickDelta(false)) return;
        lastTickTime = currentTime;
        tickCounter++;

        if (!isActive || activeTask == null) {
            disable();
            return;
        }

        previousDirection = currentDirection;
        currentDirection = activeTask.calculateNext(
                currentDirection != null ? currentDirection : getPlayerDirection()
        );

        if (activeTask.settings().enableViewSync()) {
            serverDirection = currentDirection;
        }

        if (activeTask.isCompleted(currentDirection)) {
            disable();
        }
    }

    public void execute(TargetTask task) {
        this.activeTask = task;
        this.isActive = true;
        this.tickCounter = 0;

        if (currentDirection == null) {
            currentDirection = getPlayerDirection();
            previousDirection = currentDirection;
        }
    }

    public void disable() {
        activeTask = null;
        isActive = false;
        currentDirection = null;
        previousDirection = null;
        serverDirection = null;
        tickCounter = 0;
    }

    public void disableWithSmooth() {
        if (!isActive || activeTask == null) {
            disable();
            return;
        }

        ViewDirection direction = getPlayerDirection();
        TargetTask disableTask = new TargetTask(direction, null, null, activeTask.settings());
        execute(disableTask);
    }

    public void forceRotation(ViewDirection direction) {
        this.currentDirection = direction;
        this.previousDirection = direction;
        this.serverDirection = direction;
        this.isActive = false;
    }

    private ViewDirection getPlayerDirection() {
        var player = mc.player;
        return player != null ? new ViewDirection(player.getYaw(), player.getPitch()) : ViewDirection.ORIGIN;
    }

    public ViewDirection getCurrentDirection() {
        return isActive ? currentDirection : null;
    }

    public ViewDirection getPreviousDirection() {
        return isActive ? previousDirection : null;
    }

    public ViewDirection getServerDirection() {
        if (!isActive) return null;
        return serverDirection != null ? serverDirection : getPlayerDirection();
    }

    public TargetTask getActiveTask() {
        return isActive ? activeTask : null;
    }

    public boolean isEnabled() {
        return isActive && activeTask != null;
    }

    public int getTickCount() {
        return tickCounter;
    }

    public boolean shouldInterpolate() {
        return isActive && currentDirection != null && previousDirection != null;
    }
}