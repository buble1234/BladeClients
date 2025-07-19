package win.blade.common.utils.keyboard;

import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import win.blade.common.utils.minecraft.MinecraftInstance;

import java.util.HashMap;
import java.util.Map;

public enum Keyboard implements MinecraftInstance {
    KEY_SPACE("SPACE", GLFW.GLFW_KEY_SPACE),
    KEY_APOSTROPHE("'", GLFW.GLFW_KEY_APOSTROPHE),
    KEY_COMMA(",", GLFW.GLFW_KEY_COMMA),
    KEY_MINUS("-", GLFW.GLFW_KEY_MINUS),
    KEY_PERIOD(".", GLFW.GLFW_KEY_PERIOD),
    KEY_SLASH("/", GLFW.GLFW_KEY_SLASH),
    KEY_0("0", GLFW.GLFW_KEY_0),
    KEY_1("1", GLFW.GLFW_KEY_1),
    KEY_2("2", GLFW.GLFW_KEY_2),
    KEY_3("3", GLFW.GLFW_KEY_3),
    KEY_4("4", GLFW.GLFW_KEY_4),
    KEY_5("5", GLFW.GLFW_KEY_5),
    KEY_6("6", GLFW.GLFW_KEY_6),
    KEY_7("7", GLFW.GLFW_KEY_7),
    KEY_8("8", GLFW.GLFW_KEY_8),
    KEY_9("9", GLFW.GLFW_KEY_9),
    KEY_SEMICOLON(";", GLFW.GLFW_KEY_SEMICOLON),
    KEY_EQUAL("=", GLFW.GLFW_KEY_EQUAL),
    KEY_A("A", GLFW.GLFW_KEY_A),
    KEY_B("B", GLFW.GLFW_KEY_B),
    KEY_C("C", GLFW.GLFW_KEY_C),
    KEY_D("D", GLFW.GLFW_KEY_D),
    KEY_E("E", GLFW.GLFW_KEY_E),
    KEY_F("F", GLFW.GLFW_KEY_F),
    KEY_G("G", GLFW.GLFW_KEY_G),
    KEY_H("H", GLFW.GLFW_KEY_H),
    KEY_I("I", GLFW.GLFW_KEY_I),
    KEY_J("J", GLFW.GLFW_KEY_J),
    KEY_K("K", GLFW.GLFW_KEY_K),
    KEY_L("L", GLFW.GLFW_KEY_L),
    KEY_M("M", GLFW.GLFW_KEY_M),
    KEY_N("N", GLFW.GLFW_KEY_N),
    KEY_O("O", GLFW.GLFW_KEY_O),
    KEY_P("P", GLFW.GLFW_KEY_P),
    KEY_Q("Q", GLFW.GLFW_KEY_Q),
    KEY_R("R", GLFW.GLFW_KEY_R),
    KEY_S("S", GLFW.GLFW_KEY_S),
    KEY_T("T", GLFW.GLFW_KEY_T),
    KEY_U("U", GLFW.GLFW_KEY_U),
    KEY_V("V", GLFW.GLFW_KEY_V),
    KEY_W("W", GLFW.GLFW_KEY_W),
    KEY_X("X", GLFW.GLFW_KEY_X),
    KEY_Y("Y", GLFW.GLFW_KEY_Y),
    KEY_Z("Z", GLFW.GLFW_KEY_Z),
    KEY_LEFT_BRACKET("[", GLFW.GLFW_KEY_LEFT_BRACKET),
    KEY_BACKSLASH("\\", GLFW.GLFW_KEY_BACKSLASH),
    KEY_RIGHT_BRACKET("]", GLFW.GLFW_KEY_RIGHT_BRACKET),
    KEY_GRAVE("`", GLFW.GLFW_KEY_GRAVE_ACCENT),
    KEY_ESCAPE("ESC", GLFW.GLFW_KEY_ESCAPE),
    KEY_ENTER("ENTER", GLFW.GLFW_KEY_ENTER),
    KEY_TAB("TAB", GLFW.GLFW_KEY_TAB),
    KEY_BACKSPACE("BACK", GLFW.GLFW_KEY_BACKSPACE),
    KEY_INSERT("INS", GLFW.GLFW_KEY_INSERT),
    KEY_DELETE("DEL", GLFW.GLFW_KEY_DELETE),
    KEY_RIGHT("RIGHT", GLFW.GLFW_KEY_RIGHT),
    KEY_LEFT("LEFT", GLFW.GLFW_KEY_LEFT),
    KEY_DOWN("DOWN", GLFW.GLFW_KEY_DOWN),
    KEY_UP("UP", GLFW.GLFW_KEY_UP),
    KEY_PAGE_UP("PGUP", GLFW.GLFW_KEY_PAGE_UP),
    KEY_PAGE_DOWN("PGDN", GLFW.GLFW_KEY_PAGE_DOWN),
    KEY_HOME("HOME", GLFW.GLFW_KEY_HOME),
    KEY_END("END", GLFW.GLFW_KEY_END),
    KEY_CAPS_LOCK("CAPS", GLFW.GLFW_KEY_CAPS_LOCK),
    KEY_SCROLL_LOCK("SCROLL", GLFW.GLFW_KEY_SCROLL_LOCK),
    KEY_NUM_LOCK("NUMLOCK", GLFW.GLFW_KEY_NUM_LOCK),
    KEY_PRINT_SCREEN("PRINT", GLFW.GLFW_KEY_PRINT_SCREEN),
    KEY_PAUSE("PAUSE", GLFW.GLFW_KEY_PAUSE),
    KEY_F1("F1", GLFW.GLFW_KEY_F1),
    KEY_F2("F2", GLFW.GLFW_KEY_F2),
    KEY_F3("F3", GLFW.GLFW_KEY_F3),
    KEY_F4("F4", GLFW.GLFW_KEY_F4),
    KEY_F5("F5", GLFW.GLFW_KEY_F5),
    KEY_F6("F6", GLFW.GLFW_KEY_F6),
    KEY_F7("F7", GLFW.GLFW_KEY_F7),
    KEY_F8("F8", GLFW.GLFW_KEY_F8),
    KEY_F9("F9", GLFW.GLFW_KEY_F9),
    KEY_F10("F10", GLFW.GLFW_KEY_F10),
    KEY_F11("F11", GLFW.GLFW_KEY_F11),
    KEY_F12("F12", GLFW.GLFW_KEY_F12),
    KEY_KP_0("NUM 0", GLFW.GLFW_KEY_KP_0),
    KEY_KP_1("NUM 1", GLFW.GLFW_KEY_KP_1),
    KEY_KP_2("NUM 2", GLFW.GLFW_KEY_KP_2),
    KEY_KP_3("NUM 3", GLFW.GLFW_KEY_KP_3),
    KEY_KP_4("NUM 4", GLFW.GLFW_KEY_KP_4),
    KEY_KP_5("NUM 5", GLFW.GLFW_KEY_KP_5),
    KEY_KP_6("NUM 6", GLFW.GLFW_KEY_KP_6),
    KEY_KP_7("NUM 7", GLFW.GLFW_KEY_KP_7),
    KEY_KP_8("NUM 8", GLFW.GLFW_KEY_KP_8),
    KEY_KP_9("NUM 9", GLFW.GLFW_KEY_KP_9),
    KEY_KP_DECIMAL("NUM .", GLFW.GLFW_KEY_KP_DECIMAL),
    KEY_KP_DIVIDE("NUM /", GLFW.GLFW_KEY_KP_DIVIDE),
    KEY_KP_MULTIPLY("NUM *", GLFW.GLFW_KEY_KP_MULTIPLY),
    KEY_KP_SUBTRACT("NUM -", GLFW.GLFW_KEY_KP_SUBTRACT),
    KEY_KP_ADD("NUM +", GLFW.GLFW_KEY_KP_ADD),
    KEY_KP_ENTER("NUM ENTER", GLFW.GLFW_KEY_KP_ENTER),
    KEY_KP_EQUAL("NUM =", GLFW.GLFW_KEY_KP_EQUAL),
    KEY_LEFT_SHIFT("LSHIFT", GLFW.GLFW_KEY_LEFT_SHIFT),
    KEY_LEFT_CONTROL("LCTRL", GLFW.GLFW_KEY_LEFT_CONTROL),
    KEY_LEFT_ALT("LALT", GLFW.GLFW_KEY_LEFT_ALT),
    KEY_LEFT_SUPER("LWIN", GLFW.GLFW_KEY_LEFT_SUPER),
    KEY_RIGHT_SHIFT("RSHIFT", GLFW.GLFW_KEY_RIGHT_SHIFT),
    KEY_RIGHT_CONTROL("RCTRL", GLFW.GLFW_KEY_RIGHT_CONTROL),
    KEY_RIGHT_ALT("RALT", GLFW.GLFW_KEY_RIGHT_ALT),
    KEY_RIGHT_SUPER("RWIN", GLFW.GLFW_KEY_RIGHT_SUPER),
    KEY_MENU("MENU", GLFW.GLFW_KEY_MENU),
    MOUSE_LEFT("MOUSE1", 0),
    MOUSE_RIGHT("MOUSE2", 1),
    MOUSE_MIDDLE("СКМ", 2),
    MOUSE_4("MOUSE4", 3),
    MOUSE_5("MOUSE5", 4),
    KEY_NONE("N/A", -1);

    private final String name;
    private final int key;

    Keyboard(String name, int key) {
        this.name = name;
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public int getKey() {
        return key;
    }

    private static final Map<Integer, Keyboard> KEY_MAP = new HashMap<>();

    static {
        for (Keyboard k : values()) {
            KEY_MAP.put(k.key, k);
        }
    }

    public static String getKeyName(int keyCode) {
        if (KEY_MAP.containsKey(keyCode)) {
            return KEY_MAP.get(keyCode).name;
        }
        return "NONE";
    }

    public static boolean isKeyDown(int keyCode) {
        return InputUtil.isKeyPressed(mc.getWindow().getHandle(), keyCode);
    }
}