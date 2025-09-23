package win.blade.common.gui.impl.gui.components.implement.module;

import net.minecraft.client.gui.DrawContext;
import org.joml.Matrix4f;
import win.blade.common.gui.impl.gui.components.AbstractComponent;
import win.blade.common.gui.impl.gui.components.implement.other.CheckComponent;
import win.blade.common.gui.impl.gui.components.implement.other.SettingComponent;
import win.blade.common.gui.impl.gui.components.implement.settings.AbstractSettingComponent;
import win.blade.common.gui.impl.gui.components.implement.window.AbstractWindow;
import win.blade.common.gui.impl.gui.components.implement.window.implement.module.BindWindow;
import win.blade.common.gui.impl.gui.components.implement.window.implement.module.ModuleBindWindow;
import win.blade.common.gui.impl.gui.setting.SettingComponentAdder;
import win.blade.common.utils.color.ColorUtility;
import win.blade.common.utils.keyboard.Keyboard;
import win.blade.common.utils.math.MathUtility;
import win.blade.common.utils.minecraft.ChatUtility;
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.builders.states.QuadColorState;
import win.blade.common.utils.render.builders.states.QuadRadiusState;
import win.blade.common.utils.render.builders.states.SizeState;
import win.blade.common.utils.render.msdf.FontType;
import win.blade.common.utils.render.msdf.MsdfFont;
import win.blade.core.module.api.Module;


import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ModuleComponent extends AbstractComponent {
    private final List<AbstractSettingComponent> components = new ArrayList<>();

    private final Module module;

    private final MsdfFont fontRegular = FontType.popins_regular.get();

    public ModuleComponent(Module module) {
        this.module = module;

        new SettingComponentAdder().addSettingComponent(
                module.settings(),
                components
        );
    }

    public List<AbstractComponent> getSettingComponents() {
        return new ArrayList<>(components);
    }

    public List<AbstractSettingComponent> getComponents() {
        return components;
    }

    public Module getModule() {
        return module;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        width = 142.5f;
        Matrix4f positionMatrix = context
                .getMatrices()
                .peek()
                .getPositionMatrix();

        height = getComponentHeight();
        Builder.rectangle()
                .size(new SizeState(width, height))
                .color(new QuadColorState(new Color(21,19,32)))
                .radius(new QuadRadiusState(6))
                .build()
                .render(x, y);


        Builder.rectangle()
                .size(new SizeState(width, 18.5f))
                .color(module.isEnabled() ? new QuadColorState(new Color(23,19,39), ColorUtility.fromHex("291B58")) : new QuadColorState(new Color(23,19,39)))
                .radius(new QuadRadiusState(6, 0, 0, 6))
                .build()
                .render(x, y);

        Builder.border()
                .size(new SizeState(width - 0.5f, height - 0.5f))
                .color(new QuadColorState(
                        new Color(255, 255, 255, 5),
                        new Color(255, 255, 255, 5),
                        new Color(255, 255, 255, 10),
                        new Color(255, 255, 255, 10)
                ))
                .thickness(0.5f)
                .radius(new QuadRadiusState(6))
                .build()
                .render(x + 0.25f, y + 0.25f);


        Builder.rectangle()
                .size(new SizeState(width, 1))
                .color(new QuadColorState(
                        new Color(255, 255, 255, 10),
                        new Color(255, 255, 255, 10),
                        new Color(255, 255, 255, 35),
                        new Color(255, 255, 255, 35)
                ))
                .radius(new QuadRadiusState(0))
                .build()
                .render(x, y + 18f);


        Builder.border()
                .size(new SizeState(width, height))
                .color(new QuadColorState(new Color(255,255,255,15)))
                .radius(new QuadRadiusState(6))
                .outlineColor(new QuadColorState(255,255,255,0))
                .build()
                .render(x, y);


        Builder.text()
                .font(FontType.popins_medium.get())
                .text(module.name())
                .size(5.5f)
                .color(new Color(0xFFD4D6E1))
                .build()
                .render( x + 9, y + 5);

        drawBind(context, positionMatrix);

        float offset = y + 20f;
        for (AbstractSettingComponent component : components) {
            var visible = component.getSetting().getVisible();
            if (visible != null && !visible.get()) {
                continue;
            }

            component.x = x;
            component.y = offset;
            component.width = width;
            component.render(context, mouseX, mouseY, delta);

            offset += component.height + 1;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        String bindName = Keyboard.getKeyName(module.keybind());
        float stringWidth = fontRegular.getWidth(bindName, 4);
        float bindX = x + width - stringWidth - 15;
        float bindY = y + 4f;
        float bindWidth = stringWidth + 4.8f;
        float bindHeight = 8.5f;

        if (MathUtility.isHovered(mouseX, mouseY, bindX, bindY, bindWidth, bindHeight) && button == 0) {
            spawnWindow((int)mouseX, (int)mouseY);
            return true;
        }

        if (MathUtility.isHovered(mouseX, mouseY, x, y, width, 18.5f)) {
            if (button == 0) {
                module.toggleWithoutNotification(!module.isEnabled());
                return true;
            } else if (button == 2) {
                spawnWindow((int)mouseX, (int)mouseY);
                return true;
            }
        }

        for (int i = components.size() - 1; i >= 0; i--) {
            AbstractSettingComponent component = components.get(i);
            var visible = component.getSetting()
                    .getVisible();
            if (visible != null && !visible.get()) {
                continue;
            }

            if(component.mouseClicked(mouseX, mouseY, button)){
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isHover(double mouseX, double mouseY) {
        for (AbstractComponent abstractComponent : components) {
            if (abstractComponent.isHover(mouseX, mouseY)) {
                return true;
            }
        }
        return MathUtility.isHovered(mouseX, mouseY, x, y, width, height);
    }

    @Override
    public void tick() {
        for (AbstractComponent component : components) {
            component.tick();
        }
        super.tick();
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        components.forEach(abstractComponent -> abstractComponent.mouseDragged(mouseX, mouseY, button, deltaX, deltaY));
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        components.forEach(abstractComponent -> abstractComponent.mouseReleased(mouseX, mouseY, button));
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        components.forEach(abstractComponent -> abstractComponent.mouseScrolled(mouseX, mouseY, amount));
        return super.mouseScrolled(mouseX, mouseY, amount);
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

            offsetY += component.height + 1;
        }
        return (int) (offsetY + 28);
    }

    private void drawBind(DrawContext context, Matrix4f positionMatrix) {
        String bindName = Keyboard.getKeyName(module.keybind());
        float stringWidth = fontRegular.getWidth(bindName, 4);

        Builder.rectangle()
                .size(new SizeState(stringWidth + 4.8, 8.5f))
                .color(new QuadColorState(new Color(28,26,37,255)))
                .radius(new QuadRadiusState(3f))
                .build()
                .render(x + width - stringWidth - 15, y + 4f);


        Builder.text()
                .font(fontRegular)
                .text(bindName)
                .size(3.5f)
                .color(ColorUtility.fromHex(module.keybind() == -1 ? "8C889A" : "663CFF"))
                .build()
                .render( x + width - 12.5f - stringWidth, y + 5.75f);
    }

    private void spawnWindow(int mouseX, int mouseY) {
        var existingWindow = windowManager.findWindow("bindWindow");

        if (existingWindow != null) {
            windowManager.delete(existingWindow);
        }

        AbstractWindow moduleBindWindow = new BindWindow(module)
                .position(mouseX + 5, mouseY + 5)
                .size(105, 73)
                .draggable(true);

        windowManager.add(moduleBindWindow);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ModuleComponent that = (ModuleComponent) o;
        return module.equals(that.module);
    }

    @Override
    public int hashCode() {
        return Objects.hash(module);
    }
}