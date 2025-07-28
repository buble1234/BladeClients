package win.blade.common.gui.impl.gui.components.implement.window.implement.settings;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.util.Identifier;
import win.blade.common.gui.impl.gui.components.Component;
import win.blade.common.gui.impl.gui.components.implement.settings.AbstractSettingComponent;
import win.blade.common.gui.impl.gui.components.implement.window.AbstractWindow;
import win.blade.common.gui.impl.gui.components.implement.window.WindowManager;
import win.blade.common.gui.impl.gui.setting.Setting;
import win.blade.common.utils.color.ColorUtility;
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.builders.states.QuadColorState;
import win.blade.common.utils.render.builders.states.QuadRadiusState;
import win.blade.common.utils.render.builders.states.SizeState;
import win.blade.common.utils.render.msdf.FontType;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Автор Ieo117
 * Дата создания: 24.07.2025, в 20:57:19
 */
public class PopUpWindow extends AbstractWindow {
    protected Setting parentSetting;
    protected List<AbstractSettingComponent> settingComponents = new ArrayList<>();

    public PopUpWindow(Setting setting){
        parentSetting = setting;
        windowName = "popUp";
        draggable(true);

        settingComponents.addAll(
                parentSetting.getAttachments().stream()
                        .map(st -> Component.getBySetting(st).withoutRenderingDescription())
                        .toList()
        );

        width = 105;
    }

    @Override
    public PopUpWindow position(float x, float y) {
        return (PopUpWindow) super.position(x, y);
    }

    @Override
    protected void drawWindow(DrawContext context, int mouseX, int mouseY, float delta) {

        QuadColorState color = new QuadColorState(
                new Color(50, 39, 97, 255),
                new Color(42, 35, 74, 255),
                new Color(36, 32, 58, 255),
                new Color(36, 32, 54, 255)
        );

        WindowManager._renderBackground(x, y, width, height, 8, true, color);

        float y = this.y + 2;

        Builder.rectangle()
                .size(width - 16.5f, 1f)
                .color(ColorUtility.pack(255, 255, 255, (int) (100)))
                .radius(2)
                .build()
                .render(x + 9, y + 20);


        Builder.text()
                .size(7)
                .font(FontType.popins_medium.get())
                .text("Settings Popup")
                .color(ColorUtility.pack(255, 255, 255, (int) (200)))
                .build()
                .render(x + 12, y + 6);

        float height = 33;

        float settingY = y + 20;
        float settingX = this.x + 2;


        for (AbstractSettingComponent component : settingComponents) {
            if (component.isAvailable()) continue;

            component.position(settingX, settingY);
            component.width = width - 4;

            component.render(context, mouseX, mouseY, delta);

            settingY += component.height;
            height += component.height;
        }

        this.height = height;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (AbstractSettingComponent component : settingComponents) {
            if (component.isAvailable()) continue;
            if (component.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for(int i = 0; i < settingComponents.size(); i++){
            var component = settingComponents.get(i);
            if(component.isAvailable()) continue;
            if(component.mouseReleased(mouseX, mouseY, button)) return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        for(int i = 0; i < settingComponents.size(); i++){
            var component = settingComponents.get(i);
            if(component.isAvailable()) continue;
            if(component.charTyped(chr, modifiers)) return true;
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        for(int i = 0; i < settingComponents.size(); i++){
            var component = settingComponents.get(i);
            if(component.isAvailable()) continue;
            if(component.keyPressed(keyCode, scanCode, modifiers)) return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
