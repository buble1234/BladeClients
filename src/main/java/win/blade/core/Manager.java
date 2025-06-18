package win.blade.core;

import org.lwjgl.glfw.GLFW;
import win.blade.common.gui.impl.menu.MenuScreen;
import win.blade.common.hud.ControlHudElement;
import win.blade.common.hud.impl.RectangleHudElement;
import win.blade.common.hud.impl.TimeHudElement;
import win.blade.common.utils.minecraft.ChatUtility;
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.builders.states.QuadColorState;
import win.blade.common.utils.render.builders.states.QuadRadiusState;
import win.blade.common.utils.render.builders.states.SizeState;
import win.blade.common.utils.render.renderers.impl.BuiltBlur;
import win.blade.core.event.controllers.EventBus;
import win.blade.core.event.controllers.EventHandler;
import win.blade.core.event.controllers.IEventBus;
import win.blade.core.event.impl.input.InputEvents;
import win.blade.core.event.impl.render.RenderEvents;
import win.blade.core.module.api.Module;
import win.blade.core.module.api.ModuleManager;

import java.awt.*;
import java.lang.invoke.MethodHandles;
import java.util.HashSet;
import java.util.Set;

import static win.blade.common.utils.render.renderers.IRenderer.DEFAULT_MATRIX;

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
    private static MenuScreen menuScreen;

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
    public void onRender(RenderEvents.Screen e) {
        BuiltBlur rectangle = Builder.blur()
                .size(new SizeState(80, 40))
                .color(new QuadColorState(new Color(50, 50, 50)))
                .radius(new QuadRadiusState(5f, 5f, 5f, 5f))
                .blurRadius(6)
                .build();
        rectangle.render(DEFAULT_MATRIX, 100f, 100f);
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

    public static MenuScreen getMenuScreen() {
        return menuScreen == null ? menuScreen = new MenuScreen() : menuScreen;
    }

    public static ModuleManager getModuleManagement() {
        return moduleManager;
    }
}