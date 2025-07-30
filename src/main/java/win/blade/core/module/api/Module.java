package win.blade.core.module.api;

import win.blade.common.gui.impl.gui.setting.Setting;
import win.blade.common.ui.NotificationType;
import win.blade.common.utils.minecraft.MinecraftInstance;
import win.blade.common.utils.other.SoundUtility;
import win.blade.core.Manager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public abstract class Module implements MinecraftInstance {

    public record ModuleData(String name, Category category, String description, String descKey, int defaultKeybind) {}

    private final ModuleData data;
    private boolean enabled = false;
    private int keybind;
    private BindMode bindMode;
    public long holdDuration = 50;

    public String getVisibleName() { return "Module"; }

    public Category getCategory() { return this.data.category(); }
    public int type = 1;

    List<Setting> settings =  new ArrayList<>();

    public List<Setting> settings() {
        return settings.stream().filter(win.blade.common.gui.impl.gui.setting.Setting::notInBox).toList();
    }

    public void addSettings(Setting... settings){
        this.settings.addAll(Arrays.asList(settings));
    }

    protected Module() {
        var info = Optional.ofNullable(getClass().getAnnotation(ModuleInfo.class)).orElseThrow(() -> new IllegalStateException("Module %s must have @ModuleInfo annotation".formatted(getClass().getSimpleName())));
        this.data = new ModuleData(info.name(), info.category(), info.desc(), info.descKey(), info.bind());
        this.keybind = info.bind();
        bindMode = info.mode();
    }

    public final Module toggle() {
        return setEnabled(!enabled);
    }

    public final Module setEnabled(boolean enabled) {
        if (this.enabled == enabled) return this;

        this.enabled = enabled;

        if (enabled) {
            Manager.EVENT_BUS.subscribe(this);
            SoundUtility.playSound(SoundUtility.SoundType.MODULE_ENABLE);
            onEnable();
        } else {
            Manager.EVENT_BUS.unsubscribe(this);
            onDisable();
            SoundUtility.playSound(SoundUtility.SoundType.MODULE_DISABLE);
        }

        status(enabled);
        return this;
    }

    public final Module toggleWithoutNotification(boolean value){
        if(enabled == value) return this;

        enabled = value;

        if(value){
            Manager.EVENT_BUS.subscribe(this);
            onEnable();
        } else {
            Manager.EVENT_BUS.unsubscribe(this);
            onDisable();
        }

        return this;
    }

    public void scheduledToggle(boolean state){
        Manager.executorService.schedule(() -> {
            setEnabled(state);
        }, holdDuration, TimeUnit.MILLISECONDS);
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

    private void status(boolean enabled) {
        Manager.notificationManager.add(data.name() + " " + (enabled ? "enabled" : "disabled"),
                enabled ? NotificationType.SUCCESS : NotificationType.ERROR, 2000);
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