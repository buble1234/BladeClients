package win.blade.common.gui.impl.gui.components.implement.settings.multiselect;

import net.minecraft.client.gui.DrawContext;
import win.blade.common.gui.impl.gui.components.implement.settings.AbstractSettingComponent;
import win.blade.common.gui.impl.gui.setting.implement.MultiSelectSetting;
import win.blade.common.utils.color.ColorUtility;
import win.blade.common.utils.math.MathUtility;
import win.blade.common.utils.math.animation.Animation;
import win.blade.common.utils.math.animation.Easing;
import win.blade.common.utils.other.StringUtil;
import win.blade.common.utils.render.Stencil;
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.builders.states.QuadColorState;
import win.blade.common.utils.render.builders.states.QuadRadiusState;
import win.blade.common.utils.render.builders.states.SizeState;
import win.blade.common.utils.render.msdf.FontType;
import win.blade.common.utils.render.msdf.MsdfFont;
import win.blade.common.utils.render.renderers.impl.BuiltRectangle;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class MultiSelectComponent extends AbstractSettingComponent {
    private final List<MultiSelectedButton> multiSelectedButtons = new ArrayList<>();
    private final MultiSelectSetting setting;
    private boolean open;
    private float dropdownListX, dropDownListY, dropDownListWidth, dropDownListHeight;
    private final Animation alphaAnimation = new Animation();
    private final MsdfFont fontRegular = FontType.sf_regular.get();

    public MultiSelectComponent(MultiSelectSetting setting) {
        super(setting);
        this.setting = setting;
        alphaAnimation.set(0);
        for (String s : setting.getList()) {
            multiSelectedButtons.add(new MultiSelectedButton(setting, s));
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        alphaAnimation.update();

        String wrapped = StringUtil.wrap(setting.getDescription(), 45, 6);
        height = (int) (18 + fontRegular.getFontHeight(fontRegular, 6) * (wrapped.split("\n").length - 1));

        List<String> fullSettingsList = setting.getList();
        this.dropdownListX = x + width - 75;
        this.dropDownListY = y + 20;
        this.dropDownListWidth = 66;
        this.dropDownListHeight = fullSettingsList.size() * 12 + 1.5F;

        if (open) {
            alphaAnimation.run(255, 0.3, Easing.EASE_OUT_EXPO);
        } else {
            alphaAnimation.run(0, 0.3, Easing.EASE_OUT_EXPO);
        }

        renderSelected();
        renderSelectList(context, mouseX, mouseY, delta);

        Builder.text()
                .font(fontRegular).text(setting.getName()).size(6)
                .color(new Color(0xFFD4D6E1)).build()
                .render(x + 9, y + 8 + addJust());

        if (shouldRenderDescription)
            Builder.text()
                    .font(fontRegular).text(wrapped).size(5)
                    .color(new Color(0xFF878894)).build()
                    .render(x + 9, y + 15);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            if (MathUtility.isHovered(mouseX, mouseY, x + width - 75, y + 4, 66, 14)) {
                open = !open;
            } else if (open && !isHoveredList(mouseX, mouseY)) {
                open = false;
            }

            if (open) {
                multiSelectedButtons.forEach(selectedButton -> selectedButton.mouseClicked(mouseX, mouseY, button));
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isHover(double mouseX, double mouseY) {
        return open && isHoveredList(mouseX, mouseY);
    }

    private void renderSelected() {

        BuiltRectangle backgroundBox = Builder.rectangle()
                .size(new SizeState(66, 14))
                .color(new QuadColorState(new Color(0xFF161825)))
                .radius(new QuadRadiusState(2))
                .build();

        BuiltRectangle gradientOverlay = Builder.rectangle()
                .size(new SizeState(64, 12))
                .radius(new QuadRadiusState(2))
                .color(new QuadColorState(0x00161825, 0x00161825, 0xFF161825, 0xFF161825))
                .build();



        Stencil.push();

        backgroundBox.render(x + width - 77.5f, y + 4);

        Stencil.read(1);

        backgroundBox.render(x + width - 75, y + 4);

        String selectedName = String.join(", ", setting.getSelected());
        Builder.text()
                .font(fontRegular)
                .text(selectedName)
                .size(6)
                .color(new Color(0xFFD4D6E1))
                .build()
                .render(x + width - 74 + 3, y + 7.5f);

        Stencil.pop();

        gradientOverlay.render(x + width - 74, y + 5);
    }

    private void renderSelectList(DrawContext context, int mouseX, int mouseY, float delta) {
        int opacity = (int) alphaAnimation.get();
        if (opacity > 0) {
            Builder.rectangle()
                    .size(new SizeState(dropDownListWidth, dropDownListHeight))
                    .color(new QuadColorState(new Color(ColorUtility.applyOpacity(0xFF161825, opacity), true)))
                    .radius(new QuadRadiusState(6))
                    .build()
                    .render(dropdownListX, dropDownListY);

            int offset = (int) dropDownListY + 1;
            for (MultiSelectedButton button : multiSelectedButtons) {
                button.x = dropdownListX;
                button.y = offset;
                button.width = dropDownListWidth;
                button.height = 12;
                button.setAlpha(opacity);
                button.render(context, mouseX, mouseY, delta);
                offset += 12;
            }
        }
    }

    private boolean isHoveredList(double mouseX, double mouseY) {
        return MathUtility.isHovered(mouseX, mouseY, dropdownListX, dropDownListY - 16, dropDownListWidth, dropDownListHeight + 16);
    }
}