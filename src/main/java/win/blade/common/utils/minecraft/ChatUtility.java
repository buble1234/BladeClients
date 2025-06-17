package win.blade.common.utils.minecraft;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static win.blade.common.utils.minecraft.MinecraftInstance.mc;

public class ChatUtility {

    public static void print(String text, String string, int entryScore) {
        add(Text.of(text));
    }

    public static void print(String text) {
        add(Text.of(text));
    }

    public static void print(Object... objects){
        StringBuilder builder = new StringBuilder();

        for (Object object : objects) {
            builder.append(object.toString()).append(" ");
        }

        print(builder.toString());
    }


    public static void add(Text message) {
        if (mc.inGameHud != null) {
            mc.inGameHud.getChatHud().addMessage(message);
        } else {
            System.out.println(message.getString());
        }
    }
}
