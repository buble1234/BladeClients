package win.blade.common.gui.impl.gui.components.implement.window.implement.settings.color.component;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import win.blade.common.gui.impl.gui.components.AbstractComponent;
import win.blade.common.gui.impl.gui.setting.implement.ColorSetting;
import win.blade.common.utils.color.ColorUtility;
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

        float X = x + 9f;
        float Y = y + 101.5F;
        float W = 88;
        float H = 9.5f;

        Builder.rectangle().size(new SizeState(25.5f, H)).color(new QuadColorState(ColorUtility.fromHex("1C1A25"))).radius(new QuadRadiusState(3)).build()
                .render(X, Y);
        Builder.text().font(fontRegular).text("HEX").size(3.5f).color(Color.WHITE).build().render(X + 5, Y + 2.7f);


        Builder.texture()
                .size(new SizeState(4, 4))
                .color(new QuadColorState(Color.WHITE))
                .svgTexture(0f, 0f, 1f, 1f, Identifier.of("blade", "textures/svg/gui/arrow.svg"))
                .radius(new QuadRadiusState(0f))
                .build()
                .render(X + 18f, Y + 2.75f);

        Builder.rectangle().size(new SizeState(40.5f, H)).color(new QuadColorState(ColorUtility.fromHex("1C1A25"))).radius(new QuadRadiusState(3, 3, 0, 0)).build()
                .render(X + 27.5f, Y);
        Builder.text().font(fontRegular).text(Integer.toHexString(setting.getColor())).size(3.5f).color(Color.WHITE).build().render(X + 30.5f, Y + 2.7f);

        Builder.rectangle().size(new SizeState(19.5F, H)).color(new QuadColorState(ColorUtility.fromHex("1C1A25"))).radius(new QuadRadiusState(0, 0, 3, 3)).build()
                .render(X + 67.8F, Y);

        int displayValue = (int) (setting.getAlpha() * 100);
        String percentText = displayValue + "%";
        Builder.text().font(fontRegular).text(percentText).size(3.5f).color(Color.WHITE).build().render(X + 71f, Y + 2.7f);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (MathUtility.isHovered(mouseX, mouseY, x + 122, y + 90.5F, 22, 14)) {
            setting.setAlpha(MathHelper.clamp((float) (setting.getAlpha() - (amount) / 10), 0, 1));
        }
        return super.mouseScrolled(mouseX, mouseY, amount);
    }
}