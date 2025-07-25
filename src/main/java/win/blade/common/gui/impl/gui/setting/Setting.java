package win.blade.common.gui.impl.gui.setting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class Setting {
    private final String name;
    private String description;
    private Supplier<Boolean> visible;
    private boolean inBox = false;

    private List<Setting> attachments = new ArrayList<>();

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
        return description.isEmpty() ? "No description" : description;
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

    public Setting withAttachments(Setting... settings){
        if(settings.length == 0) return this;
        attachments.addAll(Arrays.asList(settings));

        return this;
    }

    public boolean hasAttachments(){
        return !attachments.isEmpty();
    }

    public List<Setting> getAttachments(){
        return attachments;
    }

    public void setVisible(Supplier<Boolean> visible) {
        this.visible = visible;
    }

}