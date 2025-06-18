package win.blade.common.gui.impl.menu.settings;

import win.blade.common.gui.impl.menu.settings.Setting;

import java.util.function.Supplier;

public interface ISetting {
    Setting<?> setVisible(Supplier<Boolean> visibility);
}