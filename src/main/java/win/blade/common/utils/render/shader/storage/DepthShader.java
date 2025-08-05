package win.blade.common.utils.render.shader.storage;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.SimpleFramebuffer;
import org.lwjgl.opengl.GL30;
import win.blade.common.utils.render.shader.Shader;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static win.blade.common.utils.minecraft.MinecraftInstance.mc;
import static win.blade.common.utils.render.shader.ShaderHelper.drawFullScreenQuad;

/**
 * Автор: NoCap
 * Дата создания: 05.08.2025
 */
public class DepthShader extends Shader {
    public DepthShader() {
        super("effects", "depth");
    }

    public static void applyDepthMask(Shader shader, SimpleFramebuffer fbo, SimpleFramebuffer fbo2, SimpleFramebuffer copyfbo, float fogDistance) {
        fbo.beginWrite(true);
        shader.bind();
        shader.setUniform1i("Tex0", 0);
        shader.setUniform1i("Tex1", 1);
        shader.setUniform1f("Near", 0.05f);
        shader.setUniform1f("Far", mc.gameRenderer.getFarPlaneDistance());
        shader.setUniform1f("MinThreshold", 0.10f * fogDistance / 100f);
        shader.setUniform1f("MaxThreshold", 0.28f * fogDistance / 100f);

        RenderSystem.activeTexture(GL30.GL_TEXTURE0);
        RenderSystem.bindTexture(fbo2.getColorAttachment());

        RenderSystem.activeTexture(GL30.GL_TEXTURE1);
        RenderSystem.bindTexture(copyfbo.getDepthAttachment());

        GlStateManager._texParameter(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        GlStateManager._texParameter(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        drawFullScreenQuad();
        shader.unbind();

        RenderSystem.activeTexture(GL30.GL_TEXTURE0);
    }
}