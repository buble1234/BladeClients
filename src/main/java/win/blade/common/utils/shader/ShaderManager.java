package win.blade.common.utils.shader;

import net.minecraft.client.gl.Defines;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.client.render.DefaultFramebufferSet;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static win.blade.common.utils.minecraft.MinecraftInstance.mc;

public class ShaderManager {
    public static final ShaderProgramKey SPK_POSITION_TEX_COLOR_NORMAL = new ShaderProgramKey(Identifier.of("blade", "core/normal_position_tex_color"), VertexFormats.POSITION_TEXTURE_COLOR_NORMAL, Defines.EMPTY);

    public static @NotNull PostEffectProcessor getGaussianShader() {
        return getShader("gaussian");
    }

    public static @NotNull PostEffectProcessor getOutlineShader() {
        return getShader("outline");
    }

    public static @NotNull PostEffectProcessor getShader(String shaderName) {
        return Objects.requireNonNull(mc.getShaderLoader().loadPostEffect(Identifier.of("blade", shaderName), DefaultFramebufferSet.MAIN_ONLY));
    }
}