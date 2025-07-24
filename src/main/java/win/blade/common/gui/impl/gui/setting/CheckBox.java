package win.blade.common.gui.impl.gui.setting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Автор Ieo117
 * Дата создания: 21.07.2025, в 21:06:09
 */
public class CheckBox extends Setting {
    public List<Setting> children = new ArrayList<>();

    public CheckBox(String name, Setting... settings) {
        super(name);

        children.addAll(settings.length > 0 ? Arrays.asList(settings) : new ArrayList<>());
    }

    public CheckBox add(Setting setting){
        children.add(setting);

        return this;
    }

    public List<Setting> getChildren(){
        return children;
    }

    @Override
    public Setting addToBox(CheckBox box) {
        throw new RuntimeException("Нельзя добавить бокс в бокс");//super.addToBox(box);
    }
}
