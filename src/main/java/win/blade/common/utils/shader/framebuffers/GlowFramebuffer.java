package win.blade.common.utils.shader.framebuffers;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import org.lwjgl.opengl.GL30C;
import win.blade.common.utils.shader.ShaderManager;
import win.blade.common.utils.shader.ShaderTarget;

public class GlowFramebuffer extends Framebuffer {
    private static GlowFramebuffer instance;

    private GlowFramebuffer(int width, int height) {
        super(false);
        RenderSystem.assertOnRenderThreadOrInit();
        this.resize(width, height, true);
        this.setClearColor(0f, 0f, 0f, 0f);
    }

    private static GlowFramebuffer obtain() {
        if (instance == null) {
            instance = new GlowFramebuffer(MinecraftClient.getInstance().getFramebuffer().textureWidth, MinecraftClient.getInstance().getFramebuffer().textureHeight);
        }
        return instance;
    }

    public static void use(Runnable r) {
        Framebuffer mainBuffer = MinecraftClient.getInstance().getFramebuffer();
        RenderSystem.assertOnRenderThreadOrInit();
        GlowFramebuffer buffer = obtain();
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
        Framebuffer mainBuffer = MinecraftClient.getInstance().getFramebuffer();
        GlowFramebuffer buffer = obtain();
        ((ShaderTarget) ShaderManager.GLOW_SHADER.getShader()).addFakeTarget("glowFbo", buffer);
        ShaderManager.GLOW_SHADER.setUniformSampler("vanilla", mainBuffer);
        ShaderManager.GLOW_SHADER.setUniformf("radius", radius);
        ShaderManager.GLOW_SHADER.render(MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(false));
        GlStateManager._glBindFramebuffer(GL30C.GL_DRAW_FRAMEBUFFER, buffer.fbo);
        buffer.clear(true);
        GlStateManager._glBindFramebuffer(GL30C.GL_DRAW_FRAMEBUFFER, mainBuffer.fbo);
        mainBuffer.beginWrite(true);
    }

    public static void useAndDraw(Runnable r, float radius) {
        use(r);
        draw(radius);
    }
}