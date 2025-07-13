package win.blade.common.gui.impl.menu.settings.impl;

import win.blade.common.gui.impl.menu.settings.Setting;
import win.blade.core.module.api.Module;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

@SuppressWarnings("unchecked")
public class ListSetting<T> extends Setting<T> {

    public List<T> values;
    private T cachedValue;

    @SafeVarargs
    public ListSetting(Module parent, String name, T... values) {
        super(parent, name, values[0]);
        this.values = Arrays.asList(values);
        this.cachedValue = values[0];
    }

    public boolean is(T value) {
        return getValue().equals(value);
    }

    @Override
    public ListSetting<T> set(T value) {
        super.set(value);
        this.cachedValue = super.getValue();
        return this;
    }

    public ListSetting<T> setAsObject(final Object value) {
        super.set((T) value);
        this.cachedValue = super.getValue();
        return this;
    }

    @Override
    public ListSetting<T> setVisible(Supplier<Boolean> value) {
        return (ListSetting<T>) super.setVisible(value);
    }

    @Override
    public ListSetting<T> onAction(Runnable action) {
        return (ListSetting<T>) super.onAction(() -> {
            action.run();
            this.cachedValue = super.getValue();
        });
    }

    @Override
    public ListSetting<T> onSetVisible(Runnable action) {
        return (ListSetting<T>) super.onSetVisible(action);
    }

    @Override
    public T getValue() {
        if (cachedValue == null) {
            cachedValue = super.getValue();
        }
        return cachedValue;
    }
}