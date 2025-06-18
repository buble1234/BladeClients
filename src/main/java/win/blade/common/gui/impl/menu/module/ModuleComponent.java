package win.blade.common.gui.impl.menu.module;

import net.minecraft.client.gui.DrawContext;
import win.blade.common.gui.impl.menu.MenuScreen;
import win.blade.common.gui.impl.menu.component.setting.ModuleSettingComponent;
import win.blade.common.gui.impl.menu.settings.impl.ModuleSetting;
import win.blade.common.gui.impl.menu.window.WindowComponent;
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.builders.states.QuadColorState;
import win.blade.common.utils.render.builders.states.QuadRadiusState;
import win.blade.common.utils.render.builders.states.SizeState;
import win.blade.core.module.api.Module;

import java.awt.Color;

public class ModuleComponent extends WindowComponent {
    private final Module module;
    public final ModuleSettingComponent settingComponent;
    private float lastHeight = 50;

    public ModuleComponent(Module module, MenuScreen parentScreen) {
        super(parentScreen);
        this.module = module;
        this.settingComponent = new ModuleSettingComponent(parentScreen, new ModuleSetting(module, module.data().description()));
    }

    public void render(DrawContext context, float x, float y, float width, float height, int mouseX, int mouseY, float alpha, float scale, boolean hovered) {
        settingComponent.x = x + 4 * scale;
        settingComponent.y = y + 4 * scale;
        settingComponent.width = width - 8 * scale;
        settingComponent.scale = scale;

        settingComponent.render(context, mouseX, mouseY, 0, alpha);

        lastHeight = settingComponent.height + 8 * scale;

        Builder.border()
                .size(new SizeState(width, height))
                .radius(new QuadRadiusState(8f))
                .thickness(0.01f)
                .color(new QuadColorState(new Color(15, 15, 15, 50)))
//                .color(new QuadColorState(new Color(19, 21, 27, 1)))
                .build()
                .render(x, y);
    }

    public void mouseClicked(double mouseX, double mouseY, int button) {
        settingComponent.mouseClicked(mouseX, mouseY, button);
    }

    public void mouseReleased(double mouseX, double mouseY, int button) {
        settingComponent.mouseReleased(mouseX, mouseY, button);
    }

    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        settingComponent.keyPressed(keyCode, scanCode, modifiers);
    }

    public void charTyped(char chr, int modifiers) {
        settingComponent.charTyped(chr, modifiers);
    }

    public float getActualHeight() {
        return lastHeight;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta, float alpha) {
    }
}