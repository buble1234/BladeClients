package win.blade.common.utils.render.renderers.impl;

import net.minecraft.client.gl.Defines;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import org.joml.Matrix4f;

import com.mojang.blaze3d.systems.RenderSystem;
import win.blade.common.utils.render.builders.states.QuadColorState;
import win.blade.common.utils.render.builders.states.QuadRadiusState;
import win.blade.common.utils.render.builders.states.SizeState;
import win.blade.common.utils.render.renderers.IRenderer;
import win.blade.common.utils.resource.ResourceUtility;

public record BuiltBorder(
        SizeState size,
        QuadRadiusState radius,
        QuadColorState color,
        QuadColorState outlineColor,
        float thickness,
        float internalSmoothness, float externalSmoothness,
        boolean gradientEnabled,
        QuadColorState outlineColor2,
        float gradientSpeed
) implements IRenderer {

    private static final ShaderProgramKey BORDER_SHADER_KEY = new ShaderProgramKey(ResourceUtility.getShaderIdentifier("common","border"),
            VertexFormats.POSITION_COLOR, Defines.EMPTY);

    @Override
    public void render(Matrix4f matrix, float x, float y, float z) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();

        ShaderProgram shader = RenderSystem.setShader(BORDER_SHADER_KEY);
        shader.getUniform("Size").set(this.size.width(), this.size.height());
        shader.getUniform("Radius").set(this.radius.radius1(), this.radius.radius2(), this.radius.radius3(), this.radius.radius4());
        shader.getUniform("Thickness").set(this.thickness);
        shader.getUniform("Smoothness").set(this.internalSmoothness, this.externalSmoothness);

        shader.getUniform("gradientFlag").set(this.gradientEnabled ? 1.0f : 0.0f);
        shader.getUniform("gradientSpeed").set(this.gradientSpeed);
        shader.getUniform("globalTime").set(System.nanoTime() / 1_000_000_000f);

        int[] colors1 = {this.outlineColor.color1(), this.outlineColor.color2(), this.outlineColor.color3(), this.outlineColor.color4()};
        int[] colors2 = {this.outlineColor2.color1(), this.outlineColor2.color2(), this.outlineColor2.color3(), this.outlineColor2.color4()};

        for (int i = 0; i < 4; i++) {
            shader.getUniform("OutlineColor[" + i + "]").set(
                    ((colors1[i] >> 16) & 0xFF) / 255.0f, ((colors1[i] >> 8) & 0xFF) / 255.0f,
                    (colors1[i] & 0xFF) / 255.0f, ((colors1[i] >> 24) & 0xFF) / 255.0f
            );
            shader.getUniform("OutlineColor2[" + i + "]").set(
                    ((colors2[i] >> 16) & 0xFF) / 255.0f, ((colors2[i] >> 8) & 0xFF) / 255.0f,
                    (colors2[i] & 0xFF) / 255.0f, ((colors2[i] >> 24) & 0xFF) / 255.0f
            );
        }

        BufferBuilder builder = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        builder.vertex(matrix, x, y, z).color(this.color.color1());
        builder.vertex(matrix, x, y + this.size.height(), z).color(this.color.color2());
        builder.vertex(matrix, x + this.size.width(), y + this.size.height(), z).color(this.color.color3());
        builder.vertex(matrix, x + this.size.width(), y, z).color(this.color.color4());

        BufferRenderer.drawWithGlobalProgram(builder.end());

        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }
}