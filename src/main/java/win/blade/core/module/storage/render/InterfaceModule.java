package win.blade.core.module.storage.render;

import win.blade.common.gui.impl.gui.setting.implement.BooleanSetting;
import win.blade.common.gui.impl.gui.setting.implement.GroupSetting;
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
 * Рефакторинг под новый API: 14.07.2024
 */

@ModuleInfo(
        name = "Interface",
        category = Category.RENDER,
        desc = "Интерфейс клиента"
)
public class InterfaceModule extends Module {

    private final Map<String, Module> interfaceElements = new HashMap<>();
    private GroupSetting elementsSettings;

    public InterfaceModule() {
        setupElements();
        addSettings(elementsSettings);
    }

    private void setupElements() {
        interfaceElements.put("Ватермарка", new Watermark());
        interfaceElements.put("Клавиши", new Hotkey());
        interfaceElements.put("Музыка", new MusicHud());
        interfaceElements.put("Зелья", new Potions());
        interfaceElements.put("ТаргетХуд", new TargetHud());
        interfaceElements.put("Инфо", new InfoHud());

        BooleanSetting[] settings = interfaceElements.keySet().stream()
                .map(name -> new BooleanSetting(name, "").setValue(true))
                .toArray(BooleanSetting[]::new);

        elementsSettings = new GroupSetting("Элементы интерфейса", "").setToggleable();
        elementsSettings.settings(settings);
    }

    private BooleanSetting getBooleanSetting(GroupSetting group, String name) {
        return (BooleanSetting) group.getSubSetting(name);
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
            boolean shouldBeEnabled = getBooleanSetting(elementsSettings, name).getValue();
            if (module.isEnabled() != shouldBeEnabled) {
                module.toggleWithoutNotification(shouldBeEnabled);
            }
        });
    }

    private void disableAllElements() {
        interfaceElements.values().forEach(module -> module.toggleWithoutNotification(false));
    }
}