package win.blade.core.module.storage.misc;

import win.blade.common.gui.impl.gui.setting.implement.ValueSetting;
import win.blade.core.module.api.Category;
import win.blade.core.module.api.Module;
import win.blade.core.module.api.ModuleInfo;

@ModuleInfo(
        name = "SeeInvisibles",
        category = Category.RENDER,
        desc = "Позволяет видеть невидимых существ и игроков"
)
public class SeeInvisiblesModule extends Module {

    public final ValueSetting alpha = new ValueSetting("Прозрачность", "").range(0, 1);

    public SeeInvisiblesModule() {
        addSettings(alpha);
    }
}