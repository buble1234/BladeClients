package win.blade.common.utils.shader.framebuffers;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.PostEffectPass;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.client.gl.ShaderProgram;
import win.blade.common.utils.shader.ShaderManager;
import win.blade.mixin.accessor.GameRendererAccessor;
import win.blade.mixin.accessor.PostEffectProcessorAccessor;

import java.util.List;

public class MaskedBlurFramebuffer extends Framebuffer {
	private static MaskedBlurFramebuffer instance;

	private MaskedBlurFramebuffer(int width, int height) {
		super(false);
		RenderSystem.assertOnRenderThreadOrInit();
		this.resize(width, height);
		this.setClearColor(0f, 0f, 0f, 0f);
	}

	private static MaskedBlurFramebuffer obtain() {
		if (instance == null) {
			instance = new MaskedBlurFramebuffer(MinecraftClient.getInstance().getFramebuffer().textureWidth, MinecraftClient.getInstance().getFramebuffer().textureHeight);
		}
		return instance;
	}

	public static void use(Runnable r) {
		Framebuffer mainBuffer = MinecraftClient.getInstance().getFramebuffer();
		RenderSystem.assertOnRenderThreadOrInit();
		MaskedBlurFramebuffer buffer = obtain();
		if (buffer.textureWidth != mainBuffer.textureWidth || buffer.textureHeight != mainBuffer.textureHeight) {
			buffer.resize(mainBuffer.textureWidth, mainBuffer.textureHeight);
		}

		buffer.beginWrite(false);
		r.run();
		buffer.endWrite();

		mainBuffer.beginWrite(false);
	}

	public static void draw(int kernelSizePx, float sigma) {
		Framebuffer mainBuffer = MinecraftClient.getInstance().getFramebuffer();
		MaskedBlurFramebuffer buffer = obtain();

		PostEffectProcessor gaussianShader = ShaderManager.getGaussianShader();
		List<PostEffectPass> allPasses = ((PostEffectProcessorAccessor) gaussianShader).getPasses();
		PostEffectPass firstPass = allPasses.getFirst();
		ShaderProgram firstPassProgram = firstPass.getProgram();

		firstPassProgram.getUniform("sigma").set(sigma);
		firstPassProgram.getUniform("width").set(((float) kernelSizePx));
		firstPassProgram.addSamplerTexture("MaskSampler", buffer.colorAttachment);


		gaussianShader.render(mainBuffer, ((GameRendererAccessor) MinecraftClient.getInstance().gameRenderer).getPool());

		buffer.clear();

		mainBuffer.beginWrite(false);
	}
}