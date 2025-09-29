package win.blade.core.module.storage.player;

import win.blade.common.gui.impl.gui.setting.implement.TextSetting;
import win.blade.core.module.api.Category;
import win.blade.core.module.api.Module;
import win.blade.core.module.api.ModuleInfo;

/**
 * Автор: NoCap
 * Дата создания: 27.09.2025
 */
@ModuleInfo(name = "NameProtect", category = Category.PLAYER, desc = "Скрывает ваш реальный ник")
public class NameProtectModule extends Module {

    private static TextSetting name = new TextSetting("Protected", "Фейковый ник");

    public static String getName() {
        return name.getName();
    }
}
