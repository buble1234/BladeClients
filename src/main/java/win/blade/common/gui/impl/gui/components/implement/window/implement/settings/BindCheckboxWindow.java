package win.blade.common.gui.impl.gui.components.implement.window.implement.settings;

import win.blade.common.gui.impl.gui.components.implement.settings.ValueComponent;
import win.blade.common.gui.impl.gui.components.implement.window.implement.AbstractBindWindow;
import win.blade.common.gui.impl.gui.setting.implement.BooleanSetting;
import win.blade.common.gui.impl.gui.setting.implement.ValueSetting;

public class BindCheckboxWindow extends AbstractBindWindow {
    private final BooleanSetting setting;

    public BindCheckboxWindow(BooleanSetting setting) {
        this.setting = setting;
        super.setting = new ValueSetting("Value", "").range(0, 250).setValue(setting.getHoldDuration());
        super.component = (ValueComponent) new ValueComponent(super.setting);
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

    @Override
    protected Runnable onChange() {
        return () -> {
            setting.setHoldDuration((long) super.setting.getValue());
        };
    }
}