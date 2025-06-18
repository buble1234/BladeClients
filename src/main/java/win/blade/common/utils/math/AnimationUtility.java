package win.blade.common.utils.math;

/**
 * Автор: NoCap
 * Дата создания: 18.06.2025
 * Описание: Утилита для анимаций с различными Easing функциями
 */

public class AnimationUtility {
    private final long startTime;
    private final float duration;
    private final Easing easing;
    private final AnimationUpdater updater;
    private float progress;
    private boolean isFinished;

    @FunctionalInterface
    public interface AnimationUpdater {
        void update(float progress);
    }

    @FunctionalInterface
    public interface Easing {
        float apply(float t);

        Easing LINEAR = t -> t;

        Easing EASE_IN_QUAD = t -> t * t;
        Easing EASE_IN_CUBIC = t -> t * t * t;
        Easing EASE_IN_QUART = t -> t * t * t * t;
        Easing EASE_IN_QUINT = t -> t * t * t * t * t;

        Easing EASE_OUT_QUAD = t -> 1.0f - (1.0f - t) * (1.0f - t);
        Easing EASE_OUT_CUBIC = t -> 1.0f - (float) Math.pow(1.0 - t, 3);
        Easing EASE_OUT_QUART = t -> 1.0f - (float) Math.pow(1.0 - t, 4);
        Easing EASE_OUT_QUINT = t -> 1.0f - (float) Math.pow(1.0 - t, 5);

        Easing EASE_IN_OUT_QUAD = t -> t < 0.5f ? 2.0f * t * t : 1.0f - (float) Math.pow(-2.0 * t + 2.0, 2) / 2.0f;
        Easing EASE_IN_OUT_CUBIC = t -> t < 0.5f ? 4.0f * t * t * t : 1.0f - (float) Math.pow(-2.0 * t + 2.0, 3) / 2.0f;
        Easing EASE_IN_OUT_QUART = t -> t < 0.5f ? 8.0f * t * t * t * t : 1.0f - (float) Math.pow(-2.0 * t + 2.0, 4) / 2.0f;
        Easing EASE_IN_OUT_QUINT = t -> t < 0.5f ? 16.0f * t * t * t * t * t : 1.0f - (float) Math.pow(-2.0 * t + 2.0, 5) / 2.0f;
    }

    public AnimationUtility(float duration, Easing easing, AnimationUpdater updater) {
        this.startTime = System.currentTimeMillis();
        this.duration = duration;
        this.easing = easing != null ? easing : Easing.LINEAR;
        this.updater = updater;
        this.progress = 0.0f;
        this.isFinished = false;
    }

    public void update() {
        if (isFinished) return;
        progress = Math.min(1.0f, (System.currentTimeMillis() - startTime) / duration);
        float easedProgress = easing.apply(progress);
        if (updater != null) {
            updater.update(easedProgress);
        }
        if (progress >= 1.0f) {
            isFinished = true;
        }
    }

    public float getProgress() {
        return progress;
    }

    public boolean isFinished() {
        return isFinished;
    }
}