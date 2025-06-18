package win.blade.common.gui.impl.menu.helpers;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import ru.blade.common.GuiRender.melon.interfaces.TextAlign;
import win.blade.common.utils.minecraft.MinecraftInstance;
import win.blade.common.utils.other.IMouse;
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.builders.states.QuadColorState;
import win.blade.common.utils.render.builders.states.QuadRadiusState;
import win.blade.common.utils.render.builders.states.SizeState;
import win.blade.common.utils.render.msdf.MsdfFont;

import java.awt.Color;

public class TextBox implements IMouse, MinecraftInstance {

    public static final String VALID_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!?@#$%^&*()-_=+[]{}|\\;:'\"<>,./`~ ";
    public static final String NUMBERS_ONLY = "0123456789.";

    public String text = "";
    public float x, y;
    public boolean selected;
    public int cursor;
    public double animatedCursorPosition;
    public MsdfFont font;
    public float fontSize;
    public int color;

    private TextAlign textAlign;
    private float posX;
    public String placeholder;
    public float width;
    private boolean hideCharacters;
    private boolean onlyNumbers;

    public TextBox(float x, float y, float width, MsdfFont font, float fontSize, int color, TextAlign align, String placeholder, boolean hideCharacters, boolean onlyNumbers) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.font = font;
        this.fontSize = fontSize;
        this.color = color;
        this.textAlign = align;
        this.placeholder = placeholder;
        this.hideCharacters = hideCharacters;
        this.onlyNumbers = onlyNumbers;
        this.posX = x;
    }

    public void draw(DrawContext context, float alpha) {
        cursor = Math.max(0, Math.min(cursor, text.length()));
        String displayedText = text;

        if (hideCharacters && !isEmpty()) {
            displayedText = "*".repeat(text.length());
        }

        switch (textAlign) {
            case CENTER -> {
                posX += ((x - font.getWidth(isEmpty() ? placeholder : displayedText, fontSize) / 2f) - posX) * 0.5;
            }
            case LEFT -> posX = x;
        }

        if (isEmpty()) {
            Builder.text()
                    .font(font)
                    .text(placeholder)
                    .size(fontSize)
                    .color(new Color(150, 150, 150, (int) (150 * (selected ? 0.6f : 0.4f) * alpha)))
                    .build()
                    .render(posX, y);
        } else {
            Builder.text()
                    .font(font)
                    .text(displayedText)
                    .size(fontSize)
                    .color(new Color(color, true))
                    .build()
                    .render(posX, y);
        }

        animatedCursorPosition += (font.getWidth(displayedText.substring(0, cursor), fontSize) - animatedCursorPosition) * 0.1;

        if (selected) {
            float blinkAlpha = (float) ((Math.sin(System.currentTimeMillis() / 200.0) + 1.0) / 2.0);
            Builder.rectangle()
                    .size(new SizeState(1.7f, fontSize))
                    .color(new QuadColorState(new Color(255, 255, 255, (int) (255 * blinkAlpha * alpha))))
                    .radius(new QuadRadiusState(0f))
                    .build()
                    .render((float) (posX + animatedCursorPosition), y +0.7f);
        }
    }

    public void mouseClicked(double mouseX, double mouseY, int button) {
        float hoverX = x;
        if (textAlign == TextAlign.CENTER) {
            hoverX -= width / 2f;
        }
        selected = isLClick(button) && isHover(mouseX, mouseY, hoverX, y, width, fontSize);
    }

    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!selected) return;
        cursor = Math.max(0, Math.min(cursor, text.length()));

        long handle = mc.getWindow().getHandle();

        if (InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_LEFT_CONTROL) && keyCode == GLFW.GLFW_KEY_V) {
            String clipboard = mc.keyboard.getClipboard();
            if (onlyNumbers) {
                clipboard = clipboard.replaceAll("[^0-9.]", "");
            }
            addText(clipboard, cursor);
            cursor += clipboard.length();
        } else if (keyCode == GLFW.GLFW_KEY_DELETE && !text.isEmpty() && cursor < text.length()) {
            removeText(cursor + 1);
        } else if (keyCode == GLFW.GLFW_KEY_BACKSPACE && !text.isEmpty() && cursor > 0) {
            if (InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_LEFT_CONTROL)) {
                while (!text.isEmpty() && cursor > 0) {
                    removeText(cursor--);
                }
            } else {
                removeText(cursor--);
            }
        } else if (keyCode == GLFW.GLFW_KEY_RIGHT) {
            if (InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_LEFT_CONTROL)) {
                cursor = text.length();
            } else {
                cursor++;
            }
        } else if (keyCode == GLFW.GLFW_KEY_LEFT) {
            if (InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_LEFT_CONTROL)) {
                cursor = 0;
            } else {
                cursor--;
            }
        } else if (keyCode == GLFW.GLFW_KEY_HOME) {
            cursor = 0;
        } else if (keyCode == GLFW.GLFW_KEY_END) {
            cursor = text.length();
        }
        cursor = Math.max(0, Math.min(cursor, text.length()));
    }

    public void charTyped(char chr, int modifiers) {
        if (!selected) return;
        cursor = Math.max(0, Math.min(cursor, text.length()));

        String validSet = onlyNumbers ? NUMBERS_ONLY : VALID_CHARS;
        if (validSet.indexOf(chr) != -1) {
            addText(Character.toString(chr), cursor);
            cursor++;
        }
        cursor = Math.max(0, Math.min(cursor, text.length()));
    }

    private void addText(String insertion, int position) {
        String newText = new StringBuilder(text).insert(position, insertion).toString();
        if (font.getWidth(newText, fontSize) <= width) {
            text = newText;
        }
    }

    private void removeText(int position) {
        text = new StringBuilder(text).deleteCharAt(position - 1).toString();
    }

    public boolean isEmpty() {
        return text.isEmpty();
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
        if (this.text == null) this.text = "";
        cursor = this.text.length();
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public void setColor(int color) {
        this.color = color;
    }
}
