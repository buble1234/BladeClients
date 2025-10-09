package win.blade.common.gui.impl.gui.setting.implement;

import org.lwjgl.glfw.GLFW;
import win.blade.common.gui.impl.gui.setting.CheckBox;
import win.blade.common.gui.impl.gui.setting.Setting;

import java.util.function.Supplier;

public class BooleanSetting extends Setting {
    private boolean value;
    private int key = GLFW.GLFW_KEY_UNKNOWN;
    private int type = 1;
    private long holdDuration = 50;

    public BooleanSetting(String name, String description) {
        super(name, description);
    }

    public boolean getValue() {
        return value;
    }

    public BooleanSetting setValue(boolean value) {
        this.value = value;
        return this;
    }

    public int getKey() {
        return key;
    }

    public BooleanSetting setKey(int key) {
        this.key = key;
        return this;
    }

    public int getType() {
        return type;
    }

    public long getHoldDuration() {
        return holdDuration;
    }

    public BooleanSetting setType(int type) {
        this.type = type;
        return this;
    }

    public BooleanSetting setHoldDuration(long holdDuration) {
        this.holdDuration = holdDuration;

        return this;
    }

    public BooleanSetting visible(Supplier<Boolean> visible) {
        setVisible(visible);
        return this;
    }

    @Override
    public BooleanSetting withAttachments(Setting... settings) {
        return (BooleanSetting) super.withAttachments(settings);
    }


}