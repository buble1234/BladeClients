package win.blade.common.utils.render.shader;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.render.*;
import win.blade.common.utils.minecraft.MinecraftInstance;
import win.blade.common.utils.render.shader.storage.*;

public class ShaderHelper implements MinecraftInstance {
    private static GaussianShader gaussianShader;
    private static DepthShader depthShader;
    private static Shader passThroughShader;
    private static TintShader tintShader;
    private static ReflectionShader reflectionShader;
    private static ColorGradingShader colorGradingShader;
    private static SimpleFramebuffer copyFbo;
    private static SimpleFramebuffer fbo1;
    private static SimpleFramebuffer fbo2;
    private static SimpleFramebuffer depthFbo;
    private static SimpleFramebuffer tintFbo;
    private static SimpleFramebuffer reflectionFbo;
    private static SimpleFramebuffer colorGradingFbo;
    private static JumpCircleShader jumpCircleShader;


    private static boolean initialized = false;

    public static void initShadersIfNeeded() {
        if (initialized) return;
        try {
            gaussianShader = new GaussianShader();
            depthShader = new DepthShader();
            passThroughShader = new PassThroughShader();
            tintShader = new TintShader();
            reflectionShader = new ReflectionShader();
            colorGradingShader = new ColorGradingShader();
            jumpCircleShader = new JumpCircleShader();
            initialized = true;
        } catch (Exception e) {
            System.err.println("Failed to initialize shaders!");
            e.printStackTrace();
        }
    }

    public static void checkFramebuffers() {
        int width = mc.getWindow().getFramebufferWidth();
        int height = mc.getWindow().getFramebufferHeight();
        if (copyFbo == null || copyFbo.textureWidth != width || copyFbo.textureHeight != height) {
            if (copyFbo != null) {
                copyFbo.delete();
                fbo1.delete();
                fbo2.delete();
                depthFbo.delete();
                tintFbo.delete();
                reflectionFbo.delete();
                if (colorGradingFbo != null) colorGradingFbo.delete();
            }
            copyFbo = new SimpleFramebuffer(width, height, true);
            fbo1 = new SimpleFramebuffer(width, height, true);
            fbo2 = new SimpleFramebuffer(width, height, true);
            depthFbo = new SimpleFramebuffer(width, height, true);
            tintFbo = new SimpleFramebuffer(width, height, true);
            reflectionFbo = new SimpleFramebuffer(width, height, true);
            colorGradingFbo = new SimpleFramebuffer(width, height, true);
        }
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

    public static boolean isInitialized() {
        return initialized;
    }

    public static GaussianShader getGaussianShader() {
        return gaussianShader;
    }

    public static DepthShader getDepthShader() {
        return depthShader;
    }

    public static Shader getPassThroughShader() {
        return passThroughShader;
    }

    public static TintShader getTintShader() {
        return tintShader;
    }

    public static ReflectionShader getReflectionShader() {
        return reflectionShader;
    }
    public static ColorGradingShader getColorGradingShader() {
        return colorGradingShader;
    }

    public static SimpleFramebuffer getCopyFbo() {
        return copyFbo;
    }

    public static SimpleFramebuffer getFbo1() {
        return fbo1;
    }

    public static SimpleFramebuffer getFbo2() {
        return fbo2;
    }

    public static SimpleFramebuffer getDepthFbo() {
        return depthFbo;
    }

    public static SimpleFramebuffer getTintFbo() {
        return tintFbo;
    }

    public static SimpleFramebuffer getReflectionFbo() {
        return reflectionFbo;
    }
    public static JumpCircleShader getJumpCircleShader() {
        return jumpCircleShader;
    }

}