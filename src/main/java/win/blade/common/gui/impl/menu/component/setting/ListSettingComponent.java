package win.blade.common.gui.impl.menu.component.setting;

import net.minecraft.client.gui.DrawContext;
import win.blade.common.gui.impl.menu.MenuScreen;
import win.blade.common.gui.impl.menu.component.SettingComponent;
import win.blade.common.gui.impl.menu.settings.impl.ListSetting;
import win.blade.common.utils.other.IMouse;
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.builders.states.QuadColorState;
import win.blade.common.utils.render.builders.states.QuadRadiusState;
import win.blade.common.utils.render.builders.states.SizeState;


import java.awt.Color;

public class ListSettingComponent extends SettingComponent implements IMouse {

    private final ListSetting<?> listSetting;

    public ListSettingComponent(MenuScreen parentScreen, ListSetting<?> setting) {
        super(parentScreen, setting);
        this.listSetting = setting;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta, float alpha) {
        super.render(context, mouseX, mouseY, delta, alpha);

        Builder.text()
                .font(font)
                .text(listSetting.getName())
                .size(7 * scale)
                .color(new Color(255, 255, 255, (int) (255 * alpha)))
                .build()
                .render(x, y + 9 * scale);

        float wOffset = 0;
        float hOffset = 0;
        float yOffset = y + 18 * scale;
        float availableWidth = width - (4 * scale);
        float padding = 3 * scale;
        float spacing = 4 * scale;

        for (Object item : listSetting.values) {
            String itemText = item.toString();
            float textWidth = font.getWidth(itemText, 6 * scale);
            float buttonWidth = textWidth + padding * 2;

            if (wOffset + buttonWidth > availableWidth && wOffset > 0) {
                wOffset = 0;
                hOffset += 12 * scale;
            }

            Builder.rectangle()
                    .size(new SizeState(buttonWidth, 10 * scale))
                    .radius(new QuadRadiusState(3 * scale))
                    .color(new QuadColorState(new Color(45, 45, 50, (int) (150 * alpha))))
                    .build()
                    .render(x + wOffset, yOffset + hOffset);

            Builder.text()
                    .font(font)
                    .text(itemText)
                    .size(6 * scale)
                    .color(item.equals(listSetting.getValue()) ? new Color(87, 90, 198, (int) (255 * alpha)) : new Color(200, 200, 200, (int) (255 * alpha)))
                    .build()
                    .render(x + wOffset + padding, yOffset + hOffset + 2 * scale);

            wOffset += buttonWidth + spacing;
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
        float padding = 3 * scale;
        float spacing = 4 * scale;

        for (Object item : listSetting.values) {
            String itemText = item.toString();
            float textWidth = font.getWidth(itemText, 6 * scale);
            float buttonWidth = textWidth + padding * 2;

            if (wOffset + buttonWidth > availableWidth && wOffset > 0) {
                wOffset = 0;
                hOffset += 12 * scale;
            }

            if (isHover(mouseX, mouseY, x + wOffset, yOffset + hOffset, buttonWidth, 10 * scale)) {
                listSetting.setAsObject(item);
            }

            wOffset += buttonWidth + spacing;
        }
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
    }

    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers) {
    }
}