package win.blade.common.gui.impl.gui.setting;


import win.blade.common.gui.impl.gui.components.Component;
import win.blade.common.gui.impl.gui.components.implement.settings.AbstractSettingComponent;
import win.blade.common.gui.impl.gui.components.implement.settings.*;
import win.blade.common.gui.impl.gui.components.implement.settings.multiselect.MultiSelectComponent;
import win.blade.common.gui.impl.gui.components.implement.settings.select.SelectComponent;
import win.blade.common.gui.impl.gui.setting.implement.*;

import java.util.List;

public class SettingComponentAdder {
    public void addSettingComponent(List<Setting> settings, List<AbstractSettingComponent> components) {
        settings.forEach(setting -> {
            components.add(Component.getBySetting(setting));
        });
    }
}
