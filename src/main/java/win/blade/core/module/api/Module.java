package win.blade.core.module.api;

import win.blade.common.gui.impl.gui.setting.Setting;
import win.blade.common.gui.impl.gui.setting.implement.BooleanSetting;
import win.blade.common.gui.impl.gui.setting.implement.GroupSetting;
import win.blade.common.ui.NotificationType;
import win.blade.common.utils.config.ConfigManager;
import win.blade.common.utils.keyboard.Keyboard;
import win.blade.common.utils.minecraft.ChatUtility;
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
    public boolean enabled = false;
    private int keybind;
    private BindMode bindMode;
    public long holdDuration = 50;

    public int type = 1;

    private final List<Setting> settings =  new ArrayList<>();
    private final List<BooleanSetting> booleanSettings = new ArrayList<>();

    public List<Setting> settings() {
        return settings;
    }

    public void addSettings(Setting... settings){
        for (Setting setting : settings) {
            if(setting.hasAttachments()){
                for(Setting attachment : setting.getAttachments()){
                    if(attachment instanceof BooleanSetting booleanSetting){
                        booleanSettings.add(booleanSetting);
                    }
                }
            }

            if(setting instanceof GroupSetting groupSetting){
              for(Setting child : groupSetting.getSubSettings()){
                  if(child instanceof BooleanSetting booleanSetting){
                      booleanSettings.add(booleanSetting);
                  }
              }
            } else if(setting instanceof BooleanSetting booleanSetting){
                booleanSettings.add(booleanSetting);
            }

            this.settings.add(setting);
        }
    }

    protected Module() {
        var info = Optional.ofNullable(getClass().getAnnotation(ModuleInfo.class)).orElseThrow(() -> new IllegalStateException("Module %s must have @ModuleInfo annotation".formatted(getClass().getSimpleName())));
        this.data = new ModuleData(info.name(), info.category(), info.desc(), info.descKey(), info.bind());
        this.keybind = info.bind();
        bindMode = info.mode();
    }

    public final Module toggle() {
        Module result = setEnabled(!enabled);
        try {
            Manager.executorService.execute(() -> ConfigManager.instance.saveConfig("default"));
        } catch (Throwable ignored) {
            new Thread(() -> ConfigManager.instance.saveConfig("default")).start();
        }
        return result;
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

    public void handleSettingsBind(){
        for(BooleanSetting booleanSetting : booleanSettings){
            if(booleanSetting.getKey() == -1) continue;

            boolean isPressed = Keyboard.isKeyDown(booleanSetting.getKey());
            boolean wasPressed = Manager.wasKeyPressed.getOrDefault(booleanSetting.toString(), false);

//            ChatUtility.print("handling " + booleanSetting.toString());

            if(booleanSetting.getType() == 1){
                 if(isPressed && !wasPressed){
                     booleanSetting.setValue(!booleanSetting.getValue());
                 }
            } else {
                if (isPressed != wasPressed) {
                    Manager.executorService.schedule(() -> {
                        booleanSetting.setValue(! booleanSetting.getValue());
                    }, booleanSetting.getHoldDuration(), TimeUnit.MILLISECONDS);
                }
            }

            Manager.wasKeyPressed.put(booleanSetting.toString(), isPressed);
        }
    }

    public void scheduledToggle(boolean state){
        Manager.executorService.schedule(() -> {
            setEnabled(state);
            ConfigManager.instance.saveConfig("default");
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

    public Category getCategory() {
        return this.data.category();
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