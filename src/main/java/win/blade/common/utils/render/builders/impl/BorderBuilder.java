package win.blade.common.utils.render.builders.impl;

import win.blade.common.utils.render.builders.AbstractBuilder;
import win.blade.common.utils.render.builders.states.QuadColorState;
import win.blade.common.utils.render.builders.states.QuadRadiusState;
import win.blade.common.utils.render.builders.states.SizeState;
import win.blade.common.utils.render.renderers.impl.BuiltBorder;

public final class BorderBuilder extends AbstractBuilder<BuiltBorder> {

    private SizeState size;
    private QuadRadiusState radius;
    private QuadColorState color;
    private QuadColorState outlineColor;
    private float thickness;
    private float internalSmoothness, externalSmoothness;
    private boolean gradientEnabled;
    private QuadColorState outlineColor2;
    private float gradientSpeed;

    public BorderBuilder size(SizeState size) {
        this.size = size;
        return this;
    }

    public BorderBuilder radius(QuadRadiusState radius) {
        this.radius = radius;
        return this;
    }

    public BorderBuilder color(QuadColorState color) {
        this.color = color;
        return this;
    }

    public BorderBuilder outlineColor(QuadColorState outlineColor) {
        this.outlineColor = outlineColor;
        return this;
    }

    public BorderBuilder thickness(float thickness) {
        this.thickness = thickness;
        return this;
    }

    public BorderBuilder smoothness(float internalSmoothness, float externalSmoothness) {
        this.internalSmoothness = internalSmoothness;
        this.externalSmoothness = externalSmoothness;
        return this;
    }

    public BorderBuilder gradient(boolean enabled, QuadColorState outlineColor2, float speed) {
        this.gradientEnabled = enabled;
        this.outlineColor2 = outlineColor2;
        this.gradientSpeed = speed;
        return this;
    }

    @Override
    protected BuiltBorder _build() {
        return new BuiltBorder(
                this.size,
                this.radius,
                this.color,
                this.outlineColor,
                this.thickness,
                this.internalSmoothness, this.externalSmoothness,
                this.gradientEnabled,
                this.outlineColor2,
                this.gradientSpeed
        );
    }

    @Override
    protected void reset() {
        this.size = SizeState.NONE;
        this.radius = QuadRadiusState.NO_ROUND;
        this.color = QuadColorState.TRANSPARENT;
        this.outlineColor = QuadColorState.TRANSPARENT;
        this.thickness = 0.0f;
        this.internalSmoothness = 1.0f;
        this.externalSmoothness = 1.0f;
        this.gradientEnabled = false;
        this.outlineColor2 = QuadColorState.TRANSPARENT;
        this.gradientSpeed = 2.0f;
    }
}