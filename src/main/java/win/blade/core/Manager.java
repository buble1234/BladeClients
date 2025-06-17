package win.blade.core;

import org.lwjgl.glfw.GLFW;
import win.blade.common.hud.ControlHudElement;
import win.blade.common.hud.impl.RectangleHudElement;
import win.blade.common.hud.impl.TimeHudElement;
import win.blade.core.event.controllers.EventBus;
import win.blade.core.event.controllers.EventHandler;
import win.blade.core.event.controllers.IEventBus;
import win.blade.core.event.impl.input.InputEvents;
import win.blade.core.module.api.Module;
import win.blade.core.module.api.ModuleManager;

import java.lang.invoke.MethodHandles;
import java.util.HashSet;
import java.util.Set;

/**
 * Автор: NoCap
 * Дата создания: 13.05.2025
 * Описание: Глобальный управляющий класс. Хранит и инициализирует основные компоненты клиента
 */
public class Manager {

    public static final IEventBus EVENT_BUS = new EventBus();

    public static final ControlHudElement controlElement = new ControlHudElement();
    public static final TimeHudElement timeElement = new TimeHudElement();
    public static final RectangleHudElement rectangleElement = new RectangleHudElement();
    public static final ModuleManager moduleManager = new ModuleManager();

    private static final Set<Integer> pressedKeys = new HashSet<>();

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
    public void onKey(InputEvents.Keyboard e) {
        int key = e.getKey();
        int action = e.getAction();

        if (action == 2) {
            return;
        }

        if (action == 0) {
            pressedKeys.remove(key);
            return;
        }

        if (action == 1) {
            if (pressedKeys.contains(key)) {
                return;
            }

            pressedKeys.add(key);

            moduleManager.handleKey(key);
        }
    }

    @EventHandler
    public void onMouse(InputEvents.Mouse event) {
        int button = event.getButton();
        int action = event.getAction();

        if (action != 1) return;

        moduleManager.stream()
                .filter(module -> {
                    int bind = module.keybind();
                    return (bind == GLFW.GLFW_MOUSE_BUTTON_4 && button == 3) ||
                            (bind == GLFW.GLFW_MOUSE_BUTTON_5 && button == 4);
                })
                .forEach(Module::toggle);
    }

    public static ModuleManager getModuleManagement() {
        return moduleManager;
    }
}