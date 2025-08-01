package win.blade.common.gui.impl.gui.components.implement.window.implement.settings.group;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;
import win.blade.common.gui.impl.gui.components.AbstractComponent;
import win.blade.common.gui.impl.gui.components.implement.settings.AbstractSettingComponent;
import win.blade.common.gui.impl.gui.components.implement.window.AbstractWindow;
import win.blade.common.gui.impl.gui.components.implement.window.WindowManager;
import win.blade.common.gui.impl.gui.setting.SettingComponentAdder;
import win.blade.common.gui.impl.gui.setting.implement.GroupSetting;
import win.blade.common.utils.math.MathUtility;
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.builders.states.QuadColorState;
import win.blade.common.utils.render.builders.states.QuadRadiusState;
import win.blade.common.utils.render.builders.states.SizeState;
import win.blade.common.utils.render.msdf.FontType;
import win.blade.common.utils.render.msdf.MsdfFont;


import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class GroupWindow extends AbstractWindow {
    private final List<AbstractSettingComponent> components = new ArrayList<>();
    private final GroupSetting setting;
    
    private final MsdfFont fontRegular = FontType.sf_regular.get();

    public GroupWindow(GroupSetting setting) {
        this.setting = setting;

        new SettingComponentAdder().addSettingComponent(
                setting.getSubSettings(),
                components
        );
    }

    public List<AbstractSettingComponent> getComponents() {
        return components;
    }

    public GroupSetting getSetting() {
        return setting;
    }

    @Override
    public void drawWindow(DrawContext context, int mouseX, int mouseY, float delta) {
        height = MathHelper.clamp(getComponentHeight(), 0, 200);
//
//        Builder.rectangle()
//                .size(new SizeState(width, height))
//                .color(new QuadColorState(new Color(0x32000000)))
//                .radius(new QuadRadiusState(12))
//                .build()
//                .render(x, y);

//        Builder.rectangle()
//                .size(new SizeState(width, height))
//                .color(new QuadColorState(new Color(0xFF191A28)))
//                .radius(new QuadRadiusState(12))
//                .build()
//                .render(x, y);

        WindowManager._renderBackground(x, y, width, height, 12, true, null);

        Builder.text()
                .font(fontRegular)
                .text("Settings " + setting.getName())
                .size(7)
                .color(Color.WHITE)
                .build()
                .render( x + 9, y + 10);

        boolean isLimitedHeight = MathHelper.clamp(height, 0, 200) == 200;

        float offset = 0;
        int totalHeight = 0;
        for (int i = components.size() - 1; i >= 0; i--) {
            AbstractSettingComponent component = components.get(i);

            var visible = component.getSetting()
                    .getVisible();

            if (visible != null && !visible.get()) {
                continue;
            }

            float componentY = (float) (y + 19 + offset + (getComponentHeight() - 25 - component.height) + smoothedScroll);
            
            if (!isLimitedHeight || (componentY >= y + 23 && componentY <= y + height - 5)) {
                component.x = x;
                component.y = componentY;
                component.width = width;
                component.render(context, mouseX, mouseY, delta);
            }

            offset -= component.height;
            totalHeight += (int) component.height;
        }

        int maxScroll = (int) Math.max(0, totalHeight - (height - 23));
        int clamped = MathHelper.clamp(maxScroll, 0, maxScroll);
        scroll = MathHelper.clamp(scroll, -clamped, 0);
        smoothedScroll = MathHelper.lerp(0.1F, smoothedScroll, scroll);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        draggable(MathUtility.isHovered(mouseX, mouseY, x, y, width, 19) && button == 0);

        boolean isAnyComponentHovered = components
                .stream()
                .anyMatch(abstractComponent -> abstractComponent.isHover(mouseX, mouseY));

        if (isAnyComponentHovered) {
            components.forEach(abstractComponent -> {
                if (abstractComponent.isHover(mouseX, mouseY)) {
                    abstractComponent.mouseClicked(mouseX, mouseY, button);
                }
            });
            return super.mouseClicked(mouseX, mouseY, button);
        }

        components.forEach(abstractComponent -> abstractComponent.mouseClicked(mouseX, mouseY, button));
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isHover(double mouseX, double mouseY) {
        components.forEach(abstractComponent -> abstractComponent.isHover(mouseX, mouseY));

        for (AbstractComponent abstractComponent : components) {
            if (abstractComponent.isHover(mouseX, mouseY)) {
                return true;
            }
        }
        return super.isHover(mouseX, mouseY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        components.forEach(abstractComponent -> abstractComponent.mouseReleased(mouseX, mouseY, button));
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        boolean scrolled = MathHelper.clamp(height, 0, 200) == 200 && MathUtility.isHovered(mouseX, mouseY, x, y, width, height);
        if (scrolled) {
            scroll += amount * 20;
        }
        components.forEach(abstractComponent -> abstractComponent.mouseScrolled(mouseX, mouseY, amount));
        return scrolled;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        components.forEach(abstractComponent -> abstractComponent.keyPressed(keyCode, scanCode, modifiers));
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        components.forEach(abstractComponent -> abstractComponent.charTyped(chr, modifiers));
        return super.charTyped(chr, modifiers);
    }

    public int getComponentHeight() {
        float offsetY = 0;
        for (AbstractSettingComponent component : components) {
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