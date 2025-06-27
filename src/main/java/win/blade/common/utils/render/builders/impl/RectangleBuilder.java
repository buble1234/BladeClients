package win.blade.common.utils.render.builders.impl;

import win.blade.common.utils.render.builders.*;
import win.blade.common.utils.render.builders.states.*;
import win.blade.common.utils.render.renderers.impl.*;

public final class RectangleBuilder extends AbstractBuilder<BuiltRectangle> {

    private SizeState size;
    private QuadRadiusState radius;
    private QuadColorState color;
    private float smoothness;

    public RectangleBuilder size(SizeState size) {
        this.size = size;
        return this;
    }

    public RectangleBuilder size(float x, float y) {
        this.size = new SizeState(x, y);
        return this;
    }

    public RectangleBuilder radius(QuadRadiusState radius) {
        this.radius = radius;
        return this;
    }

    public RectangleBuilder radius(float radius){
    this.radius = new QuadRadiusState(radius);
        return this;
    }

    public RectangleBuilder color(QuadColorState color) {
        this.color = color;
        return this;
    }
    public RectangleBuilder color(int color) {
        this.color = new QuadColorState(color);
        return this;
    }

    public RectangleBuilder smoothness(float smoothness) {
        this.smoothness = smoothness;
        return this;
    }

    @Override
    protected BuiltRectangle _build() {
        return new BuiltRectangle(
            this.size,
            this.radius,
            this.color,
            this.smoothness
        );
    }

    @Override
    protected void reset() {
        this.size = SizeState.NONE;
        this.radius = QuadRadiusState.NO_ROUND;
        this.color = QuadColorState.TRANSPARENT;
        this.smoothness = 1.0f;
    }

}