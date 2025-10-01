package win.blade.core.event.impl.render;


import win.blade.core.event.controllers.Event;

public class FogEvent extends Event {
    private float distance;
    private int color;

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }
}