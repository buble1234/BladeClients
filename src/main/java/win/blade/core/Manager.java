package win.blade.core;

import win.blade.common.gui.impl.menu.MenuScreen;
import win.blade.common.ui.NotificationManager;
import win.blade.common.utils.config.ConfigManager;
import win.blade.core.commands.CommandManager;
import win.blade.core.event.controllers.EventBus;
import win.blade.core.event.controllers.EventHandler;
import win.blade.core.event.controllers.IEventBus;
import win.blade.core.event.impl.render.RenderEvents;
import win.blade.core.event.impl.minecraft.UpdateEvents;
import win.blade.core.module.api.BindMode;
import win.blade.core.module.api.Module;
import win.blade.core.module.api.ModuleManager;
import win.blade.common.utils.keyboard.Keyboard;
import win.blade.common.utils.minecraft.MinecraftInstance;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Manager implements MinecraftInstance {

    public static final IEventBus EVENT_BUS = new EventBus();

    private static boolean panic;
    public static final ModuleManager moduleManager = new ModuleManager();
    public static final NotificationManager notificationManager = new NotificationManager();
    public static CommandManager commandManager;

    private static MenuScreen menuScreen;

    private final Random random = new Random();
    private boolean isFreezing = false;
    private long lastFreezeTime = System.currentTimeMillis();

    private final Map<Module, Boolean> wasKeyPressed = new HashMap<>();

    public void init() {
        setPanic(false);
        EVENT_BUS.registerLambdaFactory("win.blade",
                (lookupInMethod, klass) -> (MethodHandles.Lookup) lookupInMethod.invoke(null, klass, MethodHandles.lookup()));

        ConfigManager.instance = new ConfigManager();
        commandManager = new CommandManager();

        EVENT_BUS.subscribe(this);
        EVENT_BUS.subscribe(moduleManager);
        EVENT_BUS.subscribe(commandManager);

        moduleManager.initialize();
    }



    @EventHandler
    public void onRender(RenderEvents.Screen e) {
        notificationManager.render(e.getDrawContext());
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

            if (module.getBindMode() == BindMode.ПЕРЕКЛЮЧАТЬ) {
                if (isPress && !prevPress) {
                    module.toggle();
                }
            } else if (module.getBindMode() == BindMode.УДЕРЖИВАТЬ) {
                if (isPress != module.isEnabled()) {
                    module.setEnabled(isPress);
                }
            }

            wasKeyPressed.put(module, isPress);
        }
    }

    public static MenuScreen getMenuScreen() {
        return menuScreen == null  ? menuScreen = new MenuScreen() : menuScreen;
    }

    public static boolean isPanic() {
        return panic;
    }

    public void setPanic( boolean toggle) {
        panic = toggle;
    }

    public static ModuleManager getModuleManagement() {
        return moduleManager;
    }
    public static CommandManager getCommandManager() { return commandManager; }
}