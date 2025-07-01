package win.blade.common.gui.impl.gui.components.implement.window.implement.settings;

import win.blade.common.gui.impl.gui.components.implement.window.implement.AbstractBindWindow;
import win.blade.common.gui.impl.gui.setting.implement.BooleanSetting;

public class BindCheckboxWindow extends AbstractBindWindow {
    private final BooleanSetting setting;

    public BindCheckboxWindow(BooleanSetting setting) {
        this.setting = setting;
    }

    @Override
    protected int getKey() {
        return setting.getKey();
    }

    @Override
    protected void setKey(int key) {
        setting.setKey(key);
    }

    @Override
    protected int getType() {
        return setting.getType();
    }

    @Override
    protected void setType(int type) {
        setting.setType(type);
    }
}