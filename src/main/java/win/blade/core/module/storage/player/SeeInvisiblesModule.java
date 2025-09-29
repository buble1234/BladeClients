package win.blade.core.module.storage.player;

import win.blade.common.gui.impl.gui.setting.implement.ValueSetting;
import win.blade.core.module.api.Category;
import win.blade.core.module.api.Module;
import win.blade.core.module.api.ModuleInfo;

@ModuleInfo(
        name = "SeeInvisibles",
        category = Category.PLAYER,
        desc = "Раскрывает невидимых существ."
)
public class SeeInvisiblesModule extends Module {

    public final ValueSetting alpha = new ValueSetting("Прозрачность", "Прозрачность невидимых существ.").range(0, 1);

    public SeeInvisiblesModule() {
        addSettings(alpha);
    }
}