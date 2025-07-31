package win.blade.core.module.storage.render;

import win.blade.common.gui.impl.gui.setting.Setting;
import win.blade.common.gui.impl.gui.setting.implement.SelectSetting;
import win.blade.common.gui.impl.gui.setting.implement.ValueSetting;
import win.blade.core.module.api.Category;
import win.blade.core.module.api.Module;
import win.blade.core.module.api.ModuleInfo;

@ModuleInfo(
        name = "AspectRatio",
        category = Category.RENDER,
        desc = "Изменяет соотношение сторон в игре"
)
public class AspectRatioModule extends Module {

    public final SelectSetting multiplier = new SelectSetting("Соотношение", "").value(
            "16:9",
            "16:10",
            "21:9",
            "4:3",
            "Кастомное"
    );

    public final ValueSetting customRatio = new ValueSetting("Значение", "").setValue(1.77f).range(0.5f, 2.0f);

    public AspectRatioModule() {
            customRatio.setVisible(() -> multiplier.isSelected("Кастомное"));

            addSettings(multiplier, customRatio);
    }
}