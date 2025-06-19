package win.blade.core;

import org.lwjgl.glfw.GLFW;
import win.blade.common.gui.impl.menu.MenuScreen;
import win.blade.common.hud.ControlHudElement;
import win.blade.common.hud.impl.RectangleHudElement;
import win.blade.common.hud.impl.TimeHudElement;
import win.blade.core.event.controllers.EventBus;
import win.blade.core.event.controllers.EventHandler;
import win.blade.core.event.controllers.IEventBus;
import win.blade.core.event.impl.minecraft.UpdateEvents;
import win.blade.core.module.api.BindMode;
import win.blade.core.module.api.Module;
import win.blade.core.module.api.ModuleManager;
import win.blade.common.utils.keyboard.Keyboard;
import win.blade.common.utils.minecraft.MinecraftInstance;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;

public class Manager implements MinecraftInstance {

    public static final IEventBus EVENT_BUS = new EventBus();

    public static final ControlHudElement controlElement = new ControlHudElement();
    public static final TimeHudElement timeElement = new TimeHudElement();
    public static final RectangleHudElement rectangleElement = new RectangleHudElement();
    public static final ModuleManager moduleManager = new ModuleManager();
    private static MenuScreen menuScreen;

    private final Map<Module, Boolean> wasKeyPressed = new HashMap<>();

    public void init() {
        EVENT_BUS.registerLambdaFactory("win.blade",
                (lookupInMethod, klass) -> (MethodHandles.Lookup) lookupInMethod.invoke(null, klass, MethodHandles.lookup()));

        EVENT_BUS.subscribe(this);
        EVENT_BUS.subscribe(controlElement);
        EVENT_BUS.subscribe(timeElement);
        EVENT_BUS.subscribe(rectangleElement);
        EVENT_BUS.subscribe(moduleManager);

        moduleManager.initialize();
    }


    @EventHandler
    public void onUpdate(UpdateEvents.Update e) {
        handleKeybinds();
    }

    private void handleKeybinds() {
        if (mc.currentScreen != null) {
            wasKeyPressed.clear();
            return;
        }

        for (Module module : moduleManager.all()) {
            if (module.keybind() == Keyboard.KEY_NONE.getKey()) {
                continue;
            }

            boolean isPress = Keyboard.isKeyDown(module.keybind());
            boolean prevPress = wasKeyPressed.getOrDefault(module, false);

            if (module.getBindMode() == BindMode.TOGGLE) {
                if (isPress && !prevPress) {
                    module.toggle();
                }
            } else if (module.getBindMode() == BindMode.HOLD) {
                if (isPress != module.isEnabled()) {
                    module.setEnabled(isPress);
                }
            }

            wasKeyPressed.put(module, isPress);
        }
    }

    public static MenuScreen getMenuScreen() {
        return menuScreen == null ? menuScreen = new MenuScreen() : menuScreen;
    }

    public static ModuleManager getModuleManagement() {
        return moduleManager;
    }
}