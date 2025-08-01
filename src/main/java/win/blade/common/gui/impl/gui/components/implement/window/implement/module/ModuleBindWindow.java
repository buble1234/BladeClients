package win.blade.common.gui.impl.gui.components.implement.window.implement.module;


import net.minecraft.client.gui.DrawContext;
import win.blade.common.gui.impl.gui.components.implement.settings.ValueComponent;
import win.blade.common.gui.impl.gui.components.implement.window.implement.AbstractBindWindow;
import win.blade.common.gui.impl.gui.setting.implement.ValueSetting;
import win.blade.core.module.api.Module;

public class ModuleBindWindow extends AbstractBindWindow {
    private final Module module;

    public ModuleBindWindow(Module module) {
        this.module = module;
        setting = new ValueSetting("Value", "").setMax(250).setMin(0).setValue(module.holdDuration);
        component = (ValueComponent) new ValueComponent(setting).withoutRenderingDescription();
        windowName = "bindWindow";
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

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if(getType() == 0) {
            if (component.mouseReleased(mouseX, mouseY, button)) return true;
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if(getType() == 0) {
            if (component.mouseClicked(mouseX, mouseY, button)) return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected Runnable onChange() {
        return () -> {
            this.module.holdDuration = (long) setting.getValue();
        };
    }
}