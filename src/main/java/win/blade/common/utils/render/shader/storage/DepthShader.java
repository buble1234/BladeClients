package win.blade.common.utils.render.shader.storage;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.SimpleFramebuffer;
import org.lwjgl.opengl.GL30;
import win.blade.common.utils.render.shader.Shader;
import win.blade.common.utils.render.shader.ShaderHelper;

public class DepthShader extends Shader {
    public DepthShader() throws Exception {
        super("effects", "depth");
    }

    public void apply(int blurredTexture, int depthTexture, float fogDistance, float farPlane, SimpleFramebuffer outFbo, boolean rainbow, boolean rainbowNoise, float rainbowStrength, float time, float yaw, float pitch) {
        outFbo.beginWrite(true);
        bind();
        setUniform1i("Tex0", 0);
        setUniform1i("Tex1", 1);
        setUniform1f("Near", 0.05f);
        setUniform1f("Far", farPlane);
        setUniform1f("MinThreshold", 0.10f * fogDistance / 100f);
        setUniform1f("MaxThreshold", 0.28f * fogDistance / 100f);

        setUniformBool("Rainbow", rainbow);
        setUniformBool("RainbowNoise", rainbowNoise);
        setUniform1f("RainbowStrength", rainbowStrength);
        setUniform1f("Time", time);
        setUniform1f("Yaw", yaw);
        setUniform1f("Pitch", pitch);


        RenderSystem.activeTexture(GL30.GL_TEXTURE0);
        RenderSystem.bindTexture(blurredTexture);
        RenderSystem.activeTexture(GL30.GL_TEXTURE1);
        RenderSystem.bindTexture(depthTexture);

        ShaderHelper.drawFullScreenQuad();

        unbind();
        RenderSystem.activeTexture(GL30.GL_TEXTURE0);
    }
}