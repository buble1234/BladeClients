package win.blade.common.gui.impl.gui.components.implement.settings.multiselect;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.util.Identifier;
import win.blade.common.gui.impl.gui.components.AbstractComponent;
import win.blade.common.gui.impl.gui.setting.implement.MultiSelectSetting;
import win.blade.common.utils.math.MathUtility;
import win.blade.common.utils.math.animation.Animation;
import win.blade.common.utils.math.animation.Easing;
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.builders.states.QuadColorState;
import win.blade.common.utils.render.builders.states.SizeState;
import win.blade.common.utils.render.msdf.FontType;
import win.blade.common.utils.render.msdf.MsdfFont;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static win.blade.common.utils.color.ColorUtility.applyOpacity;

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

        if (setting.getSelected().contains(text)) {
            alphaAnimation.run(0x2D, 0.4, Easing.EASE_OUT_EXPO);
        } else {
            alphaAnimation.run(0, 0.4, Easing.EASE_OUT_EXPO);
        }

        int selectionOpacity = (int) alphaAnimation.get();

        if (selectionOpacity > 0) {
            int backgroundColor = applyOpacity(
                    applyOpacity(0xFF2D2E41, selectionOpacity),
                    alpha
            );

            Builder.rectangle()
                    .size(new SizeState(width, height))
                    .color(new QuadColorState(new Color(backgroundColor, true)))
                    .build()
                    .render(x, y);
        }

        int checkOpacity = applyOpacity(
                applyOpacity(Color.WHITE.getRGB(), Math.min(255, selectionOpacity * 5)),
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
                .render(x + 4, y + 3.3f);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (MathUtility.isHovered(mouseX, mouseY, x, y, width, height) && button == 0) {
            List<String> selected = new ArrayList<>(setting.getSelected());
            if (selected.contains(text)) {
                selected.remove(text);
            } else {
                selected.add(text);
                sortSelectedAccordingToList(selected, setting.getList());
            }
            setting.setSelected(selected);
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void sortSelectedAccordingToList(List<String> selected, List<String> list) {
        selected.sort(Comparator.comparingInt(list::indexOf));
    }
}