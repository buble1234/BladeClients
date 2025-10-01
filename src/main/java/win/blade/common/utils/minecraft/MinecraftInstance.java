package win.blade.common.utils.minecraft;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Window;
import win.blade.common.gui.impl.gui.components.implement.window.WindowManager;

/**
 * Автор: NoCap
 * Дата создания: 17.06.2025
 */

public interface MinecraftInstance {
    MinecraftClient mc = MinecraftClient.getInstance();
    WindowManager windowManager = new WindowManager();
}
