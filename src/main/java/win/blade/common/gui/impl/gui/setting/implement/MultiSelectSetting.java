package win.blade.common.gui.impl.gui.setting.implement;

import win.blade.common.gui.impl.gui.setting.Setting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class MultiSelectSetting extends Setting {
    private List<String> list, selected = new ArrayList<>();

    public MultiSelectSetting(String name, String description) {
        super(name, description);
    }

    public List<String> getList() {
        return list;
    }

    public void setList(List<String> list) {
        this.list = list;
    }

    public List<String> getSelected() {
        return selected;
    }

    public void setSelected(List<String> selected) {
        this.selected = selected;
    }

    public MultiSelectSetting value(String... settings) {
        list = Arrays.asList(settings);
        return this;
    }

    public MultiSelectSetting visible(Supplier<Boolean> visible) {
        setVisible(visible);
        return this;
    }

    public boolean isSelected(String name) {
        return selected.contains(name);
    }
}