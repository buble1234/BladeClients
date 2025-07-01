

        package win.blade.common.gui.impl.gui.components.implement.window.implement.settings.color.component;


import net.minecraft.client.gui.DrawContext;
import win.blade.common.gui.impl.gui.components.AbstractComponent;
import win.blade.common.gui.impl.gui.setting.implement.ColorSetting;
import win.blade.common.utils.math.MathUtility;
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.builders.states.QuadColorState;
import win.blade.common.utils.render.builders.states.QuadRadiusState;
import win.blade.common.utils.render.builders.states.SizeState;

import java.awt.Color;

import static net.minecraft.util.math.MathHelper.clamp;

public class HueComponent extends AbstractComponent {
    private final ColorSetting setting;
    private boolean hueDragging;

    private float X, Y, W, H;

    public HueComponent(ColorSetting setting) {
        this.setting = setting;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        X = x + 6;
        Y = y + 18.5F;
        W = 138;
        H = 50;

        int hueColor = Color.HSBtoRGB(setting.getHue(), 1, 1);


        Builder.rectangle()
                .size(new SizeState(W, H))
                .color(new QuadColorState(
                        Color.WHITE.getRGB(),
                        Color.WHITE.getRGB(),
                        hueColor,
                        hueColor
                ))
                .radius(new QuadRadiusState(4))
                .build()
                .render(X, Y);

        Builder.rectangle()
                .size(new SizeState(W, H))
                .color(new QuadColorState(
                        0x00000000,
                        Color.BLACK.getRGB(),
                        Color.BLACK.getRGB(),
                        0x00000000
                ))
                .radius(new QuadRadiusState(4))
                .build()
                .render(X, Y);



        float clampedX = X + W * setting.getSaturation();
        float clampedY = Y + H * (1 - setting.getBrightness());

        Builder.rectangle()
                .size(new SizeState(5, 5))
                .color(new QuadColorState(Color.WHITE))
                .radius(new QuadRadiusState(1.5f))
                .build()
                .render(clampedX - 2.5f, clampedY - 2.5f);
        Builder.rectangle()
                .size(new SizeState(3, 3))
                .color(new QuadColorState(new Color(setting.getColorWithAlpha(), true)))
                .radius(new QuadRadiusState(0))
                .build()
                .render(clampedX - 1.5f, clampedY - 1.5f);


        if (hueDragging) {
            setting.setSaturation(clamp((mouseX - X) / W, 0, 1));
            setting.setBrightness(clamp(1 - ((mouseY - Y) / H), 0, 1));
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        hueDragging = button == 0 && MathUtility.isHovered(mouseX, mouseY, X, Y, W, H);
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        hueDragging = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }


}