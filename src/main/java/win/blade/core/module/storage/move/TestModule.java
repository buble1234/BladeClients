package win.blade.core.module.storage.move;

import win.blade.common.gui.impl.gui.setting.implement.ColorSetting;
import win.blade.common.gui.impl.gui.setting.implement.GroupSetting;
import win.blade.common.gui.impl.gui.setting.implement.TextSetting;
import win.blade.common.gui.impl.gui.setting.implement.ValueSetting;
import win.blade.core.module.api.Category;
import win.blade.core.module.api.Module;
import win.blade.core.module.api.ModuleInfo;

import java.util.List;

/**
 * Автор Ieo117
 * Дата создания: 17.06.2025, в 20:04:59
 */
@ModuleInfo(name = "Test", category = Category.MOVE, desc = "Просто тест обычный")
public class TestModule extends Module {
    public TextSetting textSetting = new TextSetting("Input Text", "Description").setMax(24);
    public GroupSetting groupSetting = new GroupSetting("Checkbox", "Description").setSubSettings(List.of(textSetting));
    public ValueSetting valueSetting = new ValueSetting("Slider", "Description").range(0, 100).setValue(50);
    public ColorSetting colorSetting = new ColorSetting("Color Picker", "");

    public TestModule (){
        addSettings(groupSetting, textSetting, valueSetting, colorSetting);
    }
}
