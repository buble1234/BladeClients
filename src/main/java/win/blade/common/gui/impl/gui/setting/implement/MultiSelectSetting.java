package win.blade.common.gui.impl.gui.setting.implement;

import win.blade.common.gui.impl.gui.setting.Setting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class MultiSelectSetting extends Setting {
    private List<String> selected = new ArrayList<>();
    private List<String> list;

    public MultiSelectSetting(String name, String description) {
        super(name, description);
    }

    public List<String> getSelected() {
        return selected;
    }

    public List<String> getList() {
        return list;
    }

    public MultiSelectSetting value(String... values) {
        this.list = Arrays.asList(values);
        return this;
    }

    public MultiSelectSetting visible(Supplier<Boolean> visible) {
        setVisible(visible);
        return this;
    }

    public boolean isSelected(String name) {
        return selected.stream().anyMatch(s -> s.equalsIgnoreCase(name));
    }

    public void toggle(String name) {
        String actualName = this.list.stream()
                .filter(s -> s.equalsIgnoreCase(name))
                .findFirst()
                .orElse(name);

        if (isSelected(actualName)) {
            selected.removeIf(s -> s.equalsIgnoreCase(actualName));
        } else {
            selected.add(actualName);
        }
    }
}