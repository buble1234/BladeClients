package win.blade.common.gui.impl.gui.components.implement.settings;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;
import win.blade.common.gui.impl.gui.setting.implement.ValueSetting;
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
import java.math.BigDecimal;
import java.math.RoundingMode;


public class ValueComponent extends AbstractSettingComponent {
    public static final int SLIDER_WIDTH = 45;

    private final ValueSetting setting;

    private boolean dragging;
    private double animation;

    private final MsdfFont fontRegular = FontType.sf_regular.get();

    public ValueComponent(ValueSetting setting) {
        super(setting);
        this.setting = setting;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        Matrix4f positionMatrix = context
                .getMatrices()
                .peek()
                .getPositionMatrix();

        String wrapped = StringUtil.wrap(setting.getDescription(), 75, 6);
        height = (int) (20 + fontRegular.getFontHeight(fontRegular, 6) * (wrapped.split("\n").length - 1));

        String value = String.valueOf(setting.getValue())
                .replace(".0", "");

            Builder.text()
                    .font(fontRegular)
                    .text(value)
                    .size(4)
                    .color(ColorUtility.fromHex("663CFF"))
                    .build()
                    .render(x + width - 9 - fontRegular.getWidth(value, 6), y + 8.5f);

        changeValue(
                getDifference(mouseX)
        );

        Builder.text()
                .font(FontType.sf_regular.get())
                .text(setting.getName())
                .size(5.5f)
                .color(new Color(0xFFD4D6E1))
                .build()
                .render(x + 9, y + 8 + addJust());

        if(shouldRenderDescription)
            Builder.text()
                .font(fontRegular)
                .text(wrapped)
                .size(4)
                .color(new Color(0xFF878894))
                .build()
                .render(x + 9, y + 15);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        dragging = MathUtility.isHovered(mouseX, mouseY, x + width - SLIDER_WIDTH - 9, y + 13, SLIDER_WIDTH, 4) && button == 0;

        if(dragging) return true;

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        dragging = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private float getDifference(int mouseX) {
        float percentValue = SLIDER_WIDTH * (setting.getValue() - setting.getMin()) / (setting.getMax() - setting.getMin());
        float difference = MathHelper.clamp(mouseX - (x + width - SLIDER_WIDTH - 9), 0, SLIDER_WIDTH);

        animation = MathHelper.lerp(0.9F, animation, percentValue);

        attachmentX = x + width - SLIDER_WIDTH - 15;
        attachmentY = y + 15;

        Builder.texture()
                .size(new SizeState(6, 6))
                .color(new QuadColorState(-1))
                .svgTexture(0f, 0f, 1f, 1f, Identifier.of("blade", "textures/svg/gui/setting.svg"))
                .radius(new QuadRadiusState(0f))
                .build()
                .render(x + width - SLIDER_WIDTH - 15.5, y + 11.5f);

        Builder.rectangle()
                .size(new SizeState(SLIDER_WIDTH, 3))
                .color(new QuadColorState(new Color(0x4D2E2E41, true)))
                .radius(new QuadRadiusState(0.5f))
                .build()
                .render(x + width - SLIDER_WIDTH - 9, y + 15);

        Builder.rectangle()
                .size(new SizeState((float) animation, 3))
                .color(new QuadColorState(ColorUtility.fromHex("663CFF")))
                .radius(new QuadRadiusState(0.5f))
                .build()
                .render(x + width - SLIDER_WIDTH - 9, y + 15);

        float handleX = x + width - SLIDER_WIDTH - 9 + (float) animation;

        Builder.border()
                .size(new SizeState(6, 6))
                .color(new QuadColorState(ColorUtility.fromHex("1C1A25")))
                .outlineColor(new QuadColorState(ColorUtility.fromHex("663CFF")))
                .radius(new QuadRadiusState(2f))
                .thickness(0.5f)
                .build()
                .render(handleX - 2, y + 13.5f);

        return difference;
    }

    private void changeValue(float difference) {
        BigDecimal bd = BigDecimal.valueOf((difference / SLIDER_WIDTH) * (setting.getMax() - setting.getMin()) + setting.getMin())
                .setScale(1, RoundingMode.HALF_UP);

        if (dragging) {
            setting.setValue(difference == 0 ? setting.getMin() : bd.floatValue());
        }
    }
}