package win.blade.core.module.storage.misc;


import win.blade.common.gui.impl.menu.settings.impl.SliderSetting;
import win.blade.core.module.api.Category;
import win.blade.core.module.api.Module;
import win.blade.core.module.api.ModuleInfo;

@ModuleInfo(
        name = "ItemScroller",
        category = Category.MISC,
        desc = "Быстрое перемещение и выбрасывание предметов"
)
public class ItemScrollerModule extends Module {
    public final SliderSetting delay = new SliderSetting(this, "Задержка", 50, 0, 100, 1);

}