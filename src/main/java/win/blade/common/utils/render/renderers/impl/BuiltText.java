package win.blade.common.utils.render.renderers.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.Defines;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.client.render.*;
import net.minecraft.client.render.VertexFormat.DrawMode;
import org.joml.Matrix4f;

import win.blade.common.utils.other.TextAlign;
import win.blade.common.utils.render.msdf.MsdfFont;
import win.blade.common.utils.render.providers.ColorProvider;
import win.blade.common.utils.render.renderers.IRenderer;
import win.blade.common.utils.resource.ResourceUtility;

public record BuiltText(
		MsdfFont font,
		String text,
		float size,
		float thickness,
		int color,
		float smoothness,
		float spacing,
		int outlineColor,
		float outlineThickness,
		TextAlign align
) implements IRenderer {

	private static final ShaderProgramKey msdfFontShaderKey = new ShaderProgramKey(ResourceUtility.getShaderIdentifier("fonts", "msdf_font"),
			VertexFormats.POSITION_TEXTURE_COLOR, Defines.EMPTY);

	@Override
	public void render(Matrix4f matrix, float x, float y, float z) {
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.disableCull();

		RenderSystem.setShaderTexture(0, this.font.getTextureId());

		boolean outlineEnabled = (this.outlineThickness > 0.0f);
		ShaderProgram shader = RenderSystem.setShader(msdfFontShaderKey);
		shader.getUniform("Range").set(this.font.getAtlas().range());
		shader.getUniform("Thickness").set(this.thickness);
		shader.getUniform("Smoothness").set(this.smoothness);
		shader.getUniform("Outline").set(outlineEnabled ? 1 : 0);

		if (outlineEnabled) {
			shader.getUniform("OutlineThickness").set(this.outlineThickness);
			float[] outlineComponents = ColorProvider.normalize(this.outlineColor);
			shader.getUniform("OutlineColor").set(outlineComponents[0], outlineComponents[1],
					outlineComponents[2], outlineComponents[3]);
		}

		BufferBuilder builder = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);

		String[] lines = this.text.split("\n");
		float yOffset = y;
		float lineHeight = this.font.getMetrics().lineHeight() * this.size;
		float baselineOffset = this.font.getMetrics().baselineHeight() * this.size;
		TextAlign currentAlign = this.align == null ? TextAlign.LEFT : this.align;

		for (String line : lines) {
			float lineX = x;
			if (currentAlign != TextAlign.LEFT) {
				float lineWidth = this.font.getWidth(line, this.size);
				if (currentAlign == TextAlign.CENTER) {
					lineX = x - lineWidth / 2f;
				} else if (currentAlign == TextAlign.RIGHT) {
					lineX = x - lineWidth;
				}
			}
			this.font.applyGlyphs(matrix, builder, line, this.size,
					(this.thickness + this.outlineThickness * 0.5f) * 0.5f * this.size, this.spacing,
					lineX, yOffset + baselineOffset, z, this.color);
			yOffset += lineHeight;
		}

		BuiltBuffer builtBuffer = builder.endNullable();
		if (builtBuffer != null) {
			BufferRenderer.drawWithGlobalProgram(builtBuffer);
		}

		RenderSystem.setShaderTexture(0, 0);
		RenderSystem.enableCull();
		RenderSystem.disableBlend();
	}
}