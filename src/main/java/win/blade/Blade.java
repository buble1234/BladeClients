package win.blade;

import net.fabricmc.api.ModInitializer;
import win.blade.core.Manager;

public class Blade implements ModInitializer {

    public static Manager manager = new Manager();

    @Override
    public void onInitialize() {
        manager.init();
    }
}