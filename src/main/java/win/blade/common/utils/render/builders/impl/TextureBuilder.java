package win.blade.common.utils.render.builders.impl;


import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.util.Identifier;
import win.blade.common.utils.render.builders.*;
import win.blade.common.utils.render.builders.states.*;
import win.blade.common.utils.render.renderers.impl.*;
import win.blade.common.utils.render.vector.VectorManager;

public final class TextureBuilder extends AbstractBuilder<BuiltTexture> {

    private SizeState size;
    private QuadRadiusState radius;
    private QuadColorState color;
    private float smoothness;
    private float u, v;
    private float texWidth, texHeight;
    private int textureId;
    private boolean svg;
    private Identifier svgIdentifier;

    public TextureBuilder size(SizeState size) {
        this.size = size;
        return this;
    }

    public TextureBuilder radius(QuadRadiusState radius) {
        this.radius = radius;
        return this;
    }

    public TextureBuilder color(QuadColorState color) {
        this.color = color;
        return this;
    }

    public TextureBuilder smoothness(float smoothness) {
        this.smoothness = smoothness;
        return this;
    }

    public TextureBuilder texture(float u, float v, float texWidth, float texHeight, AbstractTexture texture) {
        return texture(u, v, texWidth, texHeight, texture.getGlId());
    }

    public TextureBuilder texture(float u, float v, float texWidth, float texHeight, int textureId) {
        this.u = u;
        this.v = v;
        this.texWidth = texWidth;
        this.texHeight = texHeight;
        this.textureId = textureId;
        return this;
    }

    public TextureBuilder svgTexture(Identifier identifier){
        return svgTexture(0, 0, 1, 1, identifier);
    }

    public TextureBuilder svgTexture(float u, float v, float texWidth, float texHeight, Identifier identifier){
        this.u = u;
        this.v = v;
        this.texWidth = texWidth;
        this.texHeight = texHeight;
        this.svg = true;
        this.svgIdentifier = identifier;

        return this;
    }

    @Override
    protected BuiltTexture _build() {
        if(svg){
            double scale = MinecraftClient.getInstance().getWindow().getScaleFactor();

            int textureWidth = (int) Math.ceil(size.width() * scale);
            int textureHeight = (int) Math.ceil(size.height() * scale);

            textureId = VectorManager.getInstance().getTexture(svgIdentifier, textureWidth, textureHeight);
        }

        return new BuiltTexture(
            this.size,
            this.radius,
            this.color,
            this.smoothness,
            this.u, this.v,
            this.texWidth, this.texHeight,
            this.textureId
        );
    }

    @Override
    protected void reset() {
        this.size = SizeState.NONE;
        this.radius = QuadRadiusState.NO_ROUND;
        this.color = QuadColorState.WHITE;
        this.smoothness = 1.0f;
        this.u = 0.0f;
        this.v = 0.0f;
        this.texWidth = 0.0f;
        this.texHeight = 0.0f;
        this.textureId = 0;
        svg = false;
    }

}