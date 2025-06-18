package win.blade.common.gui.impl.menu.panel;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import win.blade.common.gui.impl.menu.MenuScreen;
import win.blade.common.gui.impl.menu.window.WindowComponent;
import win.blade.common.utils.math.anmation.AnimationHelp;
import win.blade.common.utils.math.anmation.Easing;
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.msdf.FontType;
import win.blade.core.module.api.Category;

import java.awt.Color;

public class CategoryPanel extends WindowComponent implements AnimationHelp {
    private Category selectCateg = Category.COMBAT;
    private Category prevCateg = null;
    private long selectTime = 0;

    public CategoryPanel(MenuScreen menuScreen) {
        super(menuScreen);
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta, float alpha, float scale, float panelX, float panelY) {
        MatrixStack matrices = context.getMatrices();
        float categoryY = panelY + 60 * scale;

        long elapsTimer = timer.elapsedTime();

        int categoryIndex = 0;
        for (Category category : Category.values()) {
            float progress = MathHelper.clamp((elapsTimer - (categoryIndex * stagger())) / duration(), 0.0f, 1.0f);

            if (!menuScreen.isClosing && progress == 0) {
                categoryY += 25 * scale;
                categoryIndex++;
                continue;
            }

            float easedProgress = (float) Easing.EASE_OUT_QUINT.ease(progress);

            float animXOffset;
            float alphaMultiplier;

            if (menuScreen.isClosing) {
                animXOffset = -40.0f * easedProgress;
                alphaMultiplier = 1.0f - easedProgress;
            } else {
                animXOffset = -40.0f * (1.0f - easedProgress);
                alphaMultiplier = easedProgress;
            }

            float selectOffset = 0;
            float selectProgress = MathHelper.clamp((timer.elapsedTime() - selectTime) / selectDuration(), 0.0f, 1.0f);
            float easedSelect = (float) Easing.EASE_OUT_CUBIC.ease(selectProgress);

            if (category == selectCateg) {
                selectOffset = 4 * scale * easedSelect;
            } else if (category == prevCateg && selectProgress < 1.0f) {
                selectOffset = 4 * scale * (1.0f - easedSelect);
            }

            int baseColor = category == selectCateg ? 0xFFFFFF : 0xB4B4B4;
            int textColor = (MathHelper.clamp((int) (255 * alpha * alphaMultiplier), 0, 255) << 24) | baseColor;

            Builder.text()
                    .font(FontType.icon.get())
                    .text(category.getIcon())
                    .color(new Color(textColor, true))
                    .size(8f)
                    .build()
                    .render(panelX + animXOffset + selectOffset + 20 * scale, categoryY + 8 * scale);

            Builder.text()
                    .font(FontType.sf_regular.get())
                    .text(category.getName())
                    .color(new Color(textColor, true))
                    .size(8f)
                    .build()
                    .render(panelX + animXOffset + selectOffset + 40 * scale, categoryY + 8 * scale);

            categoryY += 25 * scale;
            categoryIndex++;
        }
    }

    public Category getSelectedCategory() {
        return selectCateg;
    }

    public void mouseClicked(double mouseX, double mouseY, int button, float scale) {
        if (menuScreen.isClosing) return;

        float panelX = (menuScreen.width / 2f) - ((450 * scale) / 2f);
        float panelY = (menuScreen.height / 2f) - ((270 * scale) / 2f);
        float categoryY = panelY + 60 * scale;
        long elapsedTime = timer.elapsedTime();
        int categoryIndex = 0;

        for (Category category : Category.values()) {
            if (MathHelper.clamp((elapsedTime - (categoryIndex * stagger())) / duration(), 0.0f, 1.0f) >= 1.0f) {
                if (mouseX >= panelX && mouseX <= panelX + 100 * scale && mouseY >= categoryY && mouseY <= categoryY + 25 * scale) {
                    if (selectCateg != category) {
                        prevCateg = selectCateg;
                        selectCateg = category;
                        selectTime = timer.elapsedTime();
                    }
                    break;
                }
            }
            categoryY += 25 * scale;
            categoryIndex++;
        }
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta, float alpha) {
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
    }

    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers) {
    }
}
