package win.blade.core.module.storage.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;
import win.blade.common.gui.impl.gui.setting.implement.BooleanSetting;
import win.blade.common.gui.impl.gui.setting.implement.ColorSetting;
import win.blade.common.gui.impl.gui.setting.implement.MultiSelectSetting;
import win.blade.common.gui.impl.gui.setting.implement.ValueSetting;
import win.blade.common.utils.render.shader.ShaderHelper;
import win.blade.common.utils.render.shader.storage.ColorGradingShader;
import win.blade.core.event.controllers.EventHandler;
import win.blade.core.event.impl.minecraft.UpdateEvents;
import win.blade.core.event.impl.network.PacketEvent;
import win.blade.core.event.impl.render.FogEvent;
import win.blade.core.event.impl.render.RenderEvents;
import win.blade.core.module.api.Category;
import win.blade.core.module.api.Module;
import win.blade.core.module.api.ModuleInfo;

import java.awt.Color;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL30.GL_DRAW_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.GL_READ_FRAMEBUFFER;

@ModuleInfo(name = "CustomWorld", category = Category.RENDER, desc = "Изменяет время и цвета в мире.")
public class CustomWorld extends Module {


    public final MultiSelectSetting modeSetting = new MultiSelectSetting("World Setting", "Allows you to customize world")
            .value("Свое время", "Цветокоррекция","Туман");


    public ValueSetting time = new ValueSetting("Время", "Выбор времени суток.").range(0, 24000).setValue(16000).visible(() -> modeSetting.isSelected("Свое время"));


    private final ValueSetting brightness = new ValueSetting("Яркость", "Общая яркость.").range(-100, 100).setValue(-50f).visible(() -> modeSetting.isSelected("Цветокоррекция"));
    private final ValueSetting contrast = new ValueSetting("Контраст", "Контрастность изображения.").range(0, 200).setValue(55f).visible(() -> modeSetting.isSelected("Цветокоррекция"));
    private final ValueSetting exposure = new ValueSetting("Экспозиция", "Освещенность сцены.").range(-100, 100).setValue(0f).visible(() -> modeSetting.isSelected("Цветокоррекция"));
    private final ValueSetting saturation = new ValueSetting("Насыщенность", "Насыщенность цветов.").range(0, 200).setValue(110f).visible(() -> modeSetting.isSelected("Цветокоррекция"));
    private final ValueSetting hue = new ValueSetting("Оттенок", "Сдвиг оттенков.").range(-180, 180).setValue(0f).visible(() -> modeSetting.isSelected("Цветокоррекция"));
    private final ValueSetting temperature = new ValueSetting("Температура", "Цветовая температура.").range(1000, 40000).setValue(1000f).visible(() -> modeSetting.isSelected("Цветокоррекция"));
    private final ColorSetting lift = new ColorSetting("Тени", "Цвет темных участков.").value(new Color(0, 0, 0).getRGB()).visible(() -> modeSetting.isSelected("Цветокоррекция"));
    private final ColorSetting gamma = new ColorSetting("Средние тона", "Цвет средних тонов.").value(new Color(0, 35, 0).getRGB()).visible(() -> modeSetting.isSelected("Цветокоррекция"));
    private final ColorSetting gain = new ColorSetting("Светлые участки", "Цвет светлых участков.").value(new Color(0, 0, 255).getRGB()).visible(() -> modeSetting.isSelected("Цветокоррекция"));
    private final ColorSetting offset = new ColorSetting("Смещение", "Общее смещение цвета.").value(new Color(0, 0, 0).getRGB()).visible(() -> modeSetting.isSelected("Цветокоррекция"));


    public final ValueSetting distanceSetting = new ValueSetting("Дистанция тумана", "Sets the value of the time")
            .setValue(100).range(20, 200).visible(() -> modeSetting.isSelected("Туман"));

    private final ColorSetting fogcolor = new ColorSetting("Цвет тумана", "Общий цвет тумана.").value(new Color(0, 0, 0).getRGB()).visible(() -> modeSetting.isSelected("Туман"));


    public CustomWorld() {
        addSettings(
                modeSetting,    time, brightness, contrast, exposure, saturation, hue, temperature, lift, gamma, gain, offset,distanceSetting,fogcolor
        );
    }

    @EventHandler
    public void onUpdate(UpdateEvents.Update e) {
        if (mc.world != null && modeSetting.isSelected("Свое время")) {
            (mc.world.getLevelProperties()).setTimeOfDay((long) time.getValue());
        }
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive event) {
        if (modeSetting.isSelected("Свое время") && event.getPacket() instanceof WorldTimeUpdateS2CPacket) {
            event.cancel();
        }
    }

    @EventHandler
    public void onRenderWorld(RenderEvents.World event) {
        if (!modeSetting.isSelected("Цветокоррекция") || mc.gameRenderer.isRenderingPanorama()) return;

        ShaderHelper.initShadersIfNeeded();
        if (!ShaderHelper.isInitialized()) return;

        try {
            ShaderHelper.checkFramebuffers();

            Framebuffer mainFbo = mc.getFramebuffer();
            SimpleFramebuffer tempFbo = ShaderHelper.getColorGradingFbo();
            ColorGradingShader shader = ShaderHelper.getColorGradingShader();

            GlStateManager._glBindFramebuffer(GL_READ_FRAMEBUFFER, mainFbo.fbo);
            GlStateManager._glBindFramebuffer(GL_DRAW_FRAMEBUFFER, tempFbo.fbo);
            GlStateManager._glBlitFrameBuffer(
                    0, 0, mainFbo.textureWidth, mainFbo.textureHeight,
                    0, 0, tempFbo.textureWidth, tempFbo.textureHeight,
                    GL_COLOR_BUFFER_BIT, GL_NEAREST
            );

            mainFbo.beginWrite(false);
            shader.bind();
            shader.setUniforms(
                    brightness.getValue() / 100f,
                    contrast.getValue() / 100f,
                    exposure.getValue() / 100f,
                    saturation.getValue() / 100f,
                    (int) hue.getValue(),
                    temperature.getValue(),
                    lift, gamma, gain, offset
            );

            RenderSystem.bindTexture(tempFbo.getColorAttachment());
            ShaderHelper.drawFullScreenQuad();

            shader.unbind();
        } finally {
            mc.getFramebuffer().beginWrite(false);
        }
    }

    @EventHandler
    public void onFog(FogEvent e) {
        if (modeSetting.isSelected("Туман")) {
            e.setDistance(distanceSetting.getValue());
            e.setColor(fogcolor.getColor());
            e.cancel();
        }
    }

}