package win.blade.core.module.storage.render;

import win.blade.common.gui.impl.gui.setting.implement.BooleanSetting;
import win.blade.common.gui.impl.gui.setting.implement.ValueSetting;
import win.blade.common.utils.render.shader.ShaderHelper;
import win.blade.common.utils.render.shader.storage.FogBlurRender;
import win.blade.core.event.controllers.EventHandler;
import win.blade.core.event.impl.render.RenderEvents;
import win.blade.core.module.api.Category;
import win.blade.core.module.api.Module;
import win.blade.core.module.api.ModuleInfo;

@ModuleInfo(name = "FogBlur", category = Category.RENDER, desc = "Добавляет эффект размытого тумана на расстоянии.")
public class FogBlur extends Module {

    private final ValueSetting fogStrength = new ValueSetting("Сила размытия", "Сила размытия для эффекта тумана.").setValue(6f).range(1f, 20f);
    private final ValueSetting fogDistance = new ValueSetting("Дистанция тумана", "На какой дистанции появляется эффект тумана.").setValue(50f).range(0f, 200f);
    private final BooleanSetting linearSampling = new BooleanSetting("Линейная выборка", "Включает линейную выборку для Гауссова размытия.").setValue(true);

    private final BooleanSetting fogRGBPuke = new BooleanSetting("Fog RGB Puke", "Добавляет радужный эффект к туману.").setValue(false);
    private final ValueSetting fogRGBPukeOpacity = new ValueSetting("RGB прозр.", "Прозрачность радужного эффекта.").setValue(30f).range(1f, 100f);
    private final ValueSetting fogRGBPukeSaturation = new ValueSetting("RGB насыщенность", "Насыщенность радужного эффекта.").setValue(70f).range(0f, 100f);
    private final ValueSetting fogRGBPukeBrightness = new ValueSetting("RGB яркость", "Яркость радужного эффекта.").setValue(100f).range(0f, 100f);

    public FogBlur() {
        addSettings(
                fogStrength, fogDistance, linearSampling,
                fogRGBPuke, fogRGBPukeOpacity, fogRGBPukeSaturation, fogRGBPukeBrightness
        );
    }

    @EventHandler
    public void onRenderWorld(RenderEvents.World event) {
        if (mc.player == null || mc.world == null || mc.gameRenderer.isRenderingPanorama()) return;

        FogBlurRender.applyFogBlur(
                fogStrength.getValue(),
                fogDistance.getValue(),
                linearSampling.getValue(),
                fogRGBPuke.getValue(),
                fogRGBPukeOpacity.getValue() / 100.0f,
                fogRGBPukeSaturation.getValue() / 100.0f,
                fogRGBPukeBrightness.getValue() / 100.0f
        );
    }
}