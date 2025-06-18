package win.blade.core.module.api;

import win.blade.common.gui.impl.menu.settings.Setting;
import win.blade.common.utils.minecraft.ChatUtility;
import win.blade.common.utils.minecraft.MinecraftInstance;
import win.blade.core.Manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class Module implements MinecraftInstance {

    public record ModuleData(String name, Category category, String description, String descKey, int defaultKeybind) {}

    private final ModuleData data;
    private boolean enabled = false;
    private int keybind;
    private BindMode bindMode = BindMode.TOGGLE;

    private List<Setting<?>> settings;


    public List<Setting<?>> getSettings() {
        return settings;
    }


    protected Module() {
        var info = Optional.ofNullable(getClass().getAnnotation(ModuleInfo.class)).orElseThrow(() -> new IllegalStateException("Module %s must have @ModuleInfo annotation".formatted(getClass().getSimpleName())));
        this.data = new ModuleData(info.name(), info.category(), info.desc(), info.descKey(), info.bind());
        this.keybind = info.bind();
        this.settings = new ArrayList<>();
    }

    public final Module toggle() {
        return setEnabled(!enabled);
    }

    public final Module setEnabled(boolean enabled) {
        if (this.enabled == enabled) return this;

        this.enabled = enabled;

        if (enabled) {
            Manager.EVENT_BUS.subscribe(this);
            onEnable();
            notifyStatusChange(true);
        } else {
            Manager.EVENT_BUS.unsubscribe(this);
            onDisable();
            notifyStatusChange(false);
        }

        return this;
    }

    public final Module setKeybind(int keybind) {
        this.keybind = keybind;
        return this;
    }

    public BindMode getBindMode() {
        return bindMode;
    }

    public void setBindMode(BindMode bindMode) {
        this.bindMode = bindMode;
    }

    private void notifyStatusChange(boolean enabled) {
        String status = enabled ? "§aвключен" : "§cвыключен";
        ChatUtility.print("§7Модуль §f" + data.name() + " §7" + status);
    }

    protected void onEnable() {}
    protected void onDisable() {}

    public final String name() { return data.name(); }
    public final Category category() { return data.category(); }
    public final String description() { return data.description(); }
    public final String descKey() { return data.descKey(); }
    public final boolean isEnabled() { return enabled; }
    public final int keybind() { return keybind; }
    public final ModuleData data() { return data; }

    @Override
    public final String toString() { return data.name(); }

    @Override
    public final boolean equals(Object obj) {
        return obj instanceof Module module && data.name().equals(module.data.name());
    }

    @Override
    public final int hashCode() { return data.name().hashCode(); }
}