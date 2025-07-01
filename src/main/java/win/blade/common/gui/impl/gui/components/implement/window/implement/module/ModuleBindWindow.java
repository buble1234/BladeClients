package win.blade.common.gui.impl.gui.components.implement.window.implement.module;


import win.blade.common.gui.impl.gui.components.implement.window.implement.AbstractBindWindow;
import win.blade.core.module.api.Module;

public class ModuleBindWindow extends AbstractBindWindow {
    private final Module module;

    public ModuleBindWindow(Module module) {
        this.module = module;
    }

    @Override
    protected int getKey() {
        return module.keybind();
    }

    @Override
    protected void setKey(int key) {
        module.setKeybind(key);
    }

    @Override
    protected int getType() {
        return module.type;
    }

    @Override
    protected void setType(int type) {
        module.type = type;

    }

}