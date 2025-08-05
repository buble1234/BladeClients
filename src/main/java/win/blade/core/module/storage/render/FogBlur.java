package win.blade.core.module.storage.render;

import win.blade.common.gui.impl.gui.setting.implement.BooleanSetting;
import win.blade.common.gui.impl.gui.setting.implement.ValueSetting;
import win.blade.common.utils.render.shader.ShaderHelper;
import win.blade.core.event.controllers.EventHandler;
import win.blade.core.event.impl.render.RenderEvents;
import win.blade.core.module.api.Category;
import win.blade.core.module.api.Module;
import win.blade.core.module.api.ModuleInfo;

@ModuleInfo(name = "FogBlur", category = Category.RENDER, desc = "Добавляет эффект размытого тумана на расстоянии.")
public class FogBlur extends Module {

    private final ValueSetting fogStrength = new ValueSetting("Сила размытия", "Сила размытия для эффекта тумана.").setValue(6f).range(1f, 20f);
    private final ValueSetting fogDistance = new ValueSetting("Дистанция тумана", "На какой дистанции появляется эффект тумана.").setValue(50f).range(0f, 100f);
    private final BooleanSetting linearSampling = new BooleanSetting("Линейная выборка", "Включает линейную выборку для Гауссова размытия.").setValue(true);
    private final BooleanSetting rainbow = new BooleanSetting("Rainbow", "Включает радужный эффект для тумана.").setValue(false);

    private final ValueSetting rainbowStrength = new ValueSetting("Сила радуги", "Интенсивность радужного эффекта.").setValue(0.5f).range(0f, 1f);


    public FogBlur() {
        addSettings(fogStrength, fogDistance, linearSampling, rainbow, rainbowStrength);
    }

    @EventHandler
    public void onRenderWorld(RenderEvents.World event) {
        if (mc.player == null || mc.world == null || mc.gameRenderer.isRenderingPanorama()) return;

        ShaderHelper.applyFogBlur(fogStrength.getValue(), fogDistance.getValue(), linearSampling.getValue(), rainbow.getValue(), true, rainbowStrength.getValue());
    }
}