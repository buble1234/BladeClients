package win.blade.common.utils.shader.framebuffers;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.Framebuffer;
import org.lwjgl.opengl.GL30C;
import win.blade.common.utils.shader.ShaderManager;

import static win.blade.common.utils.minecraft.MinecraftInstance.mc;

public class BlurMaskFramebuffer extends Framebuffer {
    private static BlurMaskFramebuffer instance;

    private BlurMaskFramebuffer(int width, int height) {
        super(false);
        RenderSystem.assertOnRenderThreadOrInit();
        this.resize(width, height, true);
        this.setClearColor(0f, 0f, 0f, 0f);
    }

    public static BlurMaskFramebuffer getInstance() {
        if (instance == null) {
            instance = new BlurMaskFramebuffer(mc.getFramebuffer().textureWidth, mc.getFramebuffer().textureHeight);
        }
        return instance;
    }

    public static void use(Runnable r) {
        Framebuffer mainBuffer = mc.getFramebuffer();
        RenderSystem.assertOnRenderThreadOrInit();
        BlurMaskFramebuffer buffer = getInstance();
        if (buffer.textureWidth != mainBuffer.textureWidth || buffer.textureHeight != mainBuffer.textureHeight) {
            buffer.resize(mainBuffer.textureWidth, mainBuffer.textureHeight, false);
        }

        GlStateManager._glBindFramebuffer(GL30C.GL_DRAW_FRAMEBUFFER, buffer.fbo);

        buffer.beginWrite(true);
        r.run();
        buffer.endWrite();

        GlStateManager._glBindFramebuffer(GL30C.GL_DRAW_FRAMEBUFFER, mainBuffer.fbo);

        mainBuffer.beginWrite(false);
    }

    public static void draw(float radius) {
        draw(radius, 1f, 1f, 1f, 1f);
    }

    public static void draw(float radius, float r, float g, float b, float a) {
        Framebuffer mainBuffer = mc.getFramebuffer();
        BlurMaskFramebuffer buffer = getInstance();
        ShaderManager.BLUR_MASK_SHADER.setUniformSampler("MaskSampler", buffer);
        ShaderManager.BLUR_MASK_SHADER.setUniformf("Radius", radius);
        ShaderManager.BLUR_MASK_SHADER.setUniform4f("BlurColor", r, g, b, a);
        ShaderManager.BLUR_MASK_SHADER.render(mc.getRenderTickCounter().getTickDelta(false));
        GlStateManager._glBindFramebuffer(GL30C.GL_DRAW_FRAMEBUFFER, buffer.fbo);
        buffer.clear(true);
        GlStateManager._glBindFramebuffer(GL30C.GL_DRAW_FRAMEBUFFER, mainBuffer.fbo);
        mainBuffer.beginWrite(true);
    }

    public static void useAndDraw(Runnable r, float radius) {
        useAndDraw(r, radius, 1f, 1f, 1f, 1f);
    }

    public static void useAndDraw(Runnable r, float radius, float red, float green, float blue, float alpha) {
        use(r);
        draw(radius, red, green, blue, alpha);
    }
}