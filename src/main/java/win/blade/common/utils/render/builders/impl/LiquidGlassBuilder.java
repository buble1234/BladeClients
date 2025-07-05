package win.blade.common.utils.render.builders.impl;

import win.blade.common.utils.render.builders.AbstractBuilder;
import win.blade.common.utils.render.builders.states.SizeState;
import win.blade.common.utils.render.renderers.impl.BuiltLiquidGlass;

/**
 * Автор: NoCap
 * Дата создания: 04.07.2025
 */
public final class LiquidGlassBuilder extends AbstractBuilder<BuiltLiquidGlass> {

    private SizeState size;
    private float blurSize = 20.0f;
    private float quality = 10.0f;
    private float direction = 10.0f;

    public LiquidGlassBuilder size(SizeState size) {
        this.size = size;
        return this;
    }

    public LiquidGlassBuilder size(float width, float height) {
        this.size = new SizeState(width, height);
        return this;
    }

    public LiquidGlassBuilder blurSize(float blurSize) {
        this.blurSize = blurSize;
        return this;
    }

    public LiquidGlassBuilder quality(float quality) {
        this.quality = quality;
        return this;
    }

    public LiquidGlassBuilder direction(float direction) {
        this.direction = direction;
        return this;
    }

    @Override
    protected BuiltLiquidGlass _build() {
        return new BuiltLiquidGlass(
                this.size,
                this.blurSize,
                this.quality,
                this.direction
        );
    }

    @Override
    protected void reset() {
        this.size = new SizeState(100, 100);
        this.blurSize = 20.0f;
        this.quality = 10.0f;
        this.direction = 10.0f;
    }
}