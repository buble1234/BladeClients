package win.blade.common.gui.impl.menu.component.setting;

import net.minecraft.client.gui.DrawContext;
import win.blade.common.gui.impl.menu.MenuScreen;
import win.blade.common.gui.impl.menu.component.SettingComponent;
import win.blade.common.gui.impl.menu.settings.impl.BooleanSetting;
import win.blade.common.utils.color.ColorUtility;
import win.blade.common.utils.math.anmation.Easing;
import win.blade.common.utils.other.IMouse;
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.builders.states.QuadColorState;
import win.blade.common.utils.render.builders.states.QuadRadiusState;
import win.blade.common.utils.render.builders.states.SizeState;

import java.awt.Color;

public class BooleanSettingComponent extends SettingComponent implements IMouse {

    private final BooleanSetting booleanSetting;

    public BooleanSettingComponent(MenuScreen parentScreen, BooleanSetting setting) {
        super(parentScreen, setting);
        this.booleanSetting = setting;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta, float alpha) {
        super.render(context, mouseX, mouseY, delta, alpha);

        booleanSetting.getAnimation().update();
        booleanSetting.getAnimation().run(booleanSetting.getValue() ? 1.0 : 0.0, 0.25, Easing.EASE_OUT_BACK);

        Builder.text()
                .font(font)
                .text(booleanSetting.getName())
                .size(7 * scale)
                .color(new Color(255, 255, 255, (int) (255 * alpha)))
                .build()
                .render(x, y + 9 * scale);

        Builder.rectangle()
                .size(new SizeState(16 * scale, 10 * scale))
                .radius(new QuadRadiusState(4 * scale))
                .color(new QuadColorState(new Color(ColorUtility.overCol(
                        ColorUtility.multDark(((int) (alpha * 255) << 24) | 0x999AA1, 0.25f),
                        ((int) (alpha * 255) << 24) | 0x575AC6,
                        booleanSetting.getAnimation().get()), true)))
                .build()
                .render(x + width - 18 * scale, y + 8 * scale);

        Builder.rectangle()
                .size(new SizeState(6 * scale, 6 * scale))
                .radius(new QuadRadiusState(2 * scale))
                .color(new QuadColorState(new Color(255, 255, 255, (int) (255 * alpha))))
                .build()
                .render(x + width - 18 * scale + 1.5f * scale + (booleanSetting.getAnimation().get() * 6.5f * scale), y + 10 * scale);

        this.height = 30 * scale;
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (isLClick(button) && isHover(mouseX, mouseY, x + width - 18 * scale, y + 8 * scale, 16 * scale, 10 * scale)) {
            booleanSetting.set(!booleanSetting.getValue());
        }
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
    }

    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers) {
    }
}