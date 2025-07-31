package win.blade.core.module.storage.render;

import win.blade.common.gui.impl.menu.settings.impl.ModeSetting;
import win.blade.common.gui.impl.menu.settings.impl.SliderSetting;
import win.blade.core.module.api.Category;
import win.blade.core.module.api.Module;
import win.blade.core.module.api.ModuleInfo;

@ModuleInfo(
        name = "AspectRatio",
        category = Category.RENDER,
        desc = "Изменяет соотношение сторон в игре"
)
public class AspectRatioModule extends Module {

    public final ModeSetting multiplier = new ModeSetting(this, "Соотношение",
            "16:9",
            "16:10",
            "21:9",
            "4:3",
            "Кастомное"
    );

    public final SliderSetting customRatio = new SliderSetting(this, "Значение", 1.77f, 0.5f, 2.0f, 0.01f)
            .setVisible(() -> multiplier.is("Кастомное"));

}