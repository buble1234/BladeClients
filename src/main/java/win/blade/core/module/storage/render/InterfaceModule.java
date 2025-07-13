package win.blade.core.module.storage.render;

import win.blade.common.gui.impl.menu.settings.impl.BooleanSetting;
import win.blade.common.gui.impl.menu.settings.impl.MultiBooleanSetting;
import win.blade.common.ui.element.hud.*;
import win.blade.core.event.controllers.EventHandler;
import win.blade.core.event.impl.minecraft.UpdateEvents;
import win.blade.core.module.api.Category;
import win.blade.core.module.api.Module;
import win.blade.core.module.api.ModuleInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * Автор: NoCap
 * Дата создания: 19.06.2025
 */

@ModuleInfo(
        name = "Interface",
        category = Category.RENDER,
        desc = "Интерфейс клиента"
)
public class InterfaceModule extends Module {

    private final Map<String, Module> interfaceElements = new HashMap<>();
    private MultiBooleanSetting elementsSettings;

    public InterfaceModule() {
        setupElements();
    }

    private void setupElements() {
        interfaceElements.put("Ватермарка", new Watermark());
        interfaceElements.put("Клавиши", new Hotkey());
        interfaceElements.put("Зелья", new Potions());
        interfaceElements.put("ТаргетХуд", new TargetHud());
        interfaceElements.put("Инфо", new InfoHud());

        BooleanSetting[] settings = interfaceElements.keySet().stream()
                .map(name -> BooleanSetting.of(name, true))
                .toArray(BooleanSetting[]::new);

        elementsSettings = new MultiBooleanSetting(this, "Элементы интерфейса", settings);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        updateElements();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        disableAllElements();
    }

    @EventHandler
    public void onTick(UpdateEvents.Update e) {
        if (isEnabled()) {
            updateElements();
        }
    }

    private void updateElements() {
        interfaceElements.forEach((name, module) -> {
            boolean shouldBeEnabled = elementsSettings.getValue(name);
            if (module.isEnabled() != shouldBeEnabled) {
                module.setEnabled(shouldBeEnabled);
            }
        });
    }

    private void disableAllElements() {
        interfaceElements.values().forEach(module -> module.setEnabled(false));
    }
}