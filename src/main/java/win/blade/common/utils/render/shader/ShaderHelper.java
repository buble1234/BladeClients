package win.blade.common.utils.render.shader;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.*;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL30;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;

public class ShaderHelper {
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    private static Shader gaussianShader;
    private static Shader depthShader;
    private static Shader passThroughShader;

    private static SimpleFramebuffer copyFbo;
    private static SimpleFramebuffer fbo1;
    private static SimpleFramebuffer fbo2;
    private static SimpleFramebuffer depthFbo;

    private static boolean initialized = false;

    private static void initShadersIfNeeded() {
        if (initialized) return;
        try {
            gaussianShader = new Shader("gaussian");
            depthShader = new Shader("depth");
            passThroughShader = new Shader("passthrough");
            initialized = true;
        } catch (Exception e) {
            System.err.println("Failed to initialize FogBlur shaders!");
            e.printStackTrace();
        }
    }

    private static void checkFramebuffers() {
        int width = mc.getWindow().getFramebufferWidth();
        int height = mc.getWindow().getFramebufferHeight();
        if (copyFbo == null || copyFbo.textureWidth != width || copyFbo.textureHeight != height) {
            if (copyFbo != null) {
                copyFbo.delete();
                fbo1.delete();
                fbo2.delete();
                depthFbo.delete();
            }
            copyFbo = new SimpleFramebuffer(width, height, true);
            fbo1 = new SimpleFramebuffer(width, height, true);
            fbo2 = new SimpleFramebuffer(width, height, true);
            depthFbo = new SimpleFramebuffer(width, height, true);
        }
    }

    public static void applyFogBlur(float strength, float distance, boolean linearSampling) {
        initShadersIfNeeded();
        if (!initialized) return;

        try {
            checkFramebuffers();

            // --- КЛЮЧЕВОЕ ИСПРАВЛЕНИЕ: Ручное копирование кадра ---
            Framebuffer mainFbo = mc.getFramebuffer();
            GlStateManager._glBindFramebuffer(GL_READ_FRAMEBUFFER, mainFbo.fbo);
            GlStateManager._glBindFramebuffer(GL_DRAW_FRAMEBUFFER, copyFbo.fbo);
            GlStateManager._glBlitFrameBuffer(
                    0, 0, mainFbo.textureWidth, mainFbo.textureHeight,
                    0, 0, copyFbo.textureWidth, copyFbo.textureHeight,
                    GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT, GL_NEAREST
            );

            applyGaussianBlur(strength, linearSampling);
            applyDepthMask(distance);
            renderToScreen();

        } finally {
            // Восстанавливаем основной фреймбуфер Minecraft
            mc.getFramebuffer().beginWrite(false);
        }
    }

    private static void applyGaussianBlur(float strength, boolean linearSampling) {
        Vector3f gaussian = calculateGaussian(strength);
        int support = (int) Math.ceil(strength * 3);

        gaussianShader.bind();
        gaussianShader.setUniform1i("Tex0", 0);
        gaussianShader.setUniformBool("Alpha", false);
        gaussianShader.setUniform3f("Gaussian", gaussian);
        gaussianShader.setUniform1i("Support", support);
        gaussianShader.setUniformBool("LinearSampling", linearSampling);

        fbo1.beginWrite(true);
        gaussianShader.setUniform2f("Direction", 1.0f, 0.0f);
        gaussianShader.setUniform2f("TexelSize", 1.0f / fbo1.textureWidth, 1.0f / fbo1.textureHeight);
        RenderSystem.bindTexture(copyFbo.getColorAttachment());
        drawFullScreenQuad();

        fbo2.beginWrite(true);
        gaussianShader.setUniform2f("Direction", 0.0f, 1.0f);
        gaussianShader.setUniform2f("TexelSize", 1.0f / fbo2.textureWidth, 1.0f / fbo2.textureHeight);
        RenderSystem.bindTexture(fbo1.getColorAttachment());
        drawFullScreenQuad();
        gaussianShader.unbind();
    }

    private static void applyDepthMask(float fogDistance) {
        depthFbo.beginWrite(true);
        depthShader.bind();
        depthShader.setUniform1i("Tex0", 0);
        depthShader.setUniform1i("Tex1", 1);
        depthShader.setUniform1f("Near", 0.05f);
        depthShader.setUniform1f("Far", mc.gameRenderer.getFarPlaneDistance());
        depthShader.setUniform1f("MinThreshold", 0.10f * fogDistance / 100f);
        depthShader.setUniform1f("MaxThreshold", 0.28f * fogDistance / 100f);

        RenderSystem.activeTexture(GL30.GL_TEXTURE0);
        RenderSystem.bindTexture(fbo2.getColorAttachment());

        RenderSystem.activeTexture(GL30.GL_TEXTURE1);
        RenderSystem.bindTexture(copyFbo.getDepthAttachment());

        drawFullScreenQuad();
        depthShader.unbind();

        RenderSystem.activeTexture(GL30.GL_TEXTURE0);
    }

    private static void renderToScreen() {
        mc.getFramebuffer().beginWrite(false);
        passThroughShader.bind();
        passThroughShader.setUniform1i("Tex0", 0);
        passThroughShader.setUniformBool("Alpha", true);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        RenderSystem.bindTexture(depthFbo.getColorAttachment());
        drawFullScreenQuad();

        RenderSystem.disableBlend();
        passThroughShader.unbind();
    }

    private static Vector3f calculateGaussian(float sigma) {
        final float sqrt2PI = 2.50662f;
        float y = (float) Math.exp(-0.5f / (sigma * sigma));
        return new Vector3f(1 / (sqrt2PI * sigma), y, y * y);
    }

    private static void drawFullScreenQuad() {
        RenderSystem.assertOnRenderThread();
        BufferBuilder bufferBuilder = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
        bufferBuilder.vertex(-1.0f, -1.0f, 0.0f);
        bufferBuilder.vertex(1.0f, -1.0f, 0.0f);
        bufferBuilder.vertex(1.0f, 1.0f, 0.0f);
        bufferBuilder.vertex(-1.0f, 1.0f, 0.0f);
        BufferRenderer.draw(bufferBuilder.end());
    }
}