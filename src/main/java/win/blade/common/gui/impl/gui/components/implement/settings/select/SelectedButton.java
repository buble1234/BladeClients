package win.blade.common.gui.impl.gui.components.implement.settings.select;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.util.Identifier;
import win.blade.common.gui.impl.gui.components.AbstractComponent;
import win.blade.common.gui.impl.gui.setting.implement.SelectSetting;
import win.blade.common.utils.color.ColorUtility;
import win.blade.common.utils.math.MathUtility;
import win.blade.common.utils.math.animation.Animation;
import win.blade.common.utils.math.animation.Easing;
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.builders.states.QuadColorState;
import win.blade.common.utils.render.builders.states.SizeState;
import win.blade.common.utils.render.msdf.FontType;
import win.blade.common.utils.render.msdf.MsdfFont;

import java.awt.Color;

import static win.blade.common.utils.color.ColorUtility.applyOpacity;

public class SelectedButton extends AbstractComponent {
    private final SelectSetting setting;
    private final String text;
    private int alpha;
    private final Animation alphaAnimation = new Animation();
    private final MsdfFont fontRegular = FontType.sf_regular.get();

    public SelectedButton(SelectSetting setting, String text) {
        this.setting = setting;
        this.text = text;
        alphaAnimation.set(0);
    }

    public void setAlpha(int alpha) {
        this.alpha = alpha;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        alphaAnimation.update();

        if (setting.isSelected(text)) {
            alphaAnimation.run(0x2D, 0.4, Easing.EASE_OUT_EXPO);
        } else {
            alphaAnimation.run(0, 0.4, Easing.EASE_OUT_EXPO);
        }

        boolean isFirst = setting.getList().get(0).equalsIgnoreCase(text);

        Builder.text()
                .font(fontRegular)
                .text(text)
                .size(4)
                .color(setting.isSelected(text) ? new Color(255, 255, 255, 255) : new Color(175, 174, 178, 255))
                .build()
                .render(x + 4.75f, y + 5);


        if(isFirst) return;

        Builder.rectangle()
                .size(new SizeState(width - 8, 0.65f))
                .radius(0)
                .color(ColorUtility.pack(255, 255, 255, 76))
                .build()
                .render(x + 4, y + 2.9f);

    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (MathUtility.isHovered(mouseX, mouseY, x + 4, y + 5, fontRegular.getWidth(text, 4) + 2, height) && button == 0) {
            setting.setSelected(text);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}