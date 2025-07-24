package win.blade.common.utils.color;

import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;

import java.awt.*;

/**
 * Автор: NoCap
 * Дата создания: 17.06.2025
 */

public class ColorUtility {

    public static int pack(int red, int green, int blue, int alpha) {
        return ((alpha & 0xFF) << 24) | ((red & 0xFF) << 16) | ((green & 0xFF) << 8) | ((blue & 0xFF) << 0);
    }

    public static int[] unpack(int color) {
        return new int[] {color >> 16 & 0xFF, color >> 8 & 0xFF, color & 0xFF, color >> 24 & 0xFF};
    }


    public static int fade(int speed, int index, int first, int second) {
        int angle = (int) ((System.currentTimeMillis() / speed + index) % 360);
        angle = angle >= 180 ? 360 - angle : angle;
        return overCol(first, second, angle / 180f);
    }

    public static int fade(int index) {
        int clientColor = pack(196, 24, 24, 255);
        int darkColor = multDark(clientColor, 0.6f); // пример тёмного варианта, подкорректируй если нужно
        return fade(5, index, clientColor, darkColor); // speed=5 как пример, в оригинале из Theme.getSpeed()
    }


    public static int overCol(int color1, int color2, float factor) {
        float f = MathHelper.clamp(factor, 0.0f, 1.0f);
        return ColorHelper.getArgb(
                (int) MathHelper.lerp(f, ColorHelper.getAlpha(color1), ColorHelper.getAlpha(color2)),
                (int) MathHelper.lerp(f, ColorHelper.getRed(color1), ColorHelper.getRed(color2)),
                (int) MathHelper.lerp(f, ColorHelper.getGreen(color1), ColorHelper.getGreen(color2)),
                (int) MathHelper.lerp(f, ColorHelper.getBlue(color1), ColorHelper.getBlue(color2))
        );
    }

    public static int getRed(int hex) {
        return hex >> 16 & 255;
    }

    public static int getGreen(int hex) {
        return hex >> 8 & 255;
    }

    public static int getBlue(int hex) {
        return hex & 255;
    }

    public static int getAlpha(int hex) {
        return hex >> 24 & 255;
    }

    public static int applyOpacity(int color, float opacity) {
        return ColorHelper.getArgb((int) (getAlpha(color) * opacity / 255), getRed(color), getGreen(color), getBlue(color));
    }

    public static int multDark(int color, float factor) {
        return ColorHelper.getArgb(
                ColorHelper.getAlpha(color),
                Math.round(ColorHelper.getRed(color) * factor),
                Math.round(ColorHelper.getGreen(color) * factor),
                Math.round(ColorHelper.getBlue(color) * factor)
        );
    }


    public static float[] normalize(Color color) {
        return new float[] {color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f, color.getAlpha() / 255.0f};
    }

    public static float[] normalize(int color) {
        int[] components = unpack(color);
        return new float[] {components[0] / 255.0f, components[1] / 255.0f, components[2] / 255.0f, components[3] / 255.0f};
    }


    public static int applyAlpha(int color, float alpha) {
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        return pack(r, g, b, (int) (255 * alpha));
    }

    public static Color fromHex(String hex) {
        if (hex == null) {
            throw new IllegalArgumentException("Hex строка не может быть нулевой.");
        }

        if (hex.startsWith("#")) {
            hex = hex.substring(1);
        }

        if (hex.length() != 6 && hex.length() != 8) {
            throw new IllegalArgumentException("Недопустимая длина Hex строки. Должно быть 6 или 8 символов, но была " + hex.length());
        }

        try {
            long longValue = Long.parseLong(hex, 16);

            if (hex.length() == 6) {
                int r = (int) ((longValue >> 16) & 0xFF);
                int g = (int) ((longValue >> 8) & 0xFF);
                int b = (int) (longValue & 0xFF);
                return new Color(r, g, b);
            } else {
                int r = (int) ((longValue >> 24) & 0xFF);
                int g = (int) ((longValue >> 16) & 0xFF);
                int b = (int) ((longValue >> 8) & 0xFF);
                int a = (int) (longValue & 0xFF);
                return new Color(r, g, b, a);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid characters in hex string: \"" + hex + "\"", e);
        }
    }

//    public static int getAlpha(int color) {
//        return (color >> 24) & 0xFF;
//    }
}
