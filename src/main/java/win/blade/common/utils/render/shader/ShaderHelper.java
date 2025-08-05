package win.blade.common.utils.render.shader;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import org.joml.Vector3f;
import win.blade.common.utils.minecraft.MinecraftInstance;
import win.blade.common.utils.render.shader.storage.DepthShader;
import win.blade.common.utils.render.shader.storage.GaussianShader;
import win.blade.common.utils.render.shader.storage.PassThroughShader;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;

public class ShaderHelper implements MinecraftInstance {

    private static GaussianShader gaussianShader;
    private static DepthShader depthShader;
    private static PassThroughShader passThroughShader;

    private static SimpleFramebuffer copyFbo;
    private static SimpleFramebuffer fbo1;
    private static SimpleFramebuffer fbo2;
    private static SimpleFramebuffer depthFbo;

    private static boolean initialized = false;
    private static final long startTime = System.nanoTime();

    private static void initShadersIfNeeded() {
        if (initialized) return;
        try {
            gaussianShader = new GaussianShader();
            depthShader = new DepthShader();
            passThroughShader = new PassThroughShader();
            initialized = true;
        } catch (Exception e) {
            System.err.println("Не удалось инициализировать шейдеры!");
            e.printStackTrace();
        }
    }

    private static void checkFramebuffers() {
        int width = mc.getWindow().getFramebufferWidth();
        int height = mc.getWindow().getFramebufferHeight();
        if (copyFbo == null || copyFbo.textureWidth != width || copyFbo.textureHeight != height) {
            if (copyFbo != null) {
                copyFbo.delete(); fbo1.delete(); fbo2.delete(); depthFbo.delete();
            }
            copyFbo = new SimpleFramebuffer(width, height, true);
            fbo1 = new SimpleFramebuffer(width, height, true);
            fbo2 = new SimpleFramebuffer(width, height, true);
            depthFbo = new SimpleFramebuffer(width, height, true);
        }
    }

    public static void applyFogBlur(float strength, float distance, boolean linearSampling, boolean rainbow, boolean rainbowNoise, float rainbowStrength) {
        initShadersIfNeeded();
        if (!initialized) return;

        checkFramebuffers();
        copyMainFramebuffer();

        gaussianShader.apply(copyFbo.getColorAttachment(), strength, linearSampling, fbo1, fbo2);

        float time = (float) ((System.nanoTime() - startTime) / 1E9D);
        float yaw = (mc.player != null) ? mc.player.getYaw() : 0f;
        float pitch = (mc.player != null) ? mc.player.getPitch() : 0f;

        depthShader.apply(fbo2.getColorAttachment(), copyFbo.getDepthAttachment(), distance, mc.gameRenderer.getFarPlaneDistance(), depthFbo, rainbow, rainbowNoise, rainbowStrength, time, yaw, pitch);

        mc.getFramebuffer().beginWrite(false);
        passThroughShader.apply(depthFbo.getColorAttachment());
    }

    private static void copyMainFramebuffer() {
        Framebuffer mainFbo = mc.getFramebuffer();
        GlStateManager._glBindFramebuffer(GL_READ_FRAMEBUFFER, mainFbo.fbo);
        GlStateManager._glBindFramebuffer(GL_DRAW_FRAMEBUFFER, copyFbo.fbo);
        GlStateManager._glBlitFrameBuffer(
                0, 0, mainFbo.textureWidth, mainFbo.textureHeight,
                0, 0, copyFbo.textureWidth, copyFbo.textureHeight,
                GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT, GL_NEAREST
        );
        mc.getFramebuffer().beginWrite(false);
    }

    public static Vector3f calculateGaussian(float sigma) {
        final float sqrt2PI = 2.50662f;
        float y = (float) Math.exp(-0.5f / (sigma * sigma));
        return new Vector3f(1 / (sqrt2PI * sigma), y, y * y);
    }

    public static void drawFullScreenQuad() {
        RenderSystem.assertOnRenderThread();
        BufferBuilder bufferBuilder = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
        bufferBuilder.vertex(-1.0f, -1.0f, 0.0f);
        bufferBuilder.vertex(1.0f, -1.0f, 0.0f);
        bufferBuilder.vertex(1.0f, 1.0f, 0.0f);
        bufferBuilder.vertex(-1.0f, 1.0f, 0.0f);
        BufferRenderer.draw(bufferBuilder.end());
    }
}