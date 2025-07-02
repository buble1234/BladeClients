package win.blade.common.gui.impl.gui.components.implement.settings;

import win.blade.common.gui.impl.gui.components.AbstractComponent;
import win.blade.common.gui.impl.gui.setting.Setting;

public abstract class AbstractSettingComponent extends AbstractComponent {
    private final Setting setting;

    public AbstractSettingComponent(Setting setting) {
        this.setting = setting;
    }

    public Setting getSetting() {
        return setting;
    }
}