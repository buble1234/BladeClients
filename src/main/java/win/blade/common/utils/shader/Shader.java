package win.blade.common.utils.shader;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.PostEffectPass;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.util.Identifier;
import org.lwjgl.opengl.GL11;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import static win.blade.common.utils.minecraft.MinecraftInstance.mc;

public class Shader {
    final PostEffectProcessor shader;
    int previousWidth, previousHeight;

    private Shader(Identifier ident, Consumer<Shader> init) {
        try {
            this.shader = new PostEffectProcessor(mc.getTextureManager(), mc.getResourceManager(), mc.getFramebuffer(), ident);
            checkUpdateDimensions();
            init.accept(this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Shader create(String progName, Consumer<Shader> callback) {
        return new Shader(new Identifier("taksa", String.format("shaders/post/%s.json", progName)), callback);
    }

    public PostEffectProcessor getShader() {
        return shader;
    }

    void checkUpdateDimensions() {
        int currentWidth = mc.getWindow().getFramebufferWidth();
        int currentHeight = mc.getWindow().getFramebufferHeight();
        if (previousWidth != currentWidth || previousHeight != currentHeight) {
            this.shader.setupDimensions(currentWidth, currentHeight);
            previousWidth = currentWidth;
            previousHeight = currentHeight;
        }
    }

    public void setUniformf(String name, float value) {
        List<PostEffectPass> passes = ((ShaderTarget) shader).getPasses();
        passes.stream().map(PostEffectPass -> PostEffectPass.getProgram().getUniformByName(name)).filter(Objects::nonNull).forEach(glUniform -> glUniform.set(value));
    }

    public void setUniformi(String name, int value) {
        List<PostEffectPass> passes = ((ShaderTarget) shader).getPasses();
        passes.stream()
                .map(PostEffectPass -> PostEffectPass.getProgram().getUniformByName(name))
                .filter(Objects::nonNull)
                .forEach(glUniform -> glUniform.set(value));
    }

    public void setUniform2f(String name, float v1, float v2) {
        List<PostEffectPass> passes = ((ShaderTarget) shader).getPasses();
        passes.stream()
                .map(PostEffectPass -> PostEffectPass.getProgram().getUniformByName(name))
                .filter(Objects::nonNull)
                .forEach(glUniform -> glUniform.set(v1, v2));
    }

    public void setUniform4f(String name, float v1, float v2, float v3, float v4) {
        List<PostEffectPass> passes = ((ShaderTarget) shader).getPasses();
        passes.stream().map(PostEffectPass -> PostEffectPass.getProgram().getUniformByName(name)).filter(Objects::nonNull).forEach(glUniform -> glUniform.set(v1, v2, v3, v4));
    }

    public void setUniformSampler(String name, Framebuffer framebuffer) {
        List<PostEffectPass> passes = ((ShaderTarget) shader).getPasses();
        for (PostEffectPass pass : passes) {
            pass.getProgram().bindSampler(name, framebuffer::getColorAttachment);
        }
    }

    public void render(float delta) {
        checkUpdateDimensions();
        RenderSystem.disableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.enableTexture();
        RenderSystem.resetTextureMatrix();
        shader.render(delta);
        MinecraftClient.getInstance().getFramebuffer().beginWrite(true);
        RenderSystem.disableBlend();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        RenderSystem.enableDepthTest();
    }
}