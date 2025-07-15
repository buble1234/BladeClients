package win.blade.common.gui.impl.gui.components.implement.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import org.joml.Matrix4f;
import win.blade.common.gui.impl.gui.components.AbstractComponent;
import win.blade.common.gui.impl.gui.components.implement.other.CheckComponent;
import win.blade.common.gui.impl.gui.components.implement.other.SettingComponent;
import win.blade.common.gui.impl.gui.components.implement.settings.AbstractSettingComponent;
import win.blade.common.gui.impl.gui.components.implement.window.AbstractWindow;
import win.blade.common.gui.impl.gui.components.implement.window.implement.module.ModuleBindWindow;
import win.blade.common.gui.impl.gui.setting.SettingComponentAdder;
import win.blade.common.utils.keyboard.Keyboard;
import win.blade.common.utils.math.MathUtility;
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

    private final CheckComponent checkComponent = new CheckComponent();
    private final SettingComponent settingComponent = new SettingComponent();

    private final Module module;

    private final MsdfFont fontRegular = FontType.popins_regular.get();

    public ModuleComponent(Module module) {
        this.module = module;

        new SettingComponentAdder().addSettingComponent(
                module.settings(),
                components
        );
    }

    public List<AbstractSettingComponent> getComponents() {
        return components;
    }

    public CheckComponent getCheckComponent() {
        return checkComponent;
    }

    public SettingComponent getSettingComponent() {
        return settingComponent;
    }

    public Module getModule() {
        return module;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
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
                .size(new SizeState(width, 18))
                .color(new QuadColorState(new Color(23,19,39)))
                .radius(new QuadRadiusState(6, 0, 0, 6))
                .build()
                .render(x, y);





        Builder.border()
                .size(new SizeState(width, height))
                .color(new QuadColorState(new Color(255,255,255,15)))
                .radius(new QuadRadiusState(6))
                .outlineColor(new QuadColorState(255,255,255,0))
                .build()
                .render(x, y);


        Builder.text()
                .font(fontRegular)
                .text(module.name())
                .size(6)
                .color(new Color(0xFFD4D6E1))
                .build()
                .render( x + 11, y + 6);

        Builder.text()
                .font(fontRegular)
                .text("Enable")
                .size(6)
                .color(new Color(0xFFD4D6E1))
                .build()
                .render( x + 9, y + 25);

        Builder.text()
                .font(fontRegular)
                .text("Description")
                .size(5)
                .color(new Color(0xFF878894))
                .build()
                .render( x + 9, y + 32);

        ((CheckComponent) checkComponent.position(x + width - 16, y + 28))
                .setRunnable(() -> module.toggleWithoutNotification(!module.isEnabled()))
                .setState(module.isEnabled())
                .render(context, mouseX, mouseY, delta);


        ((SettingComponent) settingComponent.position(x + width - 28, y + 28.5F))
                .setRunnable(() -> spawnWindow(mouseX, mouseY))
                .render(context, mouseX, mouseY, delta);

        drawBind(context, positionMatrix);

        float offset = y + 42;
        for (int i = components.size() - 1; i >= 0; i--) {
            AbstractSettingComponent component = components.get(i);

            var visible = component.getSetting()
                    .getVisible();

            if (visible != null && !visible.get()) {
                continue;
            }

            component.x = x;
            component.y = offset + (getComponentHeight() - 46 - component.height);
            component.width = width;

            component.render(context, mouseX, mouseY, delta);

            offset -= component.height;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {

        checkComponent.mouseClicked(mouseX, mouseY, button);
        settingComponent.mouseClicked(mouseX, mouseY, button);

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

            offsetY += component.height;
        }
        return (int) (offsetY + 46);
    }

    private void drawBind(DrawContext context, Matrix4f positionMatrix) {
        String bindName = Keyboard.getKeyName(module.keybind());
        float stringWidth = fontRegular.getWidth(bindName, 5);

        Builder.rectangle()
                .size(new SizeState(stringWidth + 8, 9))
                .color(new QuadColorState(new Color(28,26,37,255)))
                .radius(new QuadRadiusState(4))
                .build()
                .render(x + width - stringWidth - 16, y + 2.5f);

        Builder.border()
                .size(new SizeState(stringWidth + 8, 12))
                .color(new QuadColorState(new Color(255,255,255,15)))
                .radius(new QuadRadiusState(4))
                .outlineColor(new QuadColorState(255,255,255,0))
                .thickness(0.3f)
                .build()
                .render(x + width - stringWidth - 16, y + 2.5f);



        Builder.text()
                .font(fontRegular)
                .text(bindName)
                .size(5)
                .color(new Color(102,60,255))
                .build()
                .render( x + width - 12 - stringWidth, y + 6);
    }

    private void spawnWindow(int mouseX, int mouseY) {
        AbstractWindow existingWindow = null;

        for (AbstractWindow window : windowManager.getWindows()) {
            if (window instanceof ModuleBindWindow) {
                existingWindow = window;
                break;
            }
        }

        if (existingWindow != null) {
            windowManager.delete(existingWindow);
        } else {
            AbstractWindow moduleBindWindow = new ModuleBindWindow(module)
                    .position(mouseX + 5, mouseY + 5)
                    .size(105, 55)
                    .draggable(false);

            windowManager.add(moduleBindWindow);
        }
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