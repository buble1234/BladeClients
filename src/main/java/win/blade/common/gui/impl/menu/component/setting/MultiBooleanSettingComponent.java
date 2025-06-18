package win.blade.common.gui.impl.menu.component.setting;

import net.minecraft.client.gui.DrawContext;
import org.joml.Matrix4f;
import win.blade.common.gui.impl.menu.MenuScreen;
import win.blade.common.gui.impl.menu.component.SettingComponent;
import win.blade.common.gui.impl.menu.settings.impl.BooleanSetting;
import win.blade.common.gui.impl.menu.settings.impl.MultiBooleanSetting;
import win.blade.common.utils.color.ColorUtility;
import win.blade.common.utils.math.animation.Easing;
import win.blade.common.utils.other.IMouse;
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.builders.states.QuadColorState;
import win.blade.common.utils.render.builders.states.QuadRadiusState;
import win.blade.common.utils.render.builders.states.SizeState;

import java.awt.Color;

public class MultiBooleanSettingComponent extends SettingComponent implements IMouse {

    private final MultiBooleanSetting multiBooleanSetting;

    public MultiBooleanSettingComponent(MenuScreen parentScreen, MultiBooleanSetting setting) {
        super(parentScreen, setting);
        this.multiBooleanSetting = setting;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta, float alpha) {
        super.render(context, mouseX, mouseY, delta, alpha);
        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();

        Builder.text()
                .font(font)
                .text(multiBooleanSetting.getName())
                .size(7 * scale)
                .color(new Color(255, 255, 255, (int) (255 * alpha)))
                .build()
                .render(matrix, x + 2 * scale, y + 9 * scale);

        float wOffset = 0;
        float hOffset = 0;
        float yOffset = y + 18 * scale;
        float availableWidth = width - (4 * scale);
        float padding = 3 * scale;
        float spacing = 4 * scale;

        for (BooleanSetting setting : multiBooleanSetting.getValues()) {
            if (!setting.getVisible().get()) continue;

            setting.getAnimation().update();
            setting.getAnimation().run(setting.getValue() ? 1.0 : 0.0, 0.2, Easing.LINEAR);

            float buttonWidth = font.getWidth(setting.getName(), 6 * scale) + padding * 2;

            if (wOffset + buttonWidth > availableWidth && wOffset > 0) {
                wOffset = 0;
                hOffset += 12 * scale;
            }

            Builder.rectangle()
                    .size(new SizeState(buttonWidth, 10 * scale))
                    .radius(new QuadRadiusState(2 * scale))
                    .color(new QuadColorState(new Color(45, 45, 50, (int)(150 * alpha))))
                    .build()
                    .render(matrix, x + 2 * scale + wOffset, yOffset + hOffset);

            Builder.text()
                    .font(font)
                    .text(setting.getName())
                    .size(6 * scale)
                    .color(new Color(ColorUtility.overCol(
                            new Color(200, 200, 200, (int)(255 * alpha)).getRGB(),
                            new Color(87, 90, 198, (int)(255 * alpha)).getRGB(),
                            setting.getAnimation().get()), true))
                    .build()
                    .render(matrix, x + 2 * scale + wOffset + padding, yOffset + hOffset + 2 * scale);

            wOffset += buttonWidth + spacing;
        }

        this.height = (yOffset - y) + hOffset + 12 * scale;
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (!isLClick(button)) return;

        float wOffset = 0;
        float hOffset = 0;
        float yOffset = y + 18 * scale;
        float availableWidth = width - (4 * scale);
        float padding = 3 * scale;
        float spacing = 4 * scale;

        for (BooleanSetting setting : multiBooleanSetting.getValues()) {
            if (!setting.getVisible().get()) continue;

            float buttonWidth = font.getWidth(setting.getName(), 6 * scale) + padding * 2;

            if (wOffset + buttonWidth > availableWidth && wOffset > 0) {
                wOffset = 0;
                hOffset += 12 * scale;
            }

            if (isHover(mouseX, mouseY, x + 2 * scale + wOffset, yOffset + hOffset, buttonWidth, 10 * scale)) {
                setting.set(!setting.getValue());
            }

            wOffset += buttonWidth + spacing;
        }
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {}

    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers) {}
}