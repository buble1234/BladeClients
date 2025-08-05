package win.blade.common.utils.render.shader.storage;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.SimpleFramebuffer;
import win.blade.common.utils.render.shader.Shader;
import win.blade.common.utils.render.shader.ShaderHelper;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.GL_DRAW_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.GL_READ_FRAMEBUFFER;
import static win.blade.common.utils.minecraft.MinecraftInstance.mc;
import static win.blade.common.utils.render.shader.ShaderHelper.checkFramebuffers;
import static win.blade.common.utils.render.shader.ShaderHelper.initShadersIfNeeded;

/**
 * Автор: NoCap
 * Дата создания: 05.08.2025
 */
public class FogBlurRender {

    public static void applyFogBlur(float strength, float distance, boolean linearSampling, boolean usePuke, float pukeOpacity, float pukeSaturation, float pukeBrightness) {
        initShadersIfNeeded();
        if (!ShaderHelper.isInitialized()) return;

        try {
            checkFramebuffers();

            Shader tintShader = ShaderHelper.getTintShader();
            Shader gaussianShader = ShaderHelper.getGaussianShader();
            Shader depthShader = ShaderHelper.getDepthShader();
            Shader passThroughShader = ShaderHelper.getPassThroughShader();
            SimpleFramebuffer tintFbo = ShaderHelper.getTintFbo();
            SimpleFramebuffer copyFbo = ShaderHelper.getCopyFbo();
            SimpleFramebuffer fbo1 = ShaderHelper.getFbo1();
            SimpleFramebuffer fbo2 = ShaderHelper.getFbo2();
            SimpleFramebuffer depthFbo = ShaderHelper.getDepthFbo();

            Framebuffer mainFbo = mc.getFramebuffer();

            GlStateManager._glBindFramebuffer(GL_READ_FRAMEBUFFER, mainFbo.fbo);
            GlStateManager._glBindFramebuffer(GL_DRAW_FRAMEBUFFER, copyFbo.fbo);
            GlStateManager._glBlitFrameBuffer(
                    0, 0, mainFbo.textureWidth, mainFbo.textureHeight,
                    0, 0, copyFbo.textureWidth, copyFbo.textureHeight,
                    GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT, GL_NEAREST
            );

            if (usePuke) {
                TintShader.applyTintPass(tintShader, tintFbo, copyFbo, pukeOpacity, pukeSaturation, pukeBrightness);
            }

            GaussianShader.applyGaussianBlur(gaussianShader, fbo1, fbo2, tintFbo, copyFbo, strength, linearSampling, usePuke);
            DepthShader.applyDepthMask(depthShader, depthFbo, fbo2, copyFbo, distance);
            PassThroughShader.renderToScreen(passThroughShader, depthFbo);

        } finally {
            mc.getFramebuffer().beginWrite(false);
        }
    }
}