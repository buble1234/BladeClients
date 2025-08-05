package win.blade.common.utils.render.shader.storage;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.SimpleFramebuffer;
import org.joml.Vector3f;
import win.blade.common.utils.render.shader.Shader;
import win.blade.common.utils.render.shader.ShaderHelper;

/**
 * Автор: NoCap
 * Дата создания: 05.08.2025
 */
public class GaussianShader extends Shader {
    public GaussianShader() throws Exception {
        super("blurs", "gaussian");
    }

    public void apply(int inputTexture, float strength, boolean linearSampling, SimpleFramebuffer fbo1, SimpleFramebuffer fbo2) {
        Vector3f gaussian = ShaderHelper.calculateGaussian(strength);
        int support = (int) Math.ceil(strength * 3);

        bind();
        setUniform1i("Tex0", 0);
        setUniformBool("Alpha", false);
        setUniform3f("Gaussian", gaussian);
        setUniform1i("Support", support);
        setUniformBool("LinearSampling", linearSampling);

        fbo1.beginWrite(true);
        setUniform2f("Direction", 1.0f, 0.0f);
        setUniform2f("TexelSize", 1.0f / fbo1.textureWidth, 1.0f / fbo1.textureHeight);
        RenderSystem.bindTexture(inputTexture);
        ShaderHelper.drawFullScreenQuad();

        fbo2.beginWrite(true);
        setUniform2f("Direction", 0.0f, 1.0f);
        setUniform2f("TexelSize", 1.0f / fbo2.textureWidth, 1.0f / fbo2.textureHeight);
        RenderSystem.bindTexture(fbo1.getColorAttachment());
        ShaderHelper.drawFullScreenQuad();

        unbind();
    }
}