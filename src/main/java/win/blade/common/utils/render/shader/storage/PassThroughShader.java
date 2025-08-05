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
public class PassThroughShader extends Shader {
    public PassThroughShader() {
        super("effects", "passthrough");
    }

    public static void renderToScreen(Shader shader, SimpleFramebuffer fbo) {
        mc.getFramebuffer().beginWrite(false);
        shader.bind();
        shader.setUniform1i("Tex0", 0);
        shader.setUniformBool("Alpha", true);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        RenderSystem.bindTexture(fbo.getColorAttachment());
        drawFullScreenQuad();

        RenderSystem.disableBlend();
        shader.unbind();
    }
}