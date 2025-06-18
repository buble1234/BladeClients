package win.blade.common.gui.impl.menu.settings.impl;

import win.blade.common.gui.impl.menu.settings.Setting;
import win.blade.core.module.api.Module;


import java.util.function.Supplier;

public class BindSetting extends Setting<Integer> {
    public final boolean allowMouse;
    private Integer cachedValue;

    public BindSetting(Module parent, String name, int value, boolean allowMouse) {
        super(parent, name, value);
        this.allowMouse = allowMouse;
        this.cachedValue = value;
    }

    public BindSetting(Module parent, String name, int value) {
        this(parent, name, value, true);
    }

    public BindSetting(Module parent, String name) {
        this(parent, name, -1, true);
    }


    @Override
    public BindSetting set(Integer value) {
        super.set(value);
        this.cachedValue = super.getValue();
        return this;
    }

    @Override
    public BindSetting setVisible(Supplier<Boolean> value) {
        return (BindSetting) super.setVisible(value);
    }

    @Override
    public BindSetting onAction(Runnable action) {
        return (BindSetting) super.onAction(() -> {
            action.run();
            this.cachedValue = super.getValue();
        });
    }

    @Override
    public BindSetting onSetVisible(Runnable action) {
        return (BindSetting) super.onSetVisible(action);
    }

    @Override
    public Integer getValue() {
        if (cachedValue == null) {
            cachedValue = -1;
        }
        return cachedValue;
    }
}