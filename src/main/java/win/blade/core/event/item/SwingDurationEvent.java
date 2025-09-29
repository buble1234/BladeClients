package win.blade.core.event.item;

import win.blade.core.event.controllers.Event;

public class SwingDurationEvent extends Event {
    private float animation;

    public float getAnimation() {
        return animation;
    }

    public void setAnimation(float animation) {
        this.animation = animation;
    }
}