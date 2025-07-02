package win.blade.common.gui.impl.gui.setting;

import com.google.common.collect.Lists;
import win.blade.common.gui.impl.gui.trait.Setupable;

import java.util.Arrays;
import java.util.List;

public class SettingRepository implements Setupable {
    private final List<Setting> settings = Lists.newArrayList();

    @Override
    public final void setup(Setting... setting) {
        settings.addAll(Arrays.asList(setting));
    }

    public Setting get(String name) {
        return settings.stream()
                .filter(setting -> setting.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public List<Setting> settings() {
        return settings;
    }
}