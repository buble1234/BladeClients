package win.blade.common.gui.impl.gui.setting.implement;

import win.blade.common.gui.impl.gui.setting.Setting;

import java.util.function.Supplier;

public class ButtonSetting extends Setting {
    private Runnable runnable;
    private String buttonName;

    public ButtonSetting(String name, String description) {
        super(name, description);
    }

    public Runnable getRunnable() {
        return runnable;
    }

    public ButtonSetting setRunnable(Runnable runnable) {
        this.runnable = runnable;
        return this;
    }

    public String getButtonName() {
        return buttonName;
    }

    public ButtonSetting setButtonName(String buttonName) {
        this.buttonName = buttonName;
        return this;
    }

    public ButtonSetting visible(Supplier<Boolean> visible) {
        setVisible(visible);
        return this;
    }
}