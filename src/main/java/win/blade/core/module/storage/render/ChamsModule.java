package win.blade.core.module.storage.render;

import net.minecraft.entity.Entity;
import win.blade.core.module.api.Category;
import win.blade.core.module.api.Module;
import win.blade.core.module.api.ModuleInfo;

@ModuleInfo(name = "Chams", category = Category.RENDER)
public class ChamsModule extends Module {

    public ChamsModule() {}

    public boolean shouldRender(Entity entity) {
        return entity != null;
    }
}