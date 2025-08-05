package win.blade.core.module.storage.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;
import org.lwjgl.opengl.GL30;
import win.blade.common.gui.impl.gui.setting.implement.BooleanSetting;
import win.blade.common.gui.impl.gui.setting.implement.ColorSetting;
import win.blade.common.gui.impl.gui.setting.implement.ValueSetting;
import win.blade.common.utils.render.shader.ShaderHelper;
import win.blade.common.utils.render.shader.storage.ColorGradingShader;
import win.blade.core.event.controllers.EventHandler;
import win.blade.core.event.impl.minecraft.UpdateEvents;
import win.blade.core.event.impl.network.PacketEvent;
import win.blade.core.event.impl.render.RenderEvents;
import win.blade.core.module.api.Category;
import win.blade.core.module.api.Module;
import win.blade.core.module.api.ModuleInfo;

import java.awt.Color;

@ModuleInfo(name = "CustomWorld", category = Category.RENDER, desc = "Визуально меняет мир (время и цветокоррекция)")
public class CustomWorld extends Module {
    private final BooleanSetting customTime = new BooleanSetting("Свое время", "Включает смену времени суток.").setValue(true);
    public ValueSetting time = new ValueSetting("Время", "Устанавливает выбранное время в мире").range(0, 24000).setValue(16000).visible(customTime::getValue);

    private final BooleanSetting colorGrading = new BooleanSetting("Цветокоррекция", "Включает эффекты цветокоррекции.").setValue(false);
    private final ValueSetting brightness = new ValueSetting("Яркость", "%").range(-100, 100).setValue(-50f).visible(colorGrading::getValue);
    private final ValueSetting contrast = new ValueSetting("Контраст", "%").range(0, 200).setValue(55f).visible(colorGrading::getValue);
    private final ValueSetting exposure = new ValueSetting("Экспозиция", "%").range(-100, 100).setValue(0f).visible(colorGrading::getValue);
    private final ValueSetting saturation = new ValueSetting("Насыщенность", "%").range(0, 200).setValue(110f).visible(colorGrading::getValue);
    private final ValueSetting hue = new ValueSetting("Оттенок", "°").range(-180, 180).setValue(0f).visible(colorGrading::getValue);
    private final ValueSetting temperature = new ValueSetting("Температура", "K").range(1000, 40000).setValue(1000f).visible(colorGrading::getValue);
    private final ColorSetting lift = new ColorSetting("Тени", "").value(new Color(0, 0, 0).getRGB()).visible(colorGrading::getValue);
    private final ColorSetting gamma = new ColorSetting("Средние тона", "").value(new Color(0, 35, 0).getRGB()).visible(colorGrading::getValue);
    private final ColorSetting gain = new ColorSetting("Светлые участки", "").value(new Color(0, 0, 255).getRGB()).visible(colorGrading::getValue);
    private final ColorSetting offset = new ColorSetting("Смещение", "").value(new Color(0, 0, 0).getRGB()).visible(colorGrading::getValue);

    public CustomWorld() {
        addSettings(
                customTime, time,
                colorGrading, brightness, contrast, exposure, saturation, hue, temperature, lift, gamma, gain, offset
        );
    }

    @EventHandler
    public void onUpdate(UpdateEvents.Update e) {
        if (mc.world != null && customTime.getValue()) {
            (mc.world.getLevelProperties()).setTimeOfDay((long) time.getValue());
        }
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive event) {
        if (customTime.getValue() && event.getPacket() instanceof WorldTimeUpdateS2CPacket) {
            event.cancel();
        }
    }

    @EventHandler
    public void onRenderWorld(RenderEvents.World event) {
        if (!colorGrading.getValue() || mc.gameRenderer.isRenderingPanorama()) return;

        ShaderHelper.initShadersIfNeeded();
        if (!ShaderHelper.isInitialized()) return;

        ColorGradingShader shader = ShaderHelper.getColorGradingShader();

        shader.bind();
        RenderSystem.activeTexture(GL30.GL_TEXTURE0);
        mc.getFramebuffer().beginRead();

        shader.setUniforms(
                brightness.getValue() / 100f,
                contrast.getValue() / 100f,
                exposure.getValue() / 100f,
                saturation.getValue() / 100f,
                (int) hue.getValue(),
                temperature.getValue(),
                lift, gamma, gain, offset
        );

        ShaderHelper.drawFullScreenQuad();

        shader.unbind();
        mc.getFramebuffer().endRead();
    }
}