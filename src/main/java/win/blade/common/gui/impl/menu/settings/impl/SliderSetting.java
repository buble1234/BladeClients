package win.blade.common.gui.impl.menu.settings.impl;

import win.blade.common.gui.impl.menu.settings.Setting;
import win.blade.core.module.api.Module;


import java.util.function.Supplier;

public class SliderSetting extends Setting<Float> {
    public float min;
    public float max;
    public float increment;
    private Float cachedValue;

    public SliderSetting(Module parent, String name, float value, float min, float max, float increment) {
        super(parent, name, value);
        this.min = min;
        this.max = max;
        this.increment = increment;
        this.cachedValue = value;
    }

    @Override
    public SliderSetting set(Float value) {
        super.set(value);
        this.cachedValue = super.getValue();
        return this;
    }

    @Override
    public SliderSetting setVisible(Supplier<Boolean> value) {
        return (SliderSetting) super.setVisible(value);
    }

    @Override
    public SliderSetting onAction(Runnable action) {
        return (SliderSetting) super.onAction(() -> {
            if (action != null) action.run();
            this.cachedValue = super.getValue();
        });
    }

    @Override
    public SliderSetting onSetVisible(Runnable action) {
        return (SliderSetting) super.onSetVisible(action);
    }

    @Override
    public Float getValue() {
        if (cachedValue == null) {
            cachedValue = super.getValue();
        }
        return cachedValue;
    }
}