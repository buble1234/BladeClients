package win.blade.core;

import com.mojang.blaze3d.systems.RenderSystem;
import win.blade.common.gui.impl.gui.MenuScreen;
import win.blade.common.gui.impl.screen.ExitScreen;
import win.blade.common.ui.NotificationManager;
import win.blade.common.utils.browser.BrowserManager;
import win.blade.common.utils.friends.FriendManager;
import win.blade.common.utils.keyboard.KeyOptions;
import win.blade.core.commands.CommandManager;
import win.blade.core.event.controllers.EventBus;
import win.blade.core.event.controllers.EventHandler;
import win.blade.core.event.controllers.IEventBus;
import win.blade.core.event.impl.input.InputEvents;
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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Manager implements MinecraftInstance {

    public static final IEventBus EVENT_BUS = new EventBus();
    public static ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

    private static boolean panic;
    public static final ModuleManager moduleManager = new ModuleManager();
    public static final NotificationManager notificationManager = new NotificationManager();
    public static CommandManager commandManager;

    public static ExitScreen screen = new ExitScreen();
    public static MenuScreen menuScreen;


    public static final Map<String, Boolean> wasKeyPressed = new HashMap<>();

    public void init() {
        setPanic(false);
        EVENT_BUS.registerLambdaFactory("win.blade",
                (lookupInMethod, klass) -> (MethodHandles.Lookup) lookupInMethod.invoke(null, klass, MethodHandles.lookup()));

        BrowserManager.INSTANCE.initializeBrowser();
        KeyOptions.initialize();
        //ConfigManager.instance = new ConfigManager();
        FriendManager.instance = new FriendManager();
        commandManager = new CommandManager();

        EVENT_BUS.subscribe(this);
        EVENT_BUS.subscribe(new KeyOptions());
        EVENT_BUS.subscribe(moduleManager);
        EVENT_BUS.subscribe(commandManager);

        moduleManager.initialize();
    }



    @EventHandler
    public void onRender(RenderEvents.Screen.POST e) {
        notificationManager.render(e.getDrawContext());
    }


    @EventHandler
    public void onUpdate(UpdateEvents.Update e) {
        BrowserManager.INSTANCE.doMessageLoop();
        handleKeybinds();
    }

    private void handleKeybinds() {
//        if (mc.currentScreen != null) {
//            wasKeyPressed.clear();
//            return;
//        }

        for (Module module : moduleManager.all()) {
            module.handleSettingsBind();

            if (module.keybind() == Keyboard.KEY_NONE.getKey()) {
                continue;
            }

            boolean isPress = Keyboard.isKeyDown(module.keybind());
            boolean prevPress = wasKeyPressed.getOrDefault(module.name(), false);

            if (module.type == 1) {
                if (isPress && !prevPress) {
                    module.toggle();
                }
            } else if (module.type == 0) {
                if (isPress != module.isEnabled()) {
                    module.scheduledToggle(isPress);
                }
            }

            wasKeyPressed.put(module.name(), isPress);
        }
    }

    public static MenuScreen getMenuScreen() {
        return menuScreen == null  ? menuScreen = new MenuScreen() : menuScreen;
    }

    public static ExitScreen getExitScreen(){
        return screen;
    }

    public static boolean canQuit(){
        if(screen.hasShown){
            return true;
        } else {
            mc.setScreen(screen);
            executorService.schedule(() -> RenderSystem.recordRenderCall(() -> screen.close()), 1000, TimeUnit.MILLISECONDS);

            return false;
        }
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