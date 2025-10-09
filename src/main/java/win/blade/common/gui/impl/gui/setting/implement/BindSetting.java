package win.blade.common.gui.impl.gui.setting.implement;

import org.lwjgl.glfw.GLFW;
import win.blade.common.gui.impl.gui.setting.Setting;

import java.util.function.Supplier;

public class BindSetting extends Setting {
    private int key = GLFW.GLFW_KEY_UNKNOWN;
    private int type = 1;

    public BindSetting(String name, String description) {
        super(name, description);
    }

    public int getKey() {
        return key;
    }

    public BindSetting setKey(int key) {
        this.key = key;
        return this;
    }

    public int getType() {
        return type;
    }

    public BindSetting setType(int type) {
        this.type = type;
        return this;
    }

    public BindSetting visible(Supplier<Boolean> visible) {
        setVisible(visible);
        return this;
    }
}