package win.blade.common.gui.impl.menu.settings.impl;

import win.blade.common.gui.impl.menu.settings.Setting;
import win.blade.core.module.api.Module;
import win.blade.common.gui.impl.menu.settings.Setting;


import java.awt.Color;
import java.util.function.Supplier;

public class ColorSetting extends Setting<Integer> {

    private Integer cachedValue;

    public ColorSetting(Module parent, String name, Color initialColor) {
        super(parent, name, initialColor.getRGB());
        this.cachedValue = initialColor.getRGB();
    }

    @Override
    public ColorSetting set(Integer value) {
        super.set(value);
        this.cachedValue = super.getValue();
        return this;
    }

    @Override
    public ColorSetting setVisible(Supplier<Boolean> value) {
        return (ColorSetting) super.setVisible(value);
    }

    @Override
    public ColorSetting onAction(Runnable action) {
        return (ColorSetting) super.onAction(() -> {
            action.run();
            this.cachedValue = super.getValue();
        });
    }

    @Override
    public ColorSetting onSetVisible(Runnable action) {
        return (ColorSetting) super.onSetVisible(action);
    }

    @Override
    public Integer getValue() {
        if (cachedValue == null) {
            cachedValue = super.getValue();
        }
        return cachedValue;
    }
}