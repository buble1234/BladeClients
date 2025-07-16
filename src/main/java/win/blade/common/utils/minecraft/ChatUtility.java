package win.blade.common.utils.minecraft;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import win.blade.common.utils.other.Result;

import java.awt.*;

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


    public static void printResult(Result<?, ?> result, String success){
        if(result.isFailure()){
            add(Text.literal(result.error().toString()).formatted(Formatting.RED));
        } else {
            add(Text.literal(success).formatted(Formatting.GREEN));
        }
    }

    public static @NotNull MutableText sendClientPrefix() {
        MutableText prefix = Text.literal("[Blade]").formatted(Formatting.WHITE);
        MutableText arrow = Text.literal("➜").formatted(Formatting.WHITE);

        return prefix.append(Text.literal(" ").formatted(Formatting.RESET)).append(arrow);
    }
}
