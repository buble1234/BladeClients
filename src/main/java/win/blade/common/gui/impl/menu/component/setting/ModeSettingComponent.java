package win.blade.common.gui.impl.menu.component.setting;

import net.minecraft.client.gui.DrawContext;
import org.joml.Matrix4f;
import win.blade.common.gui.impl.menu.MenuScreen;
import win.blade.common.gui.impl.menu.component.SettingComponent;
import win.blade.common.gui.impl.menu.settings.impl.ModeSetting;
import win.blade.common.utils.other.IMouse;
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.builders.states.QuadColorState;
import win.blade.common.utils.render.builders.states.QuadRadiusState;
import win.blade.common.utils.render.builders.states.SizeState;

import java.awt.Color;

public class ModeSettingComponent extends SettingComponent implements IMouse {

    public final ModeSetting modeSetting;

    public ModeSettingComponent(MenuScreen parentScreen, ModeSetting setting) {
        super(parentScreen, setting);
        this.modeSetting = setting;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta, float alpha) {
        super.render(context, mouseX, mouseY, delta, alpha);
        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();

        Builder.text()
                .font(font)
                .text(modeSetting.getName())
                .size(7 * scale)
                .color(new Color(255, 255, 255, (int) (255 * alpha)))
                .build()
                .render(matrix, x, y + 9 * scale);

        float wOffset = 0;
        float hOffset = 0;
        float yOffset = y + 18 * scale;
        float availableWidth = width - (4 * scale);

        for (String mode : modeSetting.modes) {
            float buttonWidth = font.getWidth(mode, 6 * scale) + 6 * scale;
            if (wOffset + buttonWidth > availableWidth) {
                wOffset = 0;
                hOffset += 12 * scale;
            }

            Builder.rectangle()
                    .size(new SizeState(buttonWidth, 10 * scale))
                    .radius(new QuadRadiusState(2 * scale))
                    .color(new QuadColorState(new Color(45, 45, 50, (int)(150 * alpha))))
                    .build()
                    .render(matrix, x + wOffset, yOffset + hOffset);

            Builder.text()
                    .font(font)
                    .text(mode)
                    .size(6 * scale)
                    .color(mode.equals(modeSetting.getValue())
                            ? new Color(87, 90, 198, (int)(255 * alpha))
                            : new Color(200, 200, 200, (int)(255 * alpha)))
                    .build()
                    .render(matrix, x + wOffset + 2.5f * scale, yOffset + hOffset + 1 * scale);

            wOffset += buttonWidth + 4 * scale;
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

        for (String mode : modeSetting.modes) {
            float buttonWidth = font.getWidth(mode, 6 * scale) + 6 * scale;
            if (wOffset + buttonWidth > availableWidth) {
                wOffset = 0;
                hOffset += 12 * scale;
            }

            if (isHover(mouseX, mouseY, x + wOffset, yOffset + hOffset, buttonWidth, 10 * scale)) {
                modeSetting.set(mode);
            }

            wOffset += buttonWidth + 4 * scale;
        }
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {}

    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers) {}
}