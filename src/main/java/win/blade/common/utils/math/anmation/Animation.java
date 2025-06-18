package win.blade.common.utils.math.anmation;


import win.blade.common.utils.math.anmation.Easing;

import java.util.function.Consumer;

public class Animation {
    private long start;
    private double duration;
    private double fromValue;
    private double toValue;
    private double value;
    private Easing easing = Easing.LINEAR;
    private Consumer<Animation> finishAction;

    public void run(double valueTo, double duration, Easing easing, boolean safe) {
        if (safe && isAnimating() && (valueTo == this.toValue)) {
            return;
        }
        this.easing = easing;
        this.duration = duration * 1000.0D;
        this.start = System.currentTimeMillis();
        this.fromValue = this.value;
        this.toValue = valueTo;
    }

    public void run(double valueTo, double duration, Easing easing) {
        run(valueTo, duration, easing, false);
    }

    public void run(double valueTo, double duration) {
        run(valueTo, duration, Easing.LINEAR, false);
    }

    public void update() {
        if (isFinished()) {
            this.value = this.toValue;
            if (this.finishAction != null) {
                this.finishAction.accept(this);
                this.finishAction = null;
            }
            return;
        }
        double part = (double) (System.currentTimeMillis() - this.start) / this.duration;
        this.value = fromValue + (toValue - fromValue) * this.easing.ease(part);
    }

    public boolean isAnimating() {
        return !isFinished();
    }

    public boolean isFinished() {
        return (System.currentTimeMillis() - this.start) >= this.duration;
    }

    public float get() {
        return (float) this.value;
    }

    public void set(double value) {
        this.value = value;
    }

    public long getStart() {
        return start;
    }

    public double getToValue() {
        return this.toValue;
    }

    public void onFinished(Consumer<Animation> action) {
        this.finishAction = action;
    }
}