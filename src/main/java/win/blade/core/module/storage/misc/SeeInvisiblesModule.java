package win.blade.core.module.storage.misc;

import win.blade.common.gui.impl.menu.settings.impl.SliderSetting;
import win.blade.core.module.api.Category;
import win.blade.core.module.api.Module;
import win.blade.core.module.api.ModuleInfo;

@ModuleInfo(
        name = "SeeInvisibles",
        category = Category.RENDER,
        desc = "Позволяет видеть невидимых существ и игроков"
)
public class SeeInvisiblesModule extends Module {

    public final SliderSetting alpha = new SliderSetting(this, "Прозрачность", 0.5F, 0.1F, 1.0F, 0.05F);

}