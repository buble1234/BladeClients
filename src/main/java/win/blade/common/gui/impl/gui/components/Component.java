package win.blade.common.gui.impl.gui.components;

import net.minecraft.client.gui.DrawContext;
import win.blade.common.gui.impl.gui.components.implement.settings.*;
import win.blade.common.gui.impl.gui.components.implement.settings.multiselect.MultiSelectComponent;
import win.blade.common.gui.impl.gui.components.implement.settings.select.SelectComponent;
import win.blade.common.gui.impl.gui.setting.CheckBox;
import win.blade.common.gui.impl.gui.setting.Setting;
import win.blade.common.gui.impl.gui.setting.implement.*;

public interface Component {
    void render(DrawContext context, int mouseX, int mouseY, float delta);

    void tick();

    boolean mouseClicked(double mouseX, double mouseY, int button);

    boolean mouseReleased(double mouseX, double mouseY, int button);

    boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY);

    boolean mouseScrolled(double mouseX, double mouseY, double amount);

    boolean keyPressed(int keyCode, int scanCode, int modifiers);

    boolean charTyped(char chr, int modifiers);

    boolean isHover(double mouseX, double mouseY);

    static AbstractSettingComponent getBySetting(Setting setting){
        if (setting instanceof BooleanSetting booleanSetting) {
            return (new BooleanComponent(booleanSetting));
        }

        if (setting instanceof BindSetting bindSetting) {
            return (new BindComponent(bindSetting));
        }

        if (setting instanceof ColorSetting colorSetting) {
            return (new ColorComponent(colorSetting));
        }

        if (setting instanceof TextSetting textSetting) {
            return (new TextComponent(textSetting));
        }

        if (setting instanceof ValueSetting valueSetting) {
            return (new ValueComponent(valueSetting));
        }

        if (setting instanceof GroupSetting groupSetting) {
            return (new GroupComponent(groupSetting));
        }

        if (setting instanceof ButtonSetting buttonSetting) {
            return (new SButtonComponent(buttonSetting));
        }

        if (setting instanceof SelectSetting selectSetting) {
            return (new SelectComponent(selectSetting));
        }

        if (setting instanceof MultiSelectSetting multiSelectSetting) {
            return (new MultiSelectComponent(multiSelectSetting));
        }

        if(setting instanceof CheckBox box){
            return new CheckBoxComponent(box);
        }


        throw new RuntimeException("Настройка не известного типа:%s".formatted(setting.getName()));
    }
}