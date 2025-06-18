package win.blade.common.gui.impl.menu.component.setting;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;
import win.blade.common.gui.impl.menu.MenuScreen;
import win.blade.common.gui.impl.menu.component.SettingComponent;
import win.blade.common.gui.impl.menu.settings.impl.SliderSetting;
import win.blade.common.utils.math.animation.Easing;
import win.blade.common.utils.other.IMouse;
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.builders.states.QuadColorState;
import win.blade.common.utils.render.builders.states.QuadRadiusState;
import win.blade.common.utils.render.builders.states.SizeState;

import java.awt.Color;

public class SliderSettingComponent extends SettingComponent implements IMouse {

    private final SliderSetting sliderSetting;
    private boolean dragging;

    public SliderSettingComponent(MenuScreen parentScreen, SliderSetting setting) {
        super(parentScreen, setting);
        this.sliderSetting = setting;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta, float alpha) {
        super.render(context, mouseX, mouseY, delta, alpha);
        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();

        sliderSetting.getAnimation().update();
        sliderSetting.getAnimation().run((sliderSetting.getValue() - sliderSetting.min) / (sliderSetting.max - sliderSetting.min), 0.2, Easing.EASE_OUT_CUBIC);

        Builder.text()
                .font(font)
                .text(sliderSetting.getName())
                .size(7 * scale)
                .color(new Color(255, 255, 255, (int)(255 * alpha)))
                .build()
                .render(matrix, x, y + 9 * scale);

        Builder.text()
                .font(font)
                .text(String.format("%.1f", sliderSetting.getValue()))
                .size(7 * scale)
                .color(new Color(153, 154, 161, (int)(255 * alpha)))
                .build()
                .render(matrix, x + width - 18 * scale, y + 9 * scale);

        float sliderWidth = width - 4 * scale;
        float sliderY = y + 20 * scale;

        Builder.rectangle()
                .size(new SizeState(sliderWidth, 2 * scale))
                .radius(new QuadRadiusState(1 * scale))
                .color(new QuadColorState(new Color(45, 45, 50, (int)(255 * alpha))))
                .build()
                .render(matrix, x, sliderY);

        Builder.rectangle()
                .size(new SizeState((float) (sliderWidth * sliderSetting.getAnimation().get()), 2 * scale))
                .radius(new QuadRadiusState(1 * scale))
                .color(new QuadColorState(new Color(87, 90, 198, (int)(255 * alpha))))
                .build()
                .render(matrix, x, sliderY);

        float knobSize = dragging ?6  * scale : 5 * scale;
        float animatedX = x + (float) (sliderWidth * sliderSetting.getAnimation().get());

        Builder.rectangle()
                .size(new SizeState(knobSize, knobSize))
                .radius(new QuadRadiusState(knobSize * 0.3f))
                .color(new QuadColorState(new Color(255, 255, 255, (int)(255 * alpha))))
                .build()
                .render(matrix, animatedX - knobSize / 2, sliderY + 1 * scale - knobSize / 2);

        Builder.rectangle()
                .size(new SizeState(knobSize / 2, knobSize / 2))
                .radius(new QuadRadiusState(knobSize * 0.05f))
                .color(new QuadColorState(new Color(87, 90, 198, (int)(255 * alpha))))
                .build()
                .render(matrix, animatedX - knobSize / 4, sliderY + 1 * scale - knobSize / 4);

        if (dragging) {
            double diff = sliderSetting.max - sliderSetting.min;
            double val = sliderSetting.min + MathHelper.clamp((mouseX - x) / sliderWidth, 0, 1) * diff;
            sliderSetting.set((float) (Math.round(val * (1 / sliderSetting.increment)) / (1 / sliderSetting.increment)));
        }

        this.height = 30 * scale;
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (isLClick(button) && isHover(mouseX, mouseY, x, y + 15 * scale, width, 10 * scale)) {
            dragging = true;
            double value = sliderSetting.min + MathHelper.clamp((mouseX - x) / (width - 4 * scale), 0, 1) * (sliderSetting.max - sliderSetting.min);
            sliderSetting.set((float) (Math.round(value / sliderSetting.increment) * sliderSetting.increment));
        }
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
        dragging = false;
    }

    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers) {}
}