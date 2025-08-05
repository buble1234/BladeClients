package win.blade.common.utils.render.shader.storage;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.SimpleFramebuffer;
import win.blade.common.utils.render.shader.Shader;

import static win.blade.common.utils.minecraft.MinecraftInstance.mc;
import static win.blade.common.utils.render.shader.ShaderHelper.drawFullScreenQuad;

/**
 * Автор: NoCap
 * Дата создания: 05.08.2025
 */
public class TintShader extends Shader {

    private static long initTime = 0L;

    public TintShader() {
        super("effects", "tint");
        initTime = System.nanoTime();
    }

    public static void applyTintPass(Shader shader, SimpleFramebuffer fbo, SimpleFramebuffer copyfbo, float opacity, float saturation, float brightness) {
        fbo.beginWrite(true);
        shader.bind();
        shader.setUniform1i("Tex0", 0);
        shader.setUniformBool("RGBPuke", true);
        shader.setUniform2f("SV", saturation, brightness);
        shader.setUniform1f("Opacity", opacity);
        shader.setUniform1f("Time", (System.nanoTime() - initTime) / 1_000_000_000.0f);
        if (mc.player != null) {
            shader.setUniform1f("Yaw", mc.player.getYaw());
            shader.setUniform1f("Pitch", mc.player.getPitch());
        }
        RenderSystem.bindTexture(copyfbo.getColorAttachment());
        drawFullScreenQuad();
        shader.unbind();
    }
}