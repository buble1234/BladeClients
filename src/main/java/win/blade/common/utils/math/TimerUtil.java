package win.blade.common.utils.math;

public class TimerUtil {
    private long last;
    private long lastMS;
    public TimerUtil() {
        reset();
    }

    public static TimerUtil create() {
        return new TimerUtil();
    }

    public void updateLast() {
        last = System.currentTimeMillis();
    }

    public void resetTimer() {
        lastMS = System.currentTimeMillis();
    }

    public boolean isReached(long time) {
        return System.currentTimeMillis() - lastMS > time;
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

    public boolean every(long ms) {
        if (hasReached(ms)) {
            reset();
            return true;
        }
        return false;
    }

    public boolean finished(long ms) {
        return hasReached(ms);
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