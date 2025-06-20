package win.blade.core.module.storage.misc;

import net.fabricmc.loader.impl.FabricLoaderImpl;
import net.fabricmc.loader.impl.ModContainerImpl;
import win.blade.Blade;
import win.blade.common.utils.keyboard.Keyboard;
import win.blade.common.utils.resource.InformationUtility;
import win.blade.core.Manager;
import win.blade.core.module.api.Category;
import win.blade.core.module.api.Module;
import win.blade.core.module.api.ModuleInfo;

@ModuleInfo(name = "Panic", category = Category.MISC)
public class Panic extends Module {

    @Override
    protected void onEnable() {
        Blade.manager.setPanic(true);

        if (mc.currentScreen != null) {
            mc.setScreen(null);
        }

        for (Module module : Manager.moduleManager.values()) {
            if (module != this) {
                module.setKeybind(Keyboard.KEY_NONE.getKey());
                module.setEnabled(false);
            }
        }

        ModContainerImpl bladeMod = (ModContainerImpl) FabricLoaderImpl.INSTANCE.getAllMods()
                .stream()
                .filter(modContainer -> modContainer.getMetadata().getId().equals(InformationUtility.CLIENT_ID))
                .findFirst()
                .orElse(null);

        if (bladeMod != null) {
            FabricLoaderImpl.INSTANCE.getModsInternal().remove(bladeMod);
        }

    }

}