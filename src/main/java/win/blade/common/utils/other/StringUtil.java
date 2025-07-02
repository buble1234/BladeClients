package win.blade.common.utils.other;

import net.minecraft.client.util.InputUtil;
import win.blade.common.utils.render.msdf.FontType;
import win.blade.common.utils.render.msdf.MsdfFont;

import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static net.minecraft.client.util.InputUtil.Type.*;

public class StringUtil {

    public static String randomString(int length) {
        return IntStream.range(0, length)
                .mapToObj(operand -> String.valueOf((char) new Random().nextInt('a', 'z' + 1)))
                .collect(Collectors.joining());
    }

    public static String getBindName(int key) {
        InputUtil.Key isMouse = key < 8 ? MOUSE.createFromCode(key) : KEYSYM.createFromCode(key);

        InputUtil.Key code = key == -1
                ? SCANCODE.createFromCode(key)
                : isMouse;

        return key == -1 ? "N/A" : code
                .getTranslationKey()
                .replace("key.keyboard.", "")
                .replace("key.mouse.", "mouse ")
                .replace(".", " ")
                .toUpperCase();
    }

    public static String wrap(String input, int maxWidth, float fontSize) {
        if (input == null || input.isEmpty()) {
            return "";
        }
        MsdfFont font = FontType.sf_regular.get();
        String[] words = input.split(" ");
        StringBuilder output = new StringBuilder();
        float lineWidth = 0;

        for (String word : words) {
            float wordWidth = font.getWidth(word, fontSize);
            if (lineWidth > 0 && lineWidth + font.getWidth(" ", fontSize) + wordWidth > maxWidth) {
                output.append("\n");
                lineWidth = 0;
            }

            if (lineWidth > 0) {
                output.append(" ");
                lineWidth += font.getWidth(" ", fontSize);
            }

            output.append(word);
            lineWidth += wordWidth;
        }

        return output.toString();
    }

}
