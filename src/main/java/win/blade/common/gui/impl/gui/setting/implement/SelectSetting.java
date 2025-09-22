package win.blade.common.gui.impl.gui.setting.implement;

import win.blade.common.gui.impl.gui.setting.Setting;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class SelectSetting extends Setting {
    private String selected;
    private List<String> list;

    public SelectSetting(String name, String description) {
        super(name, description);
    }

    public String getSelected() {
        return selected;
    }

    public List<String> getList() {
        return list;
    }

    public SelectSetting value(String... values) {
        List<String> list = Arrays.asList(values);

        selected = list.get(0);
        this.list = list;

        return this;
    }

    public SelectSetting visible(Supplier<Boolean> visible) {
        setVisible(visible);
        return this;
    }

    public boolean isSelected(String name) {
        return selected.equalsIgnoreCase(name);
    }

    public void setSelected(String selected) {
        this.selected = selected;
        if (getName().equalsIgnoreCase("AntiCheat")) {
        }
    }
}