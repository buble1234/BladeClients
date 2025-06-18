package win.blade.common.gui.impl.menu.component.setting;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;
import win.blade.common.gui.impl.menu.MenuScreen;
import win.blade.common.gui.impl.menu.component.SettingComponent;
import win.blade.common.gui.impl.menu.settings.impl.ColorSetting;
import win.blade.common.utils.other.IMouse;
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.builders.states.QuadColorState;
import win.blade.common.utils.render.builders.states.QuadRadiusState;
import win.blade.common.utils.render.builders.states.SizeState;

import java.awt.Color;

public class ColorSettingComponent extends SettingComponent implements IMouse {

    private final ColorSetting colorSetting;
    private boolean draggingHue, draggingSaturation, draggingLightness;
    private float hue, saturation, lightness;

    public ColorSettingComponent(MenuScreen parentScreen, ColorSetting setting) {
        super(parentScreen, setting);
        this.colorSetting = setting;
        updateHSBFromColor();
    }

    private void updateHSBFromColor() {
        float[] hsb = Color.RGBtoHSB(
                (colorSetting.getValue() >> 16) & 0xFF,
                (colorSetting.getValue() >> 8) & 0xFF,
                colorSetting.getValue() & 0xFF,
                null
        );
        this.hue = hsb[0];
        this.saturation = hsb[1];
        this.lightness = hsb[2];
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta, float alpha) {
        super.render(context, mouseX, mouseY, delta, alpha);

        Builder.text()
                .font(font)
                .text(colorSetting.getName())
                .size(7 * scale)
                .color(new Color(255, 255, 255, (int) (255 * alpha)))
                .build()
                .render(x, y + 9 * scale);

        Builder.rectangle()
                .size(new SizeState(12 * scale, 12 * scale))
                .radius(new QuadRadiusState(3 * scale))
                .color(new QuadColorState(new Color(colorSetting.getValue(), true)))
                .build()
                .render(x + width - 18 * scale, y + 7 * scale);

        float sliderWidth = width - 4 * scale;
        float yOffset = y + 25 * scale;
        float sliderX = x;
        float sliderHeight = 4 * scale;
        float sliderMargin = 4 * scale;

        if (draggingHue) this.hue = MathHelper.clamp((float) (mouseX - sliderX) / sliderWidth, 0.0f, 1.0f);
        if (draggingSaturation) this.saturation = MathHelper.clamp((float) (mouseX - sliderX) / sliderWidth, 0.0f, 1.0f);
        if (draggingLightness) this.lightness = MathHelper.clamp((float) (mouseX - sliderX) / sliderWidth, 0.0f, 1.0f);

        if (draggingHue || draggingLightness || draggingSaturation) {
            colorSetting.set(Color.HSBtoRGB(hue, saturation, lightness));
        }

        for (int i = 0; i < (int) sliderWidth; i++) {
            Builder.rectangle()
                    .size(new SizeState(1, sliderHeight))
                    .color(new QuadColorState(new Color(Color.HSBtoRGB(i / sliderWidth, 1f, 1f))))
                    .radius(new QuadRadiusState(0f))
                    .build()
                    .render(sliderX + i, yOffset);
        }

        Color hueColor = Color.getHSBColor(hue, 1, 1);

        Builder.rectangle()
                .size(new SizeState(sliderWidth, sliderHeight))
                .radius(new QuadRadiusState(2f))
                .color(new QuadColorState(Color.WHITE, hueColor, Color.WHITE, hueColor))
                .build()
                .render(sliderX, yOffset + sliderHeight + sliderMargin);

        Builder.rectangle()
                .size(new SizeState(sliderWidth, sliderHeight))
                .radius(new QuadRadiusState(2f))
                .color(new QuadColorState(Color.BLACK, hueColor, Color.BLACK, hueColor))
                .build()
                .render(sliderX, yOffset + 2 * (sliderHeight + sliderMargin));

        float knobHeight = sliderHeight + 4 * scale;
        Builder.rectangle()
                .size(new SizeState(2 * scale, knobHeight))
                .radius(new QuadRadiusState(1 * scale))
                .color(new QuadColorState(Color.WHITE))
                .build()
                .render(sliderX + sliderWidth * hue - 1 * scale, yOffset - 2 * scale);

        Builder.rectangle()
                .size(new SizeState(2 * scale, knobHeight))
                .radius(new QuadRadiusState(1 * scale))
                .color(new QuadColorState(Color.WHITE))
                .build()
                .render(sliderX + sliderWidth * saturation - 1 * scale, yOffset + sliderHeight + sliderMargin - 2 * scale);

        Builder.rectangle()
                .size(new SizeState(2 * scale, knobHeight))
                .radius(new QuadRadiusState(1 * scale))
                .color(new QuadColorState(Color.WHITE))
                .build()
                .render(sliderX + sliderWidth * lightness - 1 * scale, yOffset + 2 * (sliderHeight + sliderMargin) - 2 * scale);

        this.height = (yOffset - y) + 3 * sliderHeight + 2 * sliderMargin + 6 * scale;
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (!isLClick(button)) return;

        float sliderWidth = width - 4 * scale;
        float yOffset = y + 25 * scale;
        float sliderHeight = 4 * scale;
        float sliderMargin = 4 * scale;

        draggingHue = isHover(mouseX, mouseY, x, yOffset, sliderWidth, sliderHeight);
        draggingSaturation = isHover(mouseX, mouseY, x, yOffset + sliderHeight + sliderMargin, sliderWidth, sliderHeight);
        draggingLightness = isHover(mouseX, mouseY, x, yOffset + 2 * (sliderHeight + sliderMargin), sliderWidth, sliderHeight);
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
        draggingHue = false;
        draggingSaturation = false;
        draggingLightness = false;
    }

    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers) {
    }
}