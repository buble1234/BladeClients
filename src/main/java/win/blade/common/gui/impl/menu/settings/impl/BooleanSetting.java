package win.blade.common.gui.impl.menu.settings.impl;

import win.blade.common.gui.impl.menu.settings.Setting;
import win.blade.core.module.api.Module;


import java.util.function.Supplier;

public class BooleanSetting extends Setting<Boolean> {

    private Boolean cachedValue;

    public BooleanSetting(Module parent, String name, Boolean value) {
        super(parent, name, value);
        this.cachedValue = value;
    }

    private BooleanSetting(String name, Boolean value) {
        super(name, value);
        this.cachedValue = value;
    }

    public static BooleanSetting of(String name, Boolean value) {
        return new BooleanSetting(name, value);
    }

    @Override
    public BooleanSetting setVisible(Supplier<Boolean> value) {
        return (BooleanSetting) super.setVisible(value);
    }

    @Override
    public BooleanSetting set(Boolean value) {
        super.set(value);
        this.cachedValue = super.getValue() && getVisible().get();
        return this;
    }

    @Override
    public BooleanSetting onAction(Runnable action) {
        return (BooleanSetting) super.onAction(() -> {
            action.run();
            this.cachedValue = super.getValue() && getVisible().get();
        });
    }

    @Override
    public BooleanSetting onSetVisible(Runnable action) {
        return (BooleanSetting) super.onSetVisible(action);
    }

    @Override
    public Boolean getValue() {
        if (cachedValue == null) {
            cachedValue = super.getValue();
        }
        return cachedValue && getVisible().get();
    }
}