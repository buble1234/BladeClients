package win.blade.common.gui.impl.gui.setting.implement;

import win.blade.common.gui.impl.gui.setting.Setting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class GroupSetting extends Setting {
    private boolean value;
    private List<Setting> subSettings = new ArrayList<>();

    public GroupSetting(String name, String description) {
        super(name, description);
    }

    public boolean getValue() {
        return value;
    }

    public GroupSetting setValue(boolean value) {
        this.value = value;
        return this;
    }

    public List<Setting> getSubSettings() {
        return subSettings;
    }

    public GroupSetting setSubSettings(List<Setting> subSettings) {
        this.subSettings = subSettings;
        return this;
    }

    public GroupSetting settings(Setting... setting) {
        subSettings.addAll(Arrays.asList(setting));
        return this;
    }

    public GroupSetting visible(Supplier<Boolean> visible) {
        setVisible(visible);
        return this;
    }

    public Setting getSubSetting(String name) {
        return subSettings.stream()
                .filter(setting -> setting.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }
}