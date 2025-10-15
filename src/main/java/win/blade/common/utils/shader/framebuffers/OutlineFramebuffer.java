//package win.blade.common.utils.shader.framebuffers;
//
//import com.mojang.blaze3d.systems.RenderSystem;
//import net.minecraft.client.MinecraftClient;
//import net.minecraft.client.gl.Framebuffer;
//import net.minecraft.client.gl.PostEffectPass;
//import net.minecraft.client.gl.PostEffectProcessor;
//import net.minecraft.client.gl.ShaderProgram;
//import win.blade.common.utils.shader.ShaderManager;
//import win.blade.mixin.accessor.GameRendererAccessor;
//import win.blade.mixin.accessor.PostEffectProcessorAccessor;
//
//import java.awt.*;
//import java.util.List;
//
//@Deprecated(since = "1.2.4")
//public class OutlineFramebuffer extends Framebuffer {
//	private static OutlineFramebuffer instance;
//
//	private OutlineFramebuffer(int width, int height) {
//		super(false);
//		RenderSystem.assertOnRenderThreadOrInit();
//		this.resize(width, height);
//		this.setClearColor(0f, 0f, 0f, 0f);
//	}
//
//	private static OutlineFramebuffer obtain() {
//		if (instance == null) {
//			instance = new OutlineFramebuffer(MinecraftClient.getInstance().getFramebuffer().textureWidth, MinecraftClient.getInstance().getFramebuffer().textureHeight);
//		}
//		return instance;
//	}
//
//	public static void use(Runnable r) {
//		Framebuffer mainBuffer = MinecraftClient.getInstance().getFramebuffer();
//		RenderSystem.assertOnRenderThreadOrInit();
//		OutlineFramebuffer buffer = obtain();
//		if (buffer.textureWidth != mainBuffer.textureWidth || buffer.textureHeight != mainBuffer.textureHeight) {
//			buffer.resize(mainBuffer.textureWidth, mainBuffer.textureHeight);
//		}
//
//		buffer.beginWrite(false);
//		r.run();
//		buffer.endWrite();
//
//		mainBuffer.beginWrite(false);
//	}
//
//	public static void draw(float radius, Color outlineColor, Color innerColor) {
//		Framebuffer mainBuffer = MinecraftClient.getInstance().getFramebuffer();
//		OutlineFramebuffer buffer = obtain();
//
//		PostEffectProcessor outlineShader = ShaderManager.getOutlineShader();
//		List<PostEffectPass> allPasses = ((PostEffectProcessorAccessor) outlineShader).getPasses();
//		PostEffectPass firstPass = allPasses.getFirst();
//		ShaderProgram firstPassProgram = firstPass.getProgram();
//
//		firstPassProgram.addSamplerTexture("MaskSampler", buffer.colorAttachment);
//		firstPassProgram.getUniform("Radius").set(radius);
//		firstPassProgram.getUniform("OutlineColor").set(outlineColor.getRed() / 255f, outlineColor.getGreen() / 255f, outlineColor.getBlue() / 255f, outlineColor.getAlpha() / 255f);
//		firstPassProgram.getUniform("InnerColor").set(innerColor.getRed() / 255f, innerColor.getGreen() / 255f, innerColor.getBlue() / 255f, innerColor.getAlpha() / 255f);
//
//		RenderSystem.depthMask(false);
//		outlineShader.render(mainBuffer, ((GameRendererAccessor) MinecraftClient.getInstance().gameRenderer).getPool());
//		RenderSystem.depthMask(true);
//
//		buffer.clear();
//
//		mainBuffer.beginWrite(false);
//	}
//
//	public static void useAndDraw(Runnable r, float radius, Color outline, Color inner) {
//		use(r);
//		draw(radius, outline, inner);
//	}
//}