package win.blade.core.event.impl.render;

import win.blade.core.event.controllers.Event;

/**
 * Автор: NoCap
 * Дата создания: 26.07.2025
 */
public class FovEvent extends Event {
    private float fov;

    public FovEvent(float fov) {
        this.fov = fov;
    }

    public float getFov() {
        return fov;
    }

    public void setFov(float fov) {
        this.fov = fov;
    }
}