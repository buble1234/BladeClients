package win.blade.common.gui.impl.menu.settings.impl;

import win.blade.common.gui.impl.menu.settings.Setting;
import win.blade.core.module.api.Module;
import win.blade.common.gui.impl.menu.settings.Setting;
import win.blade.core.module.api.Module;

import java.util.function.Supplier;

public class StringSetting extends Setting<String> {

    public final boolean onlyNumbers;
    private String cachedValue;

    public StringSetting(Module parent, String name, String value, boolean onlyNumbers) {
        super(parent, name, value);
        this.onlyNumbers = onlyNumbers;
        this.cachedValue = value;
    }


    public StringSetting(Module parent, String name, String value) {
        this(parent, name, value, false);
    }


    @Override
    public StringSetting set(String value) {
        super.set(value);
        this.cachedValue = super.getValue();
        return this;
    }

    @Override
    public StringSetting setVisible(Supplier<Boolean> value) {
        return (StringSetting) super.setVisible(value);
    }

    @Override
    public StringSetting onAction(Runnable action) {
        return (StringSetting) super.onAction(() -> {
            action.run();
            this.cachedValue = super.getValue();
        });
    }

    @Override
    public StringSetting onSetVisible(Runnable action) {
        return (StringSetting) super.onSetVisible(action);
    }

    @Override
    public String getValue() {
        if (cachedValue == null) {
            cachedValue = "";
        }
        return cachedValue;
    }
}