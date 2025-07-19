package win.blade.common.gui.impl.menu.settings.impl;


import win.blade.common.gui.impl.menu.settings.Setting;
import win.blade.core.module.api.Module;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public class MultiBooleanSetting extends Setting<Map<String, BooleanSetting>> {

    private final Map<String, BooleanSetting> settingsMap = new LinkedHashMap<>();

    public MultiBooleanSetting(Module parent, String name, BooleanSetting... values) {
        super(parent, name, new LinkedHashMap<>());
        for (BooleanSetting value : values) {
            this.settingsMap.put(value.getName().toLowerCase(), value);
        }
    }

    public BooleanSetting get(String name) {
        return settingsMap.get(name.toLowerCase());
    }

    public boolean getValue(String name) {
        BooleanSetting setting = get(name);
        return setting != null && setting.getValue() && getVisible().get();
    }

    public Collection<BooleanSetting> getValues() {
        return settingsMap.values();
    }

    public void setEnabled(String option, Boolean value){
        var foundOption = settingsMap.get(option);

        if(foundOption != null){
            foundOption.set(value);
            settingsMap.put(option, foundOption);
        } else {
            System.err.println("No such option!");
        }
    }

    @Override
    public MultiBooleanSetting setVisible(Supplier<Boolean> value) {
        return (MultiBooleanSetting) super.setVisible(value);
    }

    @Override
    public MultiBooleanSetting onAction(Runnable action) {
        return (MultiBooleanSetting) super.onAction(action);
    }

    @Override
    public MultiBooleanSetting onSetVisible(Runnable action) {
        return (MultiBooleanSetting) super.onSetVisible(action);
    }
}