package win.blade.common.gui.impl.menu.component.setting;

import net.minecraft.client.gui.DrawContext;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;
import win.blade.common.gui.impl.menu.MenuScreen;
import win.blade.common.gui.impl.menu.component.SettingComponent;
import win.blade.common.gui.impl.menu.helpers.TextBox;
import win.blade.common.gui.impl.menu.settings.impl.StringSetting;
import win.blade.common.utils.other.IMouse;
import win.blade.common.utils.other.TextAlign;
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.builders.states.QuadColorState;
import win.blade.common.utils.render.builders.states.QuadRadiusState;
import win.blade.common.utils.render.builders.states.SizeState;

import java.awt.Color;

public class StringSettingComponent extends SettingComponent implements IMouse {

    private final StringSetting stringSetting;
    private final TextBox textBox;

    public StringSettingComponent(MenuScreen parentScreen, StringSetting setting) {
        super(parentScreen, setting);
        this.stringSetting = setting;
        this.textBox = new TextBox(0, 0, 0, font, 7 * scale, Color.WHITE.getRGB(), TextAlign.LEFT, "...", setting.onlyNumbers, false);
        this.textBox.setText(setting.getValue());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta, float alpha) {
        super.render(context, mouseX, mouseY, delta, alpha);
        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();

        Builder.text()
                .font(font)
                .text(stringSetting.getName())
                .size(7 * scale)
                .color(new Color(255, 255, 255, (int)(255 * alpha)))
                .build()
                .render(matrix, x, y + 9 * scale);

        float boxX = x;
        float boxY = y + 18 * scale;
        float boxWidth = width - 4 * scale;
        float boxHeight = 12 * scale;

        Builder.rectangle()
                .size(new SizeState(boxWidth, boxHeight))
                .radius(new QuadRadiusState(3 * scale))
                .color(new QuadColorState(new Color(45, 45, 50, (int)(150 * alpha))))
                .build()
                .render(matrix, boxX, boxY);

        textBox.x = boxX + 3 * scale;
        textBox.y = boxY + 2 * scale;
        textBox.width = boxWidth - 6 * scale;
        textBox.draw(context, alpha);

        this.height = (boxY - y) + boxHeight + 4 * scale;
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        boolean previouslySelected = textBox.selected;

        textBox.mouseClicked(mouseX, mouseY, button);

        if (previouslySelected && !textBox.selected) {
            stringSetting.set(textBox.getText());
        }
    }

    public void charTyped(char chr, int modifiers) {
        if (textBox.selected) {
            textBox.charTyped(chr, modifiers);
        }
    }

    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        if (textBox.selected) {
            textBox.keyPressed(keyCode, scanCode, modifiers);
            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                stringSetting.set(textBox.getText());
                textBox.selected = false;
            } else if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                textBox.setText(stringSetting.getValue());
                textBox.selected = false;
            }
        }
    }

    public boolean isTyping() {
        return textBox.selected;
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {}
}