package win.blade.common.gui.impl.menu.settings;

import win.blade.common.utils.math.animation.Animation;
import win.blade.common.utils.minecraft.MinecraftInstance;
import win.blade.core.module.api.Module;

import java.util.function.Supplier;

public class Setting<T> implements MinecraftInstance {

    private String name;
    private T value;
    private Module parent;

    private Supplier<Boolean> visible = () -> true;

    private Runnable onAction;
    private Runnable onSetVisible;
    private final Animation animation = new Animation();

    // Этот конструктор теперь становится главным. Он позволяет создавать сеттинги без модуля.
    public Setting(Module parent, String name, T value) {
        this.parent = parent;
        this.name = name;
        this.value = value;
        if (parent != null) {
            parent.getSettings().add(this);
        }
    }

    // Этот конструктор теперь просто вызывает главный, передавая null в качестве родителя.
    public Setting(String name, T value) {
        this(null, name, value);
    }

    public T getValue() {
        return value;
    }

    public Setting<T> set(T value) {
        this.value = value;
        if (mc.world != null && mc.player != null && onAction != null) {
            onAction.run();
        }
        return this;
    }

    public Setting<T> setVisible(Supplier<Boolean> value) {
        this.visible = value;
        if (mc.world != null && mc.player != null && onSetVisible != null) {
            onSetVisible.run();
        }
        return this;
    }

    public Setting<T> onAction(Runnable action) {
        this.onAction = action;
        return this;
    }

    public Setting<T> onSetVisible(Runnable action) {
        this.onSetVisible = action;
        return this;
    }

    public String getName() {
        return name;
    }

    public Module getParent() {
        return parent;
    }

    public Supplier<Boolean> getVisible() {
        return visible;
    }

    public Animation getAnimation() {
        return animation;
    }
}