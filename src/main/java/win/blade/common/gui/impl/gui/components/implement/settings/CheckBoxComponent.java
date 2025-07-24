package win.blade.common.gui.impl.gui.components.implement.settings;

import net.minecraft.client.gui.DrawContext;
import win.blade.common.gui.impl.gui.components.AbstractComponent;
import win.blade.common.gui.impl.gui.components.Component;
import win.blade.common.gui.impl.gui.components.implement.other.CheckComponent;
import win.blade.common.gui.impl.gui.setting.CheckBox;
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.builders.states.QuadColorState;
import win.blade.common.utils.render.builders.states.QuadRadiusState;
import win.blade.common.utils.render.builders.states.SizeState;

import java.awt.*;
import java.util.List;

/**
 * Автор Ieo117
 * Дата создания: 21.07.2025, в 21:11:42
 */
public class CheckBoxComponent extends AbstractSettingComponent {
    List<AbstractSettingComponent> settingComponents;
    public CheckComponent checkComponent;
    boolean open = false;
    public CheckBox box;
    public float smoothedScroll = 0;

    public CheckBoxComponent(CheckBox checkBox) {
        super(checkBox);

        box = checkBox;
        checkComponent = new CheckComponent();
        settingComponents = checkBox.getChildren()
                .stream()
                .map(Component::getBySetting)
                .toList();

    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        ((CheckComponent) checkComponent.position(x + width - 16, y + 28))
                .setRunnable(() -> open = !open)
                .setState(open)
                .render(context, mouseX, mouseY, delta);

        if(open){
            Builder.rectangle()
                    .size(new SizeState(width, height))
                    .color(new QuadColorState(new Color(21,19,32)))
                    .radius(new QuadRadiusState(6))
                    .build()
                    .render(x, y);

            Builder.rectangle()
                    .size(new SizeState(width, 18.5f))
                    .color(new QuadColorState(new Color(23,19,39)))
                    .radius(new QuadRadiusState(6, 0, 0, 6))
                    .build()
                    .render(x, y);

            float startX = x;
            float startY = y;

            float offset = 0;
            float componentWidth = 32;
            float totalHeight = 8;

            for (var settingComponent : settingComponents) {

                var visible = settingComponent.getSetting()
                        .getVisible();

                if (visible != null && !visible.get()) {
                    continue;
                }

                var componentY = startY + 19 + offset + (getComponentHeight() - 25 - settingComponent.height) + smoothedScroll;

                settingComponent.position(startX, componentY);
                settingComponent.width = componentWidth;

                settingComponent.render(context, mouseX, mouseY, delta);

                offset -= settingComponent.height;
                totalHeight += settingComponent.height;
            }


            height = totalHeight;
        }
    }




    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean click = checkComponent.mouseClicked(mouseX, mouseY, button);

        if(click) return true;

        if(open){
            for (AbstractSettingComponent settingComponent : settingComponents) {
                if(settingComponent.mouseClicked(mouseX, mouseY, button)) return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }


    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if(open){
            for (AbstractSettingComponent settingComponent : settingComponents) {
                if(settingComponent.mouseReleased(mouseX, mouseY, button)) return true;
            }
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if(open){
            for (AbstractSettingComponent settingComponent : settingComponents) {
                if(settingComponent.keyPressed(keyCode, scanCode, modifiers)) return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if(open){
            for (AbstractSettingComponent settingComponent : settingComponents) {
                if(settingComponent.charTyped(chr, modifiers)) return true;
            }
        }
        return super.charTyped(chr, modifiers);
    }


    public int getComponentHeight() {
        float offsetY = 0;
        for (AbstractSettingComponent component : settingComponents) {
            var visible = component.getSetting()
                    .getVisible();

            if (visible != null && !visible.get()) {
                continue;
            }

            offsetY += component.height;
        }
        return (int) (offsetY + 25);
    }
}