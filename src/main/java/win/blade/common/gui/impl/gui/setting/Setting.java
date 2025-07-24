package win.blade.common.gui.impl.gui.setting;

import java.util.function.Supplier;

public class Setting {
    private final String name;
    private String description;
    private Supplier<Boolean> visible;
    private boolean inBox = false;

    public Setting(String name) {
        this.name = name;
        description = "";
    }

    public Setting(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public Setting addToBox(CheckBox box){
        box.add(this);
        inBox = true;

        return this;
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

    public boolean isInBox(){
        return inBox;
    }

    public boolean notInBox(){
        return !isInBox();
    }

    public void setVisible(Supplier<Boolean> visible) {
        this.visible = visible;
    }
}