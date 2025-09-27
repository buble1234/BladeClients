package win.blade.core.module.storage.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.SimpleFramebuffer;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import win.blade.common.gui.impl.gui.setting.implement.ColorSetting;
import win.blade.common.gui.impl.gui.setting.implement.SelectSetting;
import win.blade.common.gui.impl.gui.setting.implement.ValueSetting;
import win.blade.common.utils.render.shader.ShaderHelper;
import win.blade.common.utils.render.shader.storage.BlurredShader;
import win.blade.common.utils.render.shader.storage.GaussianShader;
import win.blade.common.utils.render.shader.storage.LiquidGlassShader;
import win.blade.common.utils.render.shader.storage.SolidShader;
import win.blade.core.module.api.Category;
import win.blade.core.module.api.Module;
import win.blade.core.module.api.ModuleInfo;

import java.awt.*;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL13C.*;

/**
 * Автор: NoCap
 * Дата создания: 06.08.2025
 * Обновлено: Liquid Glass - искажение относительно центра предмета (локальный центр по глубине).
 */
@ModuleInfo(
        name = "Hands",
        category = Category.RENDER,
        desc = "Применяет эффекты шейдеров к рукам и предметам.",
        bind = GLFW.GLFW_KEY_G
)
public class HandsModule extends Module {

    public static final SelectSetting mode = new SelectSetting("Режим", "").value("Свой цвет", "Прозрачный", "Liquid Glass");
    public static final ColorSetting solidColor = new ColorSetting("Первый цвет", "Первый цвет для градиента.").value(new Color(0, 255, 0).getRGB()).visible(() -> mode.isSelected("Свой цвет"));
    public static final ColorSetting solidColor2 = new ColorSetting("Второй цвет", "Второй цвет для градиента.").value(new Color(255, 0, 0).getRGB()).visible(() -> mode.isSelected("Свой цвет"));
    public static final ValueSetting gradientSpeed = new ValueSetting("Скорость", "Скорость анимации градиента.").setValue(0.5f).range(0.1f, 1.0f).visible(() -> mode.isSelected("Свой цвет"));
    public static final ValueSetting blurStrength = new ValueSetting("Сила размытия", "Сила размытия для руки.").range(2, 20);

    public HandsModule() {
        addSettings(mode, solidColor, solidColor2, gradientSpeed, blurStrength);
    }

    public static void render(float FarPlaneDistance) {
        if (!ShaderHelper.isInitialized()) return;
        renderCombined(FarPlaneDistance);
    }

    private static void renderCombined(float FarPlaneDistance) {
        Framebuffer mainFbo = mc.getFramebuffer();
        SolidShader handShader = ShaderHelper.getSolidShader();
        LiquidGlassShader liquidShader = ShaderHelper.getLiquidGlassShader();
        BlurredShader blurHandShader = ShaderHelper.getBlurredShader();
        GaussianShader gaussianShader = ShaderHelper.getGaussianShader();
        SimpleFramebuffer copyFbo = ShaderHelper.getCopyFbo();
        SimpleFramebuffer fbo1 = ShaderHelper.getFbo1();
        SimpleFramebuffer fbo2 = ShaderHelper.getFbo2();
        SimpleFramebuffer solidFbo = ShaderHelper.getTintFbo();

        if (mainFbo == null || handShader == null || liquidShader == null || blurHandShader == null || gaussianShader == null || copyFbo == null || fbo1 == null || fbo2 == null || solidFbo == null) {
            return;
        }

        copyFbo.beginWrite(true);
        mainFbo.draw(copyFbo.textureWidth, copyFbo.textureHeight);
        copyFbo.copyDepthFrom(mainFbo);

        SimpleFramebuffer fboToBlur = copyFbo;

        if (mode.isSelected("Свой цвет")) {
            solidFbo.beginWrite(true);

            RenderSystem.activeTexture(GL_TEXTURE0);
            RenderSystem.bindTexture(copyFbo.getColorAttachment());
            GlStateManager._texParameter(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            GlStateManager._texParameter(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

            RenderSystem.activeTexture(GL_TEXTURE1);
            RenderSystem.bindTexture(copyFbo.getDepthAttachment());
            GlStateManager._texParameter(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            GlStateManager._texParameter(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

            RenderSystem.disableDepthTest();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();

            handShader.bind();
            handShader.setUniform1i("ColorTexture", 0);
            handShader.setUniform1i("DepthTexture", 1);
            float gameTime = (System.nanoTime() / 1_000_000_000.0f) * gradientSpeed.getValue();
            handShader.setUniform1f("time", gameTime);

            Color color1 = new Color(solidColor.getColor());
            Vector3f customColor1 = new Vector3f(
                    color1.getRed() / 255.0f,
                    color1.getGreen() / 255.0f,
                    color1.getBlue() / 255.0f
            );
            handShader.setUniform3f("customColor1", customColor1);

            Color color2 = new Color(solidColor2.getColor());
            Vector3f customColor2 = new Vector3f(
                    color2.getRed() / 255.0f,
                    color2.getGreen() / 255.0f,
                    color2.getBlue() / 255.0f
            );
            handShader.setUniform3f("customColor2", customColor2);

            handShader.setUniform1f("effectAlpha", Math.max(solidColor.getAlpha(), solidColor2.getAlpha()));
            handShader.setUniform1f("nearPlane", 0.05f);
            handShader.setUniform1f("farPlane", FarPlaneDistance);

            ShaderHelper.drawFullScreenQuad();

            handShader.unbind();

            RenderSystem.activeTexture(GL_TEXTURE0);
            RenderSystem.bindTexture(copyFbo.getColorAttachment());
            GlStateManager._texParameter(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            GlStateManager._texParameter(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

            RenderSystem.activeTexture(GL_TEXTURE1);
            RenderSystem.bindTexture(copyFbo.getDepthAttachment());
            GlStateManager._texParameter(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            GlStateManager._texParameter(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

            RenderSystem.activeTexture(GL_TEXTURE0);
            RenderSystem.enableDepthTest();
            RenderSystem.disableBlend();

            fboToBlur = solidFbo;
        } else if (mode.isSelected("Liquid Glass")) {
            solidFbo.beginWrite(true); // Reuse solidFbo as liquidFbo

            RenderSystem.activeTexture(GL_TEXTURE0);
            RenderSystem.bindTexture(copyFbo.getColorAttachment());
            GlStateManager._texParameter(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            GlStateManager._texParameter(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

            RenderSystem.activeTexture(GL_TEXTURE1);
            RenderSystem.bindTexture(copyFbo.getDepthAttachment());
            GlStateManager._texParameter(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            GlStateManager._texParameter(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

            RenderSystem.disableDepthTest();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();

            liquidShader.bind();
            liquidShader.setUniform1i("ColorTexture", 0);
            liquidShader.setUniform1i("DepthTexture", 1);
            liquidShader.setUniform2f("iResolution", (float) mc.getWindow().getFramebufferWidth(), (float) mc.getWindow().getFramebufferHeight());

            // Fixed values as in the example
            liquidShader.setUniform1f("BlurSize", 20.0f);
            liquidShader.setUniform1f("Quality", 10.0f);
            liquidShader.setUniform1f("Direction", 10.0f);
            liquidShader.setUniform1f("nearPlane", 0.05f);
            liquidShader.setUniform1f("farPlane", FarPlaneDistance);

            ShaderHelper.drawFullScreenQuad();

            liquidShader.unbind();

            RenderSystem.activeTexture(GL_TEXTURE0);
            RenderSystem.bindTexture(copyFbo.getColorAttachment());
            GlStateManager._texParameter(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            GlStateManager._texParameter(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

            RenderSystem.activeTexture(GL_TEXTURE1);
            RenderSystem.bindTexture(copyFbo.getDepthAttachment());
            GlStateManager._texParameter(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            GlStateManager._texParameter(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

            RenderSystem.activeTexture(GL_TEXTURE0);
            RenderSystem.enableDepthTest();
            RenderSystem.disableBlend();

            fboToBlur = solidFbo;
        }

        GaussianShader.applyGaussianBlur(gaussianShader, fbo1, fbo2, fboToBlur, copyFbo, blurStrength.getValue(), true, true);

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