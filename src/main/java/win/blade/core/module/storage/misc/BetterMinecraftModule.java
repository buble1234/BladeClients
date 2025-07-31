package win.blade.core.module.storage.misc;

//import win.blade.common.gui.impl.menu.settings.impl.BooleanSetting;
import win.blade.common.gui.impl.gui.setting.implement.BooleanSetting;
import win.blade.core.module.api.Category;
import win.blade.core.module.api.Module;
import win.blade.core.module.api.ModuleInfo;

@ModuleInfo(
        name = "BetterMinecraft",
        category = Category.RENDER,
        desc = "Позволяет видеть невидимых существ и игроков"
)
public class BetterMinecraftModule extends Module {
    public final BooleanSetting simpleChat = new BooleanSetting("Простой чат", "");
    public final BooleanSetting chatHistory = new BooleanSetting("История чата", "");
    public final BooleanSetting antiSpam = new BooleanSetting("АнтиСпам в чате", "");

    public BetterMinecraftModule(){
        addSettings(simpleChat, chatHistory, antiSpam);
    }
}