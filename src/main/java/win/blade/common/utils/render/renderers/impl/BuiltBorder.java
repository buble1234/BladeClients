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
        float internalSmoothness, float externalSmoothness
) implements IRenderer {

    private static final ShaderProgramKey BORDER_SHADER_KEY = new ShaderProgramKey(ResourceUtility.getShaderIdentifier("common","border"),
            VertexFormats.POSITION_COLOR, Defines.EMPTY);

    @Override
    public void render(Matrix4f matrix, float x, float y, float z) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();

        float width = this.size.width(), height = this.size.height();
        ShaderProgram shader = RenderSystem.setShader(BORDER_SHADER_KEY);
        shader.getUniform("Size").set(width, height);
        shader.getUniform("Radius").set(this.radius.radius1(), this.radius.radius2(), this.radius.radius3(), this.radius.radius4());
        shader.getUniform("Thickness").set(thickness);
        shader.getUniform("Smoothness").set(this.internalSmoothness, this.externalSmoothness);

        int color1 = this.outlineColor.color1();
        int color2 = this.outlineColor.color2();
        int color3 = this.outlineColor.color3();
        int color4 = this.outlineColor.color4();

        shader.getUniform("OutlineColor[0]").set(
                ((color1 >> 16) & 0xFF) / 255.0f, ((color1 >> 8) & 0xFF) / 255.0f,
                (color1 & 0xFF) / 255.0f, ((color1 >> 24) & 0xFF) / 255.0f
        );
        shader.getUniform("OutlineColor[1]").set(
                ((color2 >> 16) & 0xFF) / 255.0f, ((color2 >> 8) & 0xFF) / 255.0f,
                (color2 & 0xFF) / 255.0f, ((color2 >> 24) & 0xFF) / 255.0f
        );
        shader.getUniform("OutlineColor[2]").set(
                ((color3 >> 16) & 0xFF) / 255.0f, ((color3 >> 8) & 0xFF) / 255.0f,
                (color3 & 0xFF) / 255.0f, ((color3 >> 24) & 0xFF) / 255.0f
        );
        shader.getUniform("OutlineColor[3]").set(
                ((color4 >> 16) & 0xFF) / 255.0f, ((color4 >> 8) & 0xFF) / 255.0f,
                (color4 & 0xFF) / 255.0f, ((color4 >> 24) & 0xFF) / 255.0f
        );

        BufferBuilder builder = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        builder.vertex(matrix, x, y, z).color(this.color.color1());
        builder.vertex(matrix, x, y + height, z).color(this.color.color2());
        builder.vertex(matrix, x + width, y + height, z).color(this.color.color3());
        builder.vertex(matrix, x + width, y, z).color(this.color.color4());

        BufferRenderer.drawWithGlobalProgram(builder.end());

        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }
}