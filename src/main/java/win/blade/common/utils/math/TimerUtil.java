package win.blade.common.utils.math;

public class TimerUtil {
    private long last;
    private long lastMS;
    public TimerUtil() {
        reset();
    }

    public void updateLast() {
        last = System.currentTimeMillis();
    }

    public boolean timeElapsed(long ms) {
        return System.currentTimeMillis() - last >= ms;
    }

    public void reset() {
        lastMS = System.currentTimeMillis();
    }

    public long elapsedTime() {
        return System.currentTimeMillis() - lastMS;
    }

    public boolean hasReached(long time) {
        return elapsedTime() >= time;
    }

    public long getElapsedTime() {
        return System.currentTimeMillis() - last;
    }


    public long getLast() {
        return last;
    }

    public void setLast(long last) {
        this.last = last;
    }

    public void setElapsed(long ms) {
        last = System.currentTimeMillis() - ms;
    }

    public boolean hasSecondsElapsed(double seconds) {
        return hasReached((long) (seconds * 1000));
    }
}