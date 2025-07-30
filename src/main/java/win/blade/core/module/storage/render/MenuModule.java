package win.blade.core.module.storage.render;

import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.glfw.GLFW;

import win.blade.common.gui.impl.gui.MenuScreen;
import win.blade.common.utils.aim.mode.WebScreen;
import win.blade.core.module.api.Category;
import win.blade.core.module.api.Module;
import win.blade.core.module.api.ModuleInfo;

@ModuleInfo(
        name = "Menu",
        category = Category.RENDER,
        desc = "Интерфейс клиента",
        bind = GLFW.GLFW_KEY_RIGHT_SHIFT
)
public class MenuModule extends Module {

    @Override
    public void onEnable() {
        type = 1;
        RenderSystem.recordRenderCall(() -> {
            mc.setScreen(new MenuScreen());
        });
        this.toggleWithoutNotification(false);
    }
}
