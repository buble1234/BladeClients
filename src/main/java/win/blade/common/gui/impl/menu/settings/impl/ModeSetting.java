package win.blade.common.gui.impl.menu.settings.impl;

import win.blade.common.gui.impl.menu.settings.Setting;
import win.blade.core.module.api.Module;


import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class ModeSetting extends Setting<String> {

    public List<String> modes;
    private String cachedValue;

    public ModeSetting(Module parent, String name, String... values) {
        super(parent, name, values[0]);
        this.modes = Arrays.asList(values);
        this.cachedValue = values[0];
    }

    public boolean is(String mode) {
        return getValue().equalsIgnoreCase(mode) && getVisible().get();
    }

    public boolean is(int index){
        for (String mode : modes)
            if(mode.equalsIgnoreCase(getValue())) return true;

        return false;
    }

    @Override
    public ModeSetting set(String value) {
        super.set(value);
        this.cachedValue = super.getValue();
        return this;
    }


    @Override
    public ModeSetting setVisible(Supplier<Boolean> value) {
        return (ModeSetting) super.setVisible(value);
    }

    @Override
    public ModeSetting onAction(Runnable action) {
        return (ModeSetting) super.onAction(() -> {
            action.run();
            this.cachedValue = super.getValue();
        });
    }

    @Override
    public ModeSetting onSetVisible(Runnable action) {
        return (ModeSetting) super.onSetVisible(action);
    }

    @Override
    public String getValue() {
        if (cachedValue == null) {
            cachedValue = super.getValue();
        }
        return cachedValue;
    }
}