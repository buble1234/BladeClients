package win.blade.common.hud;

import java.util.HashMap;
import java.util.Map;

public class HudHolder {
    private static final Map<String, AbstractHudElement> elements = new HashMap<>();

    public static void registerElement(String name, AbstractHudElement element) {
        elements.put(name, element);
    }

    public static AbstractHudElement getElement(String name) {
        return elements.get(name);
    }
}