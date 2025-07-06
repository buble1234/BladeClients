package win.blade.core.module.storage.render;

import win.blade.common.gui.impl.menu.MenuScreen;
import win.blade.core.module.api.Category;
import win.blade.core.module.api.Module;
import win.blade.core.module.api.ModuleInfo;

/**
 * Автор: NoCap
 * Дата создания: 06.07.2025
 */
@ModuleInfo(name = "ShaderESP", category = Category.RENDER)
public class ShaderESP extends Module {
    public static boolean show = false;

    @Override
    public void onEnable() {
        show = true;
    }

    @Override
    public void onDisable() {
        show = false;
    }
}