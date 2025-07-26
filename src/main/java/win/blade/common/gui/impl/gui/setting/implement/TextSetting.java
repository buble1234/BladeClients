package win.blade.common.gui.impl.gui.setting.implement;

import win.blade.common.gui.impl.gui.setting.Setting;

import java.util.function.Supplier;

public class TextSetting extends Setting {
    private String text;
    private int min = 0, max = Integer.MAX_VALUE;

    public TextSetting(String name, String description) {
        super(name, description);
    }

    public String getText() {
        return text;
    }

    public TextSetting setText(String text) {
        this.text = text;
        return this;
    }

    public int getMin() {
        return min;
    }

    public TextSetting setMin(int min) {
        this.min = min;
        return this;
    }

    public int getMax() {
        return max;
    }

    public TextSetting setMax(int max) {
        this.max = max;
        return this;
    }

    public TextSetting visible(Supplier<Boolean> visible) {
        setVisible(visible);
        return this;
    }
}