package win.blade.common.gui.impl.gui.components.implement.settings.select;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.util.Identifier;
import win.blade.common.gui.impl.gui.components.AbstractComponent;
import win.blade.common.gui.impl.gui.setting.implement.SelectSetting;
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

        int opacity = (int) alphaAnimation.get();

        int selectedOpacity = applyOpacity(
                applyOpacity(0xFF2D2E41, opacity),
                alpha
        );

        if ((selectedOpacity & 0xFF000000) != 0) {
            Builder.rectangle()
                    .size(new SizeState(width, height))
                    .color(new QuadColorState(new Color(selectedOpacity, true)))
                    .build()
                    .render(x, y);
        }

        int checkOpacity = applyOpacity(
                applyOpacity(Color.WHITE.getRGB(), Math.min(255, opacity * 5)),
                alpha
        );

        if ((checkOpacity & 0xFF000000) != 0) {
            AbstractTexture checkTexture = MinecraftClient.getInstance().getTextureManager().getTexture(Identifier.of("blade", "textures/check.png"));
            if (checkTexture != null) {
                Builder.texture()
                        .size(new SizeState(4, 4))
                        .color(new QuadColorState(new Color(checkOpacity, true)))
                        .texture(0f, 0f, 1f, 1f, checkTexture)
                        .build()
                        .render(x + width - 8, y + 4.5f);
            }
        }

        Builder.text()
                .font(fontRegular)
                .text(text)
                .size(6)
                .color(new Color(applyOpacity(0xFFD4D6E1, alpha), true))
                .build()
                .render(x + 4, y + 5);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (MathUtility.isHovered(mouseX, mouseY, x, y, width, height) && button == 0) {
            setting.setSelected(text);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}