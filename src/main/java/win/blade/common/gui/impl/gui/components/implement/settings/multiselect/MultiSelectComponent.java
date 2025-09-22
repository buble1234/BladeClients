package win.blade.common.gui.impl.gui.components.implement.settings.multiselect;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import win.blade.common.gui.impl.gui.MenuScreen;
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
    private final List<MultiSelectedButton> selectedButtons = new ArrayList<>();
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
            selectedButtons.add(new MultiSelectedButton(setting, s));
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        alphaAnimation.update();

        String wrapped = StringUtil.wrap(setting.getDescription(), 80, 6);
        height = (int) (18 + fontRegular.getFontHeight(fontRegular, 4) * (wrapped.split("\n").length - 1));

        List<String> fullSettingsList = setting.getList();
        this.dropdownListX = x + width - 51;
        this.dropDownListY = y + 19f;
        this.dropDownListWidth = 43;
        this.dropDownListHeight = fullSettingsList.size() * 8 + 4F;

        if (open) {
            alphaAnimation.run(255, 0.3, Easing.EASE_OUT_EXPO);
        } else {
            alphaAnimation.run(0, 0.3, Easing.EASE_OUT_EXPO);
        }

        renderSelected();

        if (mc.currentScreen instanceof MenuScreen) {
            ((MenuScreen) mc.currentScreen).addPostRenderTask(() -> renderSelectList(context, mouseX, mouseY, delta));
        }

        Builder.text()
                .font(fontRegular).text(setting.getName()).size(5.5f)
                .color(new Color(0xFFD4D6E1)).build()
                .render(x + 9, y + 8 + addJust());

        Builder.text()
                .font(fontRegular).text(wrapped).size(4)
                .color(new Color(0xFF878894)).build()
                .render(x + 9, y + 15);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            if (MathUtility.isHovered(mouseX, mouseY, x + width - 75, y + 4, 66, 14)) {
                open = !open;
                return true;
            }

            if (open) {
                if (isHoveredList(mouseX, mouseY)) {
                    for (MultiSelectedButton selectedButton : selectedButtons) {
                        if (selectedButton.mouseClicked(mouseX, mouseY, button)) {
                            return true;
                        }
                    }
                    return true;
                } else {
                    open = false;
                    return false;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isHover(double mouseX, double mouseY) {
        if (open && isHoveredList(mouseX, mouseY)) {
            return true;
        }
        return MathUtility.isHovered(mouseX, mouseY, x + width - 75, y + 4, 66, 14);
    }

    public void closeDropdown() {
        open = false;
    }

    public boolean isDropdownOpen() {
        return open;
    }

    private void renderSelected() {
        BuiltRectangle backgroundBox = Builder.rectangle()
                .size(new SizeState(43, 9))
                .color(new QuadColorState(ColorUtility.fromHex("1C1A25")))
                .radius(new QuadRadiusState(4))
                .build();

        Stencil.push();
        backgroundBox.render(x + width - 52f, y + 9.5f);
        Stencil.read(1);

        backgroundBox.render(x + width - 50.5f, y + 9.5f);

        List<String> selected = setting.getSelected();
        String selectedName = selected.isEmpty() ? "None" : String.join(", ", selected);

        if (fontRegular.getWidth(selectedName, 4) > 35) {
            selectedName = selected.size() + " selected";
        }

        Builder.text()
                .font(fontRegular)
                .text(selectedName)
                .size(4)
                .color(ColorUtility.fromHex("8C889A"))
                .build()
                .render(x + width - 46.5f, y + 11.75f);

        Stencil.pop();

        Builder.texture()
                .size(new SizeState(4, 4))
                .color(new QuadColorState(Color.WHITE))
                .svgTexture(0f, 0f, 1f, 1f, Identifier.of("blade", "textures/svg/gui/arrow.svg"))
                .radius(new QuadRadiusState(0f))
                .build()
                .render(x+ 8 + width - 24, y+12);
    }

    private void renderSelectList(DrawContext context, int mouseX, int mouseY, float delta) {
        int opacity = (int) alphaAnimation.get();
        if (opacity > 0) {
            Builder.rectangle()
                    .size(new SizeState(dropDownListWidth, dropDownListHeight))
                    .color(new QuadColorState(new Color(ColorUtility.applyOpacity(ColorUtility.fromHex("1C1A25").getRGB(), opacity), true)))
                    .radius(new QuadRadiusState(4))
                    .build()
                    .render(dropdownListX, dropDownListY);

            float offset = dropDownListY - 1.5f;
            for (MultiSelectedButton button : selectedButtons) {
                button.x = dropdownListX;
                button.y = offset;
                button.width = dropDownListWidth;
                button.height = 8f;
                button.setAlpha(opacity);
                button.render(context, mouseX, mouseY, delta);
                offset += 8f;
            }
        }
    }

    private boolean isHoveredList(double mouseX, double mouseY) {
        return MathUtility.isHovered(mouseX, mouseY, dropdownListX, dropDownListY, dropDownListWidth, dropDownListHeight);
    }
}