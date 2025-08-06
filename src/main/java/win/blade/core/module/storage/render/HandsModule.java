package win.blade.core.module.storage.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.SimpleFramebuffer;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import win.blade.common.gui.impl.gui.setting.implement.BooleanSetting;
import win.blade.common.gui.impl.gui.setting.implement.SelectSetting;
import win.blade.common.gui.impl.gui.setting.implement.ValueSetting;
import win.blade.common.utils.render.shader.Shader;
import win.blade.common.utils.render.shader.ShaderHelper;
import win.blade.common.utils.render.shader.storage.*;
import win.blade.core.module.api.Category;
import win.blade.core.module.api.Module;
import win.blade.core.module.api.ModuleInfo;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL13C.*;

/**
 * Автор: NoCap
 * Дата создания: 06.08.2025
 */
@ModuleInfo(
        name = "Hands",
        category = Category.RENDER,
        bind = GLFW.GLFW_KEY_G
)
public class HandsModule extends Module {

    public static final SelectSetting shaderType = new SelectSetting("Шейдер", "Тип шейдера для рук").value("Сплошной", "Размытый");
    public static final ValueSetting blurStrength = new ValueSetting("Сила размытия", "").range(2, 20).visible(() -> shaderType.isSelected("Размытый"));
    public static final BooleanSetting fogRGBPuke = new BooleanSetting("Fog RGB Puke", "Добавляет радужный эффект к туману.").setValue(false).visible(() -> shaderType.isSelected("Размытый"));
    public static final ValueSetting fogRGBPukeOpacity = new ValueSetting("RGB прозр.", "Прозрачность радужного эффекта.").setValue(30f).range(1f, 100f).visible(() -> fogRGBPuke.getValue());
    public static final ValueSetting fogRGBPukeSaturation = new ValueSetting("RGB насыщенность", "Насыщенность радужного эффекта.").setValue(70f).range(0f, 100f).visible(() -> fogRGBPuke.getValue());
    public static final ValueSetting fogRGBPukeBrightness = new ValueSetting("RGB яркость", "Яркость радужного эффекта.").setValue(100f).range(0f, 100f).visible(() -> fogRGBPuke.getValue());


    public HandsModule() {
        addSettings(shaderType, blurStrength, fogRGBPuke, fogRGBPukeOpacity, fogRGBPukeSaturation, fogRGBPukeBrightness);
    }

    public static void render(float FarPlaneDistance) {
        if (!ShaderHelper.isInitialized()) return;

        if (shaderType.isSelected("Сплошной")) {
            renderNormal(FarPlaneDistance);
        } else if (shaderType.isSelected("Размытый")) {
            renderBlur(blurStrength.getValue(), FarPlaneDistance);
        }
    }


    private static void renderNormal(float FarPlaneDistance) {
        HandShader handShader = ShaderHelper.getHandShader();
        Framebuffer mainFbo = mc.getFramebuffer();
        if (handShader == null || mainFbo == null) return;

        RenderSystem.activeTexture(GL_TEXTURE0);
        RenderSystem.bindTexture(mainFbo.getColorAttachment());
        GlStateManager._texParameter(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        GlStateManager._texParameter(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        RenderSystem.activeTexture(GL_TEXTURE1);
        RenderSystem.bindTexture(mainFbo.getDepthAttachment());
        GlStateManager._texParameter(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        GlStateManager._texParameter(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        handShader.bind();
        handShader.setUniform1i("ColorTexture", 0);
        handShader.setUniform1i("DepthTexture", 1);
        handShader.setUniform1f("time", (float) (System.currentTimeMillis() % 2000L) / 2000.0f);
        handShader.setUniform3f("customColor", new Vector3f(0.0f, 1.0f, 0.0f));
        handShader.setUniform1f("effectAlpha", 1f);
        handShader.setUniform1f("nearPlane", 0.05f);
        handShader.setUniform1f("farPlane", FarPlaneDistance);

        ShaderHelper.drawFullScreenQuad();

        handShader.unbind();

        RenderSystem.activeTexture(GL_TEXTURE0);
        RenderSystem.bindTexture(mainFbo.getColorAttachment());
        GlStateManager._texParameter(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        GlStateManager._texParameter(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        RenderSystem.activeTexture(GL_TEXTURE1);
        RenderSystem.bindTexture(mainFbo.getDepthAttachment());
        GlStateManager._texParameter(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        GlStateManager._texParameter(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        RenderSystem.activeTexture(GL_TEXTURE0);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    private static void renderBlur(float strength, float FarPlaneDistance) {
        Framebuffer mainFbo = mc.getFramebuffer();
        BlurredShader blurHandShader = ShaderHelper.getBlurredShader();
        GaussianShader gaussianShader = ShaderHelper.getGaussianShader();
        SimpleFramebuffer copyFbo = ShaderHelper.getCopyFbo();
        SimpleFramebuffer fbo1 = ShaderHelper.getFbo1();
        SimpleFramebuffer fbo2 = ShaderHelper.getFbo2();
        Shader tintShader = ShaderHelper.getTintShader();
        SimpleFramebuffer tintFbo = ShaderHelper.getTintFbo();

        if (mainFbo == null || blurHandShader == null || gaussianShader == null || copyFbo == null || fbo1 == null || fbo2 == null) {
            return;
        }

        copyFbo.beginWrite(true);
        mainFbo.draw(copyFbo.textureWidth, copyFbo.textureHeight);

        if (fogRGBPuke.getValue()) {
            TintShader.applyTintPass(tintShader, tintFbo, copyFbo, fogRGBPukeOpacity.getValue() / 100.0f, fogRGBPukeSaturation.getValue() / 100.0f, fogRGBPukeBrightness.getValue() / 100.0f);
        }

        GaussianShader.applyGaussianBlur(gaussianShader, fbo1, fbo2, tintFbo, copyFbo, strength, true, fogRGBPuke.getValue());

        mainFbo.beginWrite(true);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();

        RenderSystem.activeTexture(GL_TEXTURE0);
        RenderSystem.bindTexture(mainFbo.getColorAttachment());
        RenderSystem.texParameter(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        RenderSystem.texParameter(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        RenderSystem.activeTexture(GL_TEXTURE1);
        RenderSystem.bindTexture(fbo2.getColorAttachment());

        RenderSystem.activeTexture(GL_TEXTURE2);
        RenderSystem.bindTexture(mainFbo.getDepthAttachment());

        blurHandShader.bind();
        blurHandShader.setUniform1i("OriginalTexture", 0);
        blurHandShader.setUniform1i("BlurredTexture", 1);
        blurHandShader.setUniform1i("DepthTexture", 2);
        blurHandShader.setUniform1f("nearPlane", 0.05f);
        blurHandShader.setUniform1f("farPlane", FarPlaneDistance);

        ShaderHelper.drawFullScreenQuad();
        blurHandShader.unbind();

        RenderSystem.activeTexture(GL_TEXTURE0);
        RenderSystem.bindTexture(mainFbo.getColorAttachment());
        RenderSystem.texParameter(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        RenderSystem.texParameter(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        RenderSystem.activeTexture(GL_TEXTURE2);
        RenderSystem.bindTexture(0);
        RenderSystem.activeTexture(GL_TEXTURE1);
        RenderSystem.bindTexture(0);
        RenderSystem.activeTexture(GL_TEXTURE0);
        RenderSystem.bindTexture(0);

        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();

        if (mc.getFramebuffer() != null) {
            mc.getFramebuffer().beginWrite(true);
        }
    }
}