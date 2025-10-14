package win.blade.common.utils.shader.framebuffers;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import org.lwjgl.opengl.GL30C;
import win.blade.common.utils.shader.ShaderManager;
import win.blade.common.utils.shader.ShaderTarget;

public class OutlineFramebuffer extends Framebuffer {
    private static OutlineFramebuffer instance;

    private OutlineFramebuffer(int width, int height) {
        super(false);
        RenderSystem.assertOnRenderThreadOrInit();
        this.resize(width, height, true);
        this.setClearColor(0f, 0f, 0f, 0f);
    }

    private static OutlineFramebuffer obtain() {
        if (instance == null) {
            instance = new OutlineFramebuffer(
                MinecraftClient.getInstance().getFramebuffer().textureWidth,
                MinecraftClient.getInstance().getFramebuffer().textureHeight
            );
        }
        return instance;
    }

    public static void use(Runnable r) {
        Framebuffer mainBuffer = MinecraftClient.getInstance().getFramebuffer();
        RenderSystem.assertOnRenderThreadOrInit();
        OutlineFramebuffer buffer = obtain();

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

    public static void draw(float thickness, float r, float g, float b, float a) {
        Framebuffer mainBuffer = MinecraftClient.getInstance().getFramebuffer();
        OutlineFramebuffer buffer = obtain();

        ((ShaderTarget) ShaderManager.OUTLINE_SHADER.getShader()).addFakeTarget("outlineFbo", buffer);
        ShaderManager.OUTLINE_SHADER.setUniformSampler("vanilla", mainBuffer);
        ShaderManager.OUTLINE_SHADER.setUniformf("thickness", thickness);
        ShaderManager.OUTLINE_SHADER.setUniform4f("outlineColor", r, g, b, a);
        ShaderManager.OUTLINE_SHADER.render(MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(false));

        GlStateManager._glBindFramebuffer(GL30C.GL_DRAW_FRAMEBUFFER, buffer.fbo);
        buffer.clear(true);
        GlStateManager._glBindFramebuffer(GL30C.GL_DRAW_FRAMEBUFFER, mainBuffer.fbo);
        mainBuffer.beginWrite(true);
    }

    public static void draw(float thickness) {
        draw(thickness, 1f, 1f, 1f, 1f);
    }

    public static void useAndDraw(Runnable r, float thickness, float red, float green, float blue, float alpha) {
        use(r);
        draw(thickness, red, green, blue, alpha);
    }

    public static void useAndDraw(Runnable r, float thickness) {
        use(r);
        draw(thickness);
    }
}