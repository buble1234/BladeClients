package win.blade.common.utils.math.animation;


import win.blade.common.utils.math.TimerUtil;

public interface AnimationHelp {
    TimerUtil timer = new TimerUtil();

    default float stagger() { return 75f; }
    default float duration() { return 400f; }
    default float moduleStagger() { return 50f; }
    default float moduleDuration() { return 300f; }
    default float selectDuration() { return 200f; }
}