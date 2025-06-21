package win.blade.common.utils.system;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinDef;

import java.util.regex.Pattern;

public class LayoutUtil {

    private static final Pattern pattern = Pattern.compile(".*\\p{IsCyrillic}.*");
    private static final long ru_id = 0x0419;

    private interface User32 extends Library {
        User32 INSTANCE = Native.load("user32", User32.class);
        WinDef.HKL GetKeyboardLayout(int idThread);
    }

    public static boolean isRussianLayout() {
        String os = System.getProperty("os.name").toLowerCase();
        if (!os.contains("win")) {
            return false;
        }

        try {
            WinDef.HKL hkl = User32.INSTANCE.GetKeyboardLayout(0);
            long langId = Pointer.nativeValue(hkl.getPointer()) & 0xFFFF;
            return langId == ru_id;
        } catch (Throwable e) {
            return false;
        }
    }

    public static boolean isCyrillic(String text) {
        return pattern.matcher(text).matches();
    }
}