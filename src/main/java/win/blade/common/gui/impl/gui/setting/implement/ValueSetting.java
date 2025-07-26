package win.blade.common.gui.impl.gui.setting.implement;

import win.blade.common.gui.impl.gui.setting.Setting;

import java.util.function.Supplier;

public class ValueSetting extends Setting {
    private float value,
            min,
            max;

    public ValueSetting(String name, String description) {
        super(name, description);
    }

    public float getValue() {
        return value;
    }

    public ValueSetting setValue(float value) {
        this.value = value;
        return this;
    }

    public float getMin() {
        return min;
    }

    public ValueSetting setMin(float min) {
        this.min = min;
        return this;
    }

    public float getMax() {
        return max;
    }

    public ValueSetting setMax(float max) {
        this.max = max;
        return this;
    }

    public ValueSetting range(float min, float max) {
        this.min = min;
        this.max = max;
        return this;
    }

    public ValueSetting visible(Supplier<Boolean> visible) {
        setVisible(visible);
        return this;
    }

    @Override
    public ValueSetting withAttachments(Setting... settings) {
        return (ValueSetting) super.withAttachments(settings);
    }
}