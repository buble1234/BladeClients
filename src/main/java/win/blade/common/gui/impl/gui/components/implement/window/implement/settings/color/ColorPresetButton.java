package win.blade.common.gui.impl.gui.components.implement.window.implement.settings.color;

import net.minecraft.client.gui.DrawContext;
import win.blade.common.gui.impl.gui.components.AbstractComponent;
import win.blade.common.gui.impl.gui.setting.implement.ColorSetting;
import win.blade.common.utils.math.MathUtility;
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.builders.states.QuadColorState;
import win.blade.common.utils.render.builders.states.QuadRadiusState;
import win.blade.common.utils.render.builders.states.SizeState;

import java.awt.Color;

public class ColorPresetButton extends AbstractComponent {
    private final ColorSetting setting;
    private final int color;

    public ColorPresetButton(ColorSetting setting, int color) {
        this.setting = setting;
        this.color = color;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        Builder.rectangle()
                .size(new SizeState(8, 8))
                .color(new QuadColorState(new Color(color, true)))
                .radius(new QuadRadiusState(2))
                .build()
                .render(x, y);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (MathUtility.isHovered(mouseX, mouseY, x, y, 8, 8) && button == 0) {
            setting.setColor(color);
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}