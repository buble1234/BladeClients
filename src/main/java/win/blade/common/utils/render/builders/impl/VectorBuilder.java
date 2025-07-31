package win.blade.common.utils.render.builders.impl;

import net.minecraft.util.Identifier;
import win.blade.common.utils.render.builders.AbstractBuilder;
import win.blade.common.utils.render.builders.states.*;
import win.blade.common.utils.render.renderers.impl.BuiltVector;

public final class VectorBuilder extends AbstractBuilder<BuiltVector> {

    private SizeState size;
    private QuadRadiusState radius;
    private QuadColorState color;
    private float smoothness;
    private Identifier svgPath;

    public VectorBuilder size(SizeState size) {
        this.size = size;
        return this;
    }

    public VectorBuilder radius(QuadRadiusState radius) {
        this.radius = radius;
        return this;
    }

    public VectorBuilder color(QuadColorState color) {
        this.color = color;
        return this;
    }

    public VectorBuilder smoothness(float smoothness) {
        this.smoothness = smoothness;
        return this;
    }

    public VectorBuilder svg(Identifier svgPath) {
        this.svgPath = svgPath;
        return this;
    }

    @Override
    protected BuiltVector _build() {
        return new BuiltVector(
            this.size,
            this.radius,
            this.color,
            this.smoothness,
            this.svgPath
        );
    }

    @Override
    protected void reset() {
        this.size = SizeState.NONE;
        this.radius = QuadRadiusState.NO_ROUND;
        this.color = QuadColorState.WHITE;
        this.smoothness = 1.0f;
        this.svgPath = null;
    }
}