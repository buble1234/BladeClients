package win.blade.core.event.impl.render;

import win.blade.core.event.controllers.Event;

/**
 * Автор: NoCap
 * Дата создания: 05.10.2025
 */
public class CameraClipEvent extends Event {

    private float distance;
    private boolean raytrace;

    public CameraClipEvent(float distance, boolean raytrace) {
        this.distance = distance;
        this.raytrace = raytrace;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public boolean getRaytrace() {
        return raytrace;
    }

    public void setRaytrace(boolean raytrace) {
        this.raytrace = raytrace;
    }
}
