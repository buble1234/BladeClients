package win.blade.common.gui.impl.menu.settings.impl;

import win.blade.common.gui.impl.menu.settings.Setting;
import win.blade.core.module.api.Module;

import java.util.function.Supplier;

public class ModuleSetting extends Setting<Boolean> {
    private final Module module;
    private final String description;

    public ModuleSetting(Module module, String description) {
        super("Enabled", module.isEnabled());
        this.module = module;
        this.description = description;
    }

    public Module getModule() {
        return module;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public ModuleSetting set(Boolean value) {
        module.setEnabled(value);
        super.set(value);
        return this;
    }

    @Override
    public Boolean getValue() {
        return module.isEnabled();
    }

    @Override
    public ModuleSetting setVisible(Supplier<Boolean> value) {
        return (ModuleSetting) super.setVisible(value);
    }
}