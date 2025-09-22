package win.blade.common.gui.impl.gui.components.implement.settings.multiselect;

import net.minecraft.client.gui.DrawContext;
import win.blade.common.gui.impl.gui.components.AbstractComponent;
import win.blade.common.gui.impl.gui.setting.implement.MultiSelectSetting;
import win.blade.common.utils.math.MathUtility;
import win.blade.common.utils.math.animation.Animation;
import win.blade.common.utils.math.animation.Easing;
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.builders.states.SizeState;
import win.blade.common.utils.render.msdf.FontType;
import win.blade.common.utils.render.msdf.MsdfFont;

import java.awt.Color;

public class MultiSelectedButton extends AbstractComponent {
    private final MultiSelectSetting setting;
    private final String text;
    private int alpha;
    private final Animation alphaAnimation = new Animation();
    private final MsdfFont fontRegular = FontType.sf_regular.get();

    public MultiSelectedButton(MultiSelectSetting setting, String text) {
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

        Color selectedColor = new Color(255, 255, 255, alpha);
        Color unselectedColor = new Color(175, 174, 178, alpha);

        Builder.text()
                .font(fontRegular)
                .text(text)
                .size(4)
                .color(setting.isSelected(text) ? selectedColor : unselectedColor)
                .build()
                .render(x + 4.75f, y + 5);

        if(isFirst) return;

        Builder.rectangle()
                .size(new SizeState(width - 8, 0.65f))
                .radius(0)
                .color(new Color(255, 255, 255, (int)(76 * (alpha / 255.0f))).getRGB())
                .build()
                .render(x + 4, y + 2.9f);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (alpha > 200 && MathUtility.isHovered(mouseX, mouseY, x + 4, y + 5, fontRegular.getWidth(text, 4) + 2, height) && button == 0) {
            setting.toggle(text);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}