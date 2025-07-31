package win.blade.core.module.storage.misc;

import win.blade.common.gui.impl.gui.setting.implement.ValueSetting;
import win.blade.core.module.api.Category;
import win.blade.core.module.api.Module;
import win.blade.core.module.api.ModuleInfo;

@ModuleInfo(
        name = "ItemScroller",
        category = Category.MISC,
        desc = "Быстрое перемещение и выбрасывание предметов"
)
public class ItemScrollerModule extends Module {
    public final ValueSetting delay = new ValueSetting("Задержка", "").setValue(50f).range(0f, 100f);


    public ItemScrollerModule() {
        addSettings(delay);
    }
}