package win.blade.common.gui.impl.gui.components.implement.settings;

import net.minecraft.client.gui.DrawContext;
import win.blade.common.gui.impl.gui.components.implement.window.AbstractWindow;
import win.blade.common.gui.impl.gui.components.implement.window.implement.settings.color.ColorWindow;
import win.blade.common.gui.impl.gui.setting.implement.ColorSetting;
import win.blade.common.utils.color.ColorUtility;
import win.blade.common.utils.math.MathUtility;
import win.blade.common.utils.other.StringUtil;
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.builders.states.QuadColorState;
import win.blade.common.utils.render.builders.states.QuadRadiusState;
import win.blade.common.utils.render.builders.states.SizeState;
import win.blade.common.utils.render.msdf.FontType;
import win.blade.common.utils.render.msdf.MsdfFont;

import java.awt.Color;

public class ColorComponent extends AbstractSettingComponent {
    private final ColorSetting setting;
    private final MsdfFont fontRegular = FontType.sf_regular.get();

    public ColorComponent(ColorSetting setting) {
        super(setting);
        this.setting = setting;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        String wrapped = StringUtil.wrap(setting.getDescription(), 100, 6);
        height = (int) (18 + fontRegular.getFontHeight(fontRegular, 6) * (wrapped.split("\n").length - 1));

        Builder.text()
                .font(fontRegular)
                .text(setting.getName())
                .size(6)
                .color(new Color(0xFFD4D6E1))
                .build()
                .render(x + 9, y + 8 + addJust());

        if (shouldRenderDescription)
            Builder.text()
                    .font(fontRegular)
                    .text(wrapped)
                    .size(5)
                    .color(new Color(0xFF878894))
                    .build()
                    .render(x + 9, y + 15);


        Builder.rectangle()
                .size(new SizeState(8, 8))
                .color(new QuadColorState(ColorUtility.applyOpacity(setting.getColor(),60)))
                .radius(new QuadRadiusState(3))
                .build()
                .render(x + width - 17.5f, y + 6.2f);

        Builder.rectangle()
                .size(new SizeState(8, 8))
                .color(new QuadColorState(0xFF000000))
                .radius(new QuadRadiusState(3))
                .build()
                .render(x + width - 17.5f, y + 6.2f);

        Builder.rectangle()
                .size(new SizeState(6, 6))
                .color(new QuadColorState(setting.getColor()))
                .radius(new QuadRadiusState(2))
                .build()
                .render(x + width - 16.5f, y + 7.2f);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (MathUtility.isHovered(mouseX, mouseY, x + width - 17, y + 6.7F, 7, 7) && button == 0) {
            AbstractWindow existingWindow = null;
            for (AbstractWindow window : windowManager.getWindows()) {
                if (window instanceof ColorWindow) {
                    existingWindow = window;
                    break;
                }
            }
            if (existingWindow != null) {
                windowManager.delete(existingWindow);
            } else {
                AbstractWindow colorWindow = new ColorWindow(setting)
                        .position((int) (mouseX + 20), (int) (mouseY - 82))
                        .size(150, 165)
                        .draggable(true);
                windowManager.add(colorWindow);
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}