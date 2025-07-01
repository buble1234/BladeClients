package win.blade.common.gui.impl.gui.components.implement.window.implement.settings.color.component;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;
import win.blade.common.gui.impl.gui.components.AbstractComponent;
import win.blade.common.gui.impl.gui.setting.implement.ColorSetting;
import win.blade.common.utils.math.MathUtility;
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.builders.states.QuadColorState;
import win.blade.common.utils.render.builders.states.QuadRadiusState;
import win.blade.common.utils.render.builders.states.SizeState;
import win.blade.common.utils.render.msdf.FontType;
import win.blade.common.utils.render.msdf.MsdfFont;

import java.awt.Color;

public class ColorEditorComponent extends AbstractComponent {
    private final ColorSetting setting;
    private final MsdfFont fontRegular = FontType.sf_regular.get();

    public ColorEditorComponent(ColorSetting setting) {
        this.setting = setting;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        Builder.rectangle().size(new SizeState(31, 14)).color(new QuadColorState(new Color(0x80222336, true))).radius(new QuadRadiusState(3)).build().render(x + 6, y + 90.5f);
        Builder.text().font(fontRegular).text("HEX").size(6.5f).color(Color.WHITE).build().render(x + 10, y + 94);

        Builder.rectangle().size(new SizeState(80, 14)).color(new QuadColorState(new Color(0x80222336, true))).radius(new QuadRadiusState(3)).build().render(x + 40, y + 90.5f);
        Builder.text().font(fontRegular).text("#" + Integer.toHexString(setting.getColor())).size(6.5f).color(Color.WHITE).build().render(x + 45, y + 94);

        Builder.rectangle().size(new SizeState(22, 14)).color(new QuadColorState(new Color(0x80222336, true))).radius(new QuadRadiusState(3)).build().render(x + 122, y + 90.5f);
        int displayValue = (int) (setting.getAlpha() * 100);
        String percentText = displayValue + "%";
        float textWidth = fontRegular.getWidth(percentText, 6.5f);
        Builder.text().font(fontRegular).text(percentText).size(6.5f).color(Color.WHITE).build().render(x + 133 - textWidth / 2, y + 94);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (MathUtility.isHovered(mouseX, mouseY, x + 122, y + 90.5F, 22, 14)) {
            setting.setAlpha(MathHelper.clamp((float) (setting.getAlpha() - (amount) / 10), 0, 1));
        }
        return super.mouseScrolled(mouseX, mouseY, amount);
    }
}