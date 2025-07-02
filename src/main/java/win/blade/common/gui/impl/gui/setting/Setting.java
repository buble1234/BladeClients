package win.blade.common.gui.impl.gui.setting;

import java.util.function.Supplier;

public class Setting {
    private final String name;
    private String description;
    private Supplier<Boolean> visible;

    public Setting(String name) {
        this.name = name;
    }

    public Setting(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Supplier<Boolean> getVisible() {
        return visible;
    }

    public void setVisible(Supplier<Boolean> visible) {
        this.visible = visible;
    }
}