package win.blade.common.gui.impl.gui.components.implement.window.implement.settings.color.component;

import net.minecraft.client.gui.DrawContext;
import win.blade.common.gui.impl.gui.components.AbstractComponent;
import win.blade.common.gui.impl.gui.components.implement.window.implement.settings.color.ColorPresetButton;
import win.blade.common.gui.impl.gui.setting.implement.ColorSetting;
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.msdf.FontType;
import win.blade.common.utils.render.msdf.MsdfFont;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class ColorPresetComponent extends AbstractComponent {
    private final List<ColorPresetButton> colorPresetButtonList = new ArrayList<>();
    private final ColorSetting setting;
    private float windowHeight;
    private final MsdfFont fontRegular = FontType.sf_regular.get();

    public ColorPresetComponent(ColorSetting setting) {
        this.setting = setting;
        if (setting.getPresets() != null) {
            for (int preset : setting.getPresets()) {
                colorPresetButtonList.add(new ColorPresetButton(setting, preset));
            }
        }
    }

    public float getWindowHeight() {
        return windowHeight;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (!colorPresetButtonList.isEmpty()) {
            Builder.text().font(fontRegular).text("Presets").size(5.5f).color(Color.WHITE).build().render(x + 6, y + 112);
        }

        int xOffset = 0, yOffset = 0;
        int colorIndex = 0;
        int size = 13;

        for (ColorPresetButton button : colorPresetButtonList) {
            button.x = x + 6 + xOffset;
            button.y = y + 120 + yOffset;
            button.render(context, mouseX, mouseY, delta);
            xOffset += size;
            colorIndex++;
            if (colorIndex >= 11) {
                colorIndex = 0;
                xOffset = 0;
                yOffset += size - 1;
            }
        }

        windowHeight = colorPresetButtonList.isEmpty() ? 132 : 156 + yOffset - (float) yOffset / 2;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        colorPresetButtonList.forEach(colorPresetButton -> colorPresetButton.mouseClicked(mouseX, mouseY, button));
        return super.mouseClicked(mouseX, mouseY, button);
    }
}