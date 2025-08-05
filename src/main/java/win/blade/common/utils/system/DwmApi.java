package win.blade.common.utils.system;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.ptr.IntByReference;

/**
 * Автор: NoCap
 * Дата создания: 05.08.2025
 */
public interface DwmApi extends Library {
    DwmApi INSTANCE = Native.load("dwmapi", DwmApi.class);
    IntByReference useDarkTheme = new IntByReference(1);
    int dark = 20;

    int DwmSetWindowAttribute(
        HWND hwnd,
        int dwAttribute,
        Pointer pvAttribute,
        int cbAttribute
    );
}