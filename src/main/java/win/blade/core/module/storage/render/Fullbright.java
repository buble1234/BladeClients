package win.blade.core.module.storage.render;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import win.blade.common.gui.impl.menu.settings.impl.ModeSetting;
import win.blade.common.gui.impl.menu.settings.impl.SliderSetting;
import win.blade.core.event.controllers.EventHandler;
import win.blade.core.event.impl.minecraft.OptionEvents;
import win.blade.core.event.impl.minecraft.UpdateEvents;
import win.blade.core.module.api.Category;
import win.blade.core.module.api.Module;
import win.blade.core.module.api.ModuleInfo;

/**
 * Автор: NoCap
 * Дата создания: 02.07.2025
 */
@ModuleInfo(
        name = "Fullbright",
        desc = "Делает мир ярким",
        category = Category.RENDER
)
public class Fullbright extends Module {

    private final ModeSetting mode = new ModeSetting(this,"Мод", "Яркость", "Зелье");
    private final SliderSetting brightness = new SliderSetting(this,"Яркость", 15.0f, 1.0f, 20.0f, 0.5f).setVisible(() -> mode.is("Яркость"));

    private double oldGamma;

    @Override
    public void onEnable() {
        super.onEnable();
        if (mc.options != null) {
            this.oldGamma = mc.options.getGamma().getValue();
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (mc.options != null) {
            mc.options.getGamma().setValue(this.oldGamma);
        }
        if (mc.player != null) {
            mc.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
        }
    }

    @EventHandler
    public void onUpdate(UpdateEvents.Update e) {
        if (mc.player == null || mc.options == null) return;

        switch (mode.getValue()) {
            case "Зелье":
                mc.player.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.NIGHT_VISION,
                        Integer.MAX_VALUE,
                        0,
                        true,
                        false
                ));
                mc.options.getGamma().setValue(this.oldGamma);
                break;

            case "Яркость":
                if (mc.player.hasStatusEffect(StatusEffects.NIGHT_VISION)) {
                    mc.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
                }
                break;
        }
    }

    @EventHandler
    public void onGamma(OptionEvents.Gamma e) {
        if (mode.is("Яркость")) {
            e.setGamma(brightness.getValue());
        }
    }
}