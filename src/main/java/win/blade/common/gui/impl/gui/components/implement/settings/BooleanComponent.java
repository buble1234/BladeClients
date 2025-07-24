package win.blade.common.gui.impl.gui.components.implement.settings;

import net.minecraft.client.gui.DrawContext;
import win.blade.common.gui.impl.gui.components.implement.other.CheckComponent;
import win.blade.common.gui.impl.gui.components.implement.other.SettingComponent;
import win.blade.common.gui.impl.gui.components.implement.window.AbstractWindow;
import win.blade.common.gui.impl.gui.components.implement.window.implement.settings.BindCheckboxWindow;
import win.blade.common.gui.impl.gui.setting.implement.BooleanSetting;
import win.blade.common.utils.other.StringUtil;
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.msdf.FontType;
import win.blade.common.utils.render.msdf.MsdfFont;

import java.awt.Color;

public class BooleanComponent extends AbstractSettingComponent {
    private final CheckComponent checkComponent = new CheckComponent();
    private final SettingComponent settingComponent = new SettingComponent();

    private final BooleanSetting setting;
    
    private final MsdfFont fontRegular = FontType.sf_regular.get();

    public BooleanComponent(BooleanSetting setting) {
        super(setting);
        this.setting = setting;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        String wrapped = StringUtil.wrap(setting.getDescription(), 100, 6);
        height = (int) (18 + fontRegular.getFontHeight(fontRegular,6) * (wrapped.split("\n").length - 1));

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

        ((CheckComponent) checkComponent.position(x + width - 16, y + 7.5F))
                .setRunnable(() -> setting.setValue(!setting.getValue()))
                .setState(setting.getValue())
                .render(context, mouseX, mouseY, delta);

        ((SettingComponent) settingComponent.position(x + width - 28, y + 8))
                .setRunnable(() -> spawnWindow(mouseX, mouseY))
                .render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        checkComponent.mouseClicked(mouseX, mouseY, button);
        settingComponent.mouseClicked(mouseX, mouseY, button);
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void spawnWindow(int mouseX, int mouseY) {
        AbstractWindow existingWindow = null;

        for (AbstractWindow window : windowManager.getWindows()) {
            if (window instanceof BindCheckboxWindow) {
                existingWindow = window;
                break;
            }
        }

        if (existingWindow != null) {
            windowManager.delete(existingWindow);
        } else {
            AbstractWindow bindCheckboxWindow = new BindCheckboxWindow(setting)
                    .position(mouseX + 5, mouseY + 5)
                    .size(105, 55)
                    .draggable(false);

            windowManager.add(bindCheckboxWindow);
        }
    }
}