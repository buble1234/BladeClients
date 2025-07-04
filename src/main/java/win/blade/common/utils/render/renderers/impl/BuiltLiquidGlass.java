package win.blade.common.utils.render.renderers.impl;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.*;
import net.minecraft.client.render.*;
import org.joml.Matrix4f;
import win.blade.common.utils.render.builders.states.SizeState;
import win.blade.common.utils.render.renderers.IRenderer;
import win.blade.common.utils.resource.ResourceUtility;

import static win.blade.common.utils.minecraft.MinecraftInstance.mc;

/**
 * Автор: NoCap
 * Дата создания: 04.07.2025
 */
public record BuiltLiquidGlass(
        SizeState size,
        float blurSize,
        float quality,
        float direction
) implements IRenderer {

    private static final ShaderProgramKey LIQUID_GLASS_SHADER_KEY = new ShaderProgramKey(ResourceUtility.getShaderIdentifier("common", "liquid_glass"), VertexFormats.POSITION_TEXTURE_COLOR, Defines.EMPTY);

    private static final Supplier<SimpleFramebuffer> TEMP_FBO_SUPPLIER = Suppliers.memoize(() -> new SimpleFramebuffer(1920, 1024, false));
    private static final Framebuffer MAIN_FBO = MinecraftClient.getInstance().getFramebuffer();

    @Override
    public void render(Matrix4f matrix, float x, float y, float z) {
        SimpleFramebuffer tempFbo = TEMP_FBO_SUPPLIER.get();
        if (tempFbo.textureWidth != MAIN_FBO.textureWidth || tempFbo.textureHeight != MAIN_FBO.textureHeight) {
            tempFbo.resize(MAIN_FBO.textureWidth, MAIN_FBO.textureHeight);
        }

        tempFbo.beginWrite(false);
        MAIN_FBO.draw(tempFbo.textureWidth, tempFbo.textureHeight);
        MAIN_FBO.beginWrite(false);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();

        RenderSystem.setShaderTexture(0, tempFbo.getColorAttachment());

        float width = this.size.width(), height = this.size.height();

        float mouseX = (float) (mc.mouse.getX() * mc.getWindow().getScaleFactor());
        float mouseY = (float) (mc.mouse.getY() * mc.getWindow().getScaleFactor());

        ShaderProgram shader = RenderSystem.setShader(LIQUID_GLASS_SHADER_KEY);

        shader.getUniform("Size").set(width, height);
        shader.getUniform("iResolution").set((float) mc.getWindow().getFramebufferWidth(), (float) mc.getWindow().getFramebufferHeight());
        shader.getUniform("iMouse").set(mouseX, (float) mc.getWindow().getFramebufferHeight() - mouseY);

        shader.getUniform("BlurSize").set(this.blurSize);
        shader.getUniform("Quality").set(this.quality);
        shader.getUniform("Direction").set(this.direction);

        BufferBuilder builder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);

        builder.vertex(matrix, x, y, z).texture(0.0f, 0.0f).color(255, 255, 255, 255);
        builder.vertex(matrix, x, y + height, z).texture(0.0f, 1.0f).color(255, 255, 255, 255);
        builder.vertex(matrix, x + width, y + height, z).texture(1.0f, 1.0f).color(255, 255, 255, 255);
        builder.vertex(matrix, x + width, y, z).texture(1.0f, 0.0f).color(255, 255, 255, 255);

        BufferRenderer.drawWithGlobalProgram(builder.end());

        RenderSystem.setShaderTexture(0, 0);

        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }
}