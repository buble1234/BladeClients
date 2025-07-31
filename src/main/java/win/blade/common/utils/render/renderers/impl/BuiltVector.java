package win.blade.common.utils.render.renderers.impl;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Defines;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import com.mojang.blaze3d.systems.RenderSystem;
import win.blade.common.utils.render.builders.states.*;
import win.blade.common.utils.render.renderers.IRenderer;
import win.blade.common.utils.render.vector.VectorManager;
import win.blade.common.utils.resource.ResourceUtility;

public record BuiltVector(
        SizeState size,
        QuadRadiusState radius,
        QuadColorState color,
        float smoothness,
        Identifier svgPath
) implements IRenderer {

    private static final ShaderProgramKey TEXTURE_SHADER_KEY = new ShaderProgramKey(ResourceUtility.getShaderIdentifier("common","texture"),
            VertexFormats.POSITION_TEXTURE_COLOR, Defines.EMPTY);

    @Override
    public void render(Matrix4f matrix, float x, float y, float z) {
        if (svgPath == null) return;

        float logicalWidth = this.size.width();
        float logicalHeight = this.size.height();

        double scale = MinecraftClient.getInstance().getWindow().getScaleFactor();

        int textureWidth = (int) Math.ceil(logicalWidth * scale);
        int textureHeight = (int) Math.ceil(logicalHeight * scale);

        int textureId = VectorManager.getInstance().getTexture(svgPath, textureWidth, textureHeight);

        if (textureId == 0) return;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();

        RenderSystem.setShaderTexture(0, textureId);

        ShaderProgram shader = RenderSystem.setShader(TEXTURE_SHADER_KEY);
        shader.getUniform("Size").set(logicalWidth, logicalHeight);
        shader.getUniform("Radius").set(this.radius.radius1(), this.radius.radius2(), this.radius.radius3(), this.radius.radius4());
        shader.getUniform("Smoothness").set(this.smoothness);

        BufferBuilder builder = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        builder.vertex(matrix, x, y, z).texture(0, 0).color(this.color.color1());
        builder.vertex(matrix, x, y + logicalHeight, z).texture(0, 1).color(this.color.color2());
        builder.vertex(matrix, x + logicalWidth, y + logicalHeight, z).texture(1, 1).color(this.color.color3());
        builder.vertex(matrix, x + logicalWidth, y, z).texture(1, 0).color(this.color.color4());

        BufferRenderer.drawWithGlobalProgram(builder.end());

        RenderSystem.setShaderTexture(0, 0);

        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }
}