package win.blade.common.gui.impl.gui.components.implement.settings;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.ColorHelper;
import org.joml.Matrix4f;
import win.blade.common.gui.impl.gui.setting.implement.BindSetting;
import win.blade.common.utils.math.MathUtility;
import win.blade.common.utils.other.StringUtil;
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.builders.states.QuadColorState;
import win.blade.common.utils.render.builders.states.QuadRadiusState;
import win.blade.common.utils.render.builders.states.SizeState;
import win.blade.common.utils.render.msdf.FontType;
import win.blade.common.utils.render.msdf.MsdfFont;

import java.awt.Color;

public class BindComponent extends AbstractSettingComponent {
    private final BindSetting setting;
    private boolean binding;
    
    private final MsdfFont fontRegular = FontType.sf_regular.get();

    public BindComponent(BindSetting setting) {
        super(setting);
        this.setting = setting;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        Matrix4f positionMatrix = context
                .getMatrices()
                .peek()
                .getPositionMatrix();

        var bindName = StringUtil.getBindName(setting.getKey());
        var stringWidth = fontRegular.getWidth(bindName, 6) - 2;
        var wrapped = StringUtil.wrap(setting.getDescription(), (int) (width - stringWidth - 28), 6);

        height = (int) (18 + fontRegular.getFontHeight(fontRegular,6) * (wrapped.split("\n").length - 1));

        Builder.rectangle()
                .size(new SizeState(stringWidth + 10, 12))
                .color(new QuadColorState(new Color(0xFF161825)))
                .radius(new QuadRadiusState(6))
                .build()
                .render(x + width - stringWidth - 17, y + 5);

        int bindingColor = binding
                ? 0xFF8187ff
                : ColorHelper.getArgb(255, 135, 136, 148);

        Builder.text()
                .font(fontRegular)
                .text(bindName)
                .size(6)
                .color(new Color(bindingColor))
                .build()
                .render( x + width - 12 - stringWidth - 1, y + 9.5f);

        Builder.text()
                .font(fontRegular)
                .text(setting.getName())
                .size(7)
                .color(new Color(0xFFD4D6E1))
                .build()
                .render( x + 9, y + 6);

        Builder.text()
                .font(fontRegular)
                .text(wrapped)
                .size(6)
                .color(new Color(0xFF878894))
                .build()
                .render( x + 9, y + 15);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            if (MathUtility.isHovered(mouseX, mouseY, x, y, width, height)) {
                binding = !binding;
            } else {
                binding = false;
            }
        }

        if (binding && button > 1) {
            setting.setKey(button);
            binding = false;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (binding) {
            setting.setKey(keyCode);
            binding = false;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}