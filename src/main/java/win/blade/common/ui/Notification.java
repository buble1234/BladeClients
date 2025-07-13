package win.blade.common.ui;

import net.minecraft.client.gui.DrawContext;
import win.blade.common.utils.math.TimerUtil;
import win.blade.common.utils.math.animation.Animation;
import win.blade.common.utils.math.animation.Easing;
import win.blade.common.utils.minecraft.MinecraftInstance;
import win.blade.common.utils.render.msdf.FontType;
import win.blade.common.utils.render.msdf.MsdfFont;

public abstract class Notification implements MinecraftInstance {

    private enum State {
        APPEARING,
        DISPLAYING,
        FADING_OUT
    }

    protected final String content;
    protected final long delay;
    protected final MsdfFont font = FontType.sf_regular.get();
    protected final float fontSize = 5f;
    private final TimerUtil timer = new TimerUtil();

    protected final Animation slideAnimation = new Animation();
    protected final Animation fadeAnimation = new Animation();
    protected final Animation yOffsetAnimation = new Animation();

    private State currentState = State.APPEARING;

    private final float notificationHeight = 32f;
    private final float notificationGap = 4f;

    public Notification(String content, long delay, int index) {
        this.content = content;
        this.delay = delay;
        this.timer.reset();

        float targetY = calculateTargetY(index);

        slideAnimation.set(0.0);
        fadeAnimation.set(0.0);
        yOffsetAnimation.set(targetY + notificationHeight);

        slideAnimation.run(1.0, 0.4, Easing.EASE_OUT_CUBIC);
        fadeAnimation.run(1.0, 0.4, Easing.EASE_OUT_CUBIC);
        yOffsetAnimation.run(targetY, 0.4, Easing.EASE_OUT_CUBIC);
    }

    public abstract void render(DrawContext context);

    public void update(int newIndex) {
        slideAnimation.update();
        fadeAnimation.update();
        yOffsetAnimation.update();

        float targetY = calculateTargetY(newIndex);
        yOffsetAnimation.run(targetY, 0.3, Easing.EASE_OUT_SINE, true);

        if (currentState == State.APPEARING && slideAnimation.isFinished()) {
            currentState = State.DISPLAYING;
            timer.reset();
        }

        if (currentState == State.DISPLAYING && timer.hasReached(delay)) {
            startFadingOut();
        }
    }

    private void startFadingOut() {
        currentState = State.FADING_OUT;
        slideAnimation.run(0.0, 0.4, Easing.EASE_IN_CUBIC);
        fadeAnimation.run(0.0, 0.4, Easing.EASE_IN_CUBIC);
    }

    public boolean isFinished() {
        return currentState == State.FADING_OUT && fadeAnimation.isFinished();
    }

    protected float calculateTargetY(int multiplier) {
        return mc.getWindow().getScaledHeight() - 42 - (multiplier * (notificationHeight + notificationGap));
    }
}