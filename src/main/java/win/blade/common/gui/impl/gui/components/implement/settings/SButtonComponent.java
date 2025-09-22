package win.blade.common.gui.impl.gui.components.implement.settings;

import net.minecraft.client.gui.DrawContext;
import win.blade.common.gui.impl.gui.components.implement.other.ButtonComponent;
import win.blade.common.gui.impl.gui.setting.implement.ButtonSetting;
import win.blade.common.utils.other.StringUtil;
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.msdf.FontType;
import win.blade.common.utils.render.msdf.MsdfFont;

import java.awt.Color;

public class SButtonComponent extends AbstractSettingComponent {
    private final ButtonComponent buttonComponent = new ButtonComponent();
    private final ButtonSetting setting;
    
    private final MsdfFont fontRegular = FontType.sf_regular.get();

    public SButtonComponent(ButtonSetting setting) {
        super(setting);
        this.setting = setting;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        var wrapped = StringUtil.wrap(setting.getDescription(), 80, 6);

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

        ((ButtonComponent) buttonComponent.setText("Click on me")
                .setRunnable(setting.getRunnable())
                .position(x + width - 9 - buttonComponent.width, y + 5))
                .render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (buttonComponent.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}