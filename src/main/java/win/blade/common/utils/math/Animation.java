package win.blade.common.utils.math;

public class Animation {
    private Easing easing;
    private long duration;
    private long millis;
    private long startTime;
    private double startValue;
    private double destinationValue;
    private double value;
    private boolean finished;
    private Direction direction;

    public Animation(final Easing easing, final long duration) {
        this.easing = easing;
        this.startTime = System.currentTimeMillis();
        this.duration = duration;
    }

    /**
     * Updates the animation by using the easing function and time
     *
     * @param destinationValue the value that the animation is going to reach
     */
    public void run(final double destinationValue) {
        this.millis = System.currentTimeMillis();
        if (this.destinationValue != destinationValue) {
            this.destinationValue = destinationValue;
            this.reset();
        } else {
            this.finished = this.millis - this.duration > this.startTime;
            if (this.finished) {
                this.value = destinationValue;
                return;
            }
        }
        final double result = this.easing.ease(this.getProgress());
        if (this.value > destinationValue) {
            this.value = this.startValue - (this.startValue - destinationValue) * result;
        } else {
            this.value = this.startValue + (destinationValue - this.startValue) * result;
        }
    }

    public boolean update() {
        boolean alive = this.isAlive();
        if (alive) {
            double progress = this.getProgress();
            if (progress >= 1.0) {
                this.value = this.destinationValue;
                this.finished = true;
            } else {
                double result = this.easing.ease(progress);
                if (this.value > this.destinationValue) {
                    this.value = this.startValue - (this.startValue - this.destinationValue) * result;
                } else {
                    this.value = this.startValue + (this.destinationValue - this.startValue) * result;
                }
            }
        } else {
            this.setValue(this.destinationValue);
        }
        return alive;
    }

    /**
     * Returns the progress of the animation
     *
     * @return value between 0 and 1
     */
    public double getProgress() {
        double progress =  Math.max(0, Math.min(1, (double) (System.currentTimeMillis() - this.startTime) / (double) this.duration));
        if (direction == Direction.BACKWARDS){
            progress = 1- progress;
        }
        return progress;
    }


    public boolean isAlive() {
        return !this.isFinished();
    }

    /**
     * Resets the animation to the start value
     */
    public void reset() {
        this.startTime = System.currentTimeMillis();
        this.startValue = this.value;
        this.finished = false;
    }

    public boolean isFinished() {
        return this.finished;
    }

    public boolean isDone(){
        return isFinished();
    }

    public float getValue() {
        return (float) value;
    }

    public void setEasing(Easing easing) {
        this.easing = easing;
    }
    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public boolean isFinished(Direction direction){
        return this.isFinished() && this.direction == direction;
    }
    public enum Direction {
        FORWARDS,
        BACKWARDS
    }
}