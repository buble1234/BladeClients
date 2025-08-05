package win.blade.common.utils.render.shader.storage;

import com.mojang.blaze3d.systems.RenderSystem;
import win.blade.common.utils.render.shader.Shader;
import win.blade.common.utils.render.shader.ShaderHelper;

/**
 * Автор: NoCap
 * Дата создания: 05.08.2025
 */
public class PassThroughShader extends Shader {
    public PassThroughShader() throws Exception {
        super("effects", "passthrough");
    }

    public void apply(int inputTexture) {
        bind();
        setUniform1i("Tex0", 0);
        setUniformBool("Alpha", true);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.bindTexture(inputTexture);

        ShaderHelper.drawFullScreenQuad();

        RenderSystem.disableBlend();
        unbind();
    }
}