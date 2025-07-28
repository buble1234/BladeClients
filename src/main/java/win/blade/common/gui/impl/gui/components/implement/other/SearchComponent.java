package win.blade.common.gui.impl.gui.components.implement.other;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;
import win.blade.common.gui.impl.gui.components.AbstractComponent;
import win.blade.common.utils.color.ColorUtility;
import win.blade.common.utils.math.MathUtility;
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.builders.states.QuadColorState;
import win.blade.common.utils.render.builders.states.QuadRadiusState;
import win.blade.common.utils.render.builders.states.SizeState;
import win.blade.common.utils.render.msdf.FontType;
import win.blade.common.utils.render.msdf.MsdfFont;

import java.awt.Color;

public class SearchComponent extends AbstractComponent {
    private String text = "";

    private int cursorPosition = 0;
    private int selectionStart = -1;
    private int selectionEnd = -1;
    private boolean isTyping = false;

    private final MsdfFont fontRegular = FontType.popins_regular.get();

    public String getText() {
        return text;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        Matrix4f positionMatrix = context
                .getMatrices()
                .peek()
                .getPositionMatrix();

        width = 108;
        height = 13.5f;

        Builder.rectangle()
                .size(new SizeState(width, height))
                .color(new QuadColorState(new Color(28,26,37)))
                .radius(new QuadRadiusState(6))
                .build()
                .render(x -21, y+1);

        AbstractTexture searchTexture = MinecraftClient.getInstance().getTextureManager().getTexture(Identifier.of("blade", "textures/search.png"));

        Builder.texture()
                .size(new SizeState(5, 5))
                .color(new QuadColorState(Color.WHITE))
                .texture(0f, 0f, 1f, 1f, searchTexture)
                .radius(new QuadRadiusState(0f))
                .build()
                .render(x + width - 32, y + 5);

        String displayText = text.equalsIgnoreCase("") && !isTyping ? "Search" : text;

        Builder.text()
                .font(fontRegular)
                .text(displayText)
                .size(4.5f)
                .color(ColorUtility.fromHex("8C889A"))
                .build()
                .render( x -13, y + 4);

        if (isTyping) {
            float cursorX = x -14 + fontRegular.getWidth(text.substring(0, cursorPosition), 5.2f);

            Builder.rectangle()
                    .size(new SizeState(0.5f, height - 8))
                    .color(new QuadColorState(Color.WHITE))
                    .radius(new QuadRadiusState(0f))
                    .build()
                    .render(cursorX, y + 5);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (MathUtility.isHovered(mouseX, mouseY, x-40, y, width, height)) {
            cursorPosition = getClickedPosition(mouseX);
            selectionStart = -1;
            selectionEnd = -1;
            isTyping = true;
            return true;
        } else {
            isTyping = false;
            return super.mouseClicked(mouseX, mouseY, button);
        }
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (isTyping && fontRegular.getWidth(text, 6) < 55) {
            updateText(chr);
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (isTyping) {
            switch (keyCode) {
                case GLFW.GLFW_KEY_BACKSPACE -> handleBackspace();
                case GLFW.GLFW_KEY_LEFT -> {
                    if (cursorPosition > 0) {
                        cursorPosition--;
                    }
                }
                case GLFW.GLFW_KEY_RIGHT -> {
                    if (cursorPosition < text.length()) {
                        cursorPosition++;
                    }
                }
                case GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_ESCAPE -> isTyping = false;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (isTyping && button == 0 && selectionStart != -1 && selectionEnd == -1) {
            selectionEnd = cursorPosition;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private void handleBackspace() {
        if (cursorPosition > 0) {
            if (selectionStart != -1 && selectionEnd != -1) {
                int start = Math.min(selectionStart, selectionEnd);
                int end = Math.max(selectionStart, selectionEnd);
                text = text.substring(0, start) + text.substring(end);
                cursorPosition = start;
                selectionStart = -1;
                selectionEnd = -1;
            } else {
                text = text.substring(0, cursorPosition - 1) + text.substring(cursorPosition);
                cursorPosition--;
            }
        }
    }

    private void updateText(char chr) {
        if (selectionStart != -1 && selectionEnd != -1) {
            int start = Math.min(selectionStart, selectionEnd);
            int end = Math.max(selectionStart, selectionEnd);
            text = text.substring(0, start) + chr + text.substring(end);
            cursorPosition = start + 1;
            selectionStart = -1;
            selectionEnd = -1;
        } else {
            text = text.substring(0, cursorPosition) + chr + text.substring(cursorPosition);
            cursorPosition++;
        }
    }

    private int getClickedPosition(double mouseX) {
        int relativeX = (int) (mouseX - x - 7);
        for (int i = 0; i <= text.length(); i++) {
            if (fontRegular.getWidth(text.substring(0, i), 6) > relativeX) {
                return i;
            }
        }
        return text.length();
    }
}