package win.blade.common.gui.impl.gui.components.implement.settings;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;
import win.blade.common.gui.impl.gui.setting.implement.TextSetting;
import win.blade.common.utils.color.ColorUtility;
import win.blade.common.utils.math.MathUtility;
import win.blade.common.utils.minecraft.MinecraftInstance;
import win.blade.common.utils.other.StringUtil;
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.builders.states.QuadColorState;
import win.blade.common.utils.render.builders.states.QuadRadiusState;
import win.blade.common.utils.render.builders.states.SizeState;
import win.blade.common.utils.render.msdf.FontType;
import win.blade.common.utils.render.msdf.MsdfFont;

import java.awt.Color;

public class TextComponent extends AbstractSettingComponent implements MinecraftInstance {
    private final TextSetting setting;
    private float rectX, rectY, rectWidth, rectHeight;
    public static boolean typing;
    private boolean dragging;
    private String text = "";
    private int cursorPosition = 0;
    private int selectionStart = -1;
    private int selectionEnd = -1;
    private long lastClickTime = 0;
    private float xOffset = 0;
    private long textInputCursor = GLFW.glfwCreateStandardCursor(GLFW.GLFW_IBEAM_CURSOR);
    private long defaultCursor = GLFW.glfwCreateStandardCursor(GLFW.GLFW_ARROW_CURSOR);
    private long lastInputTime = System.currentTimeMillis();
    
    private final MsdfFont fontRegular = FontType.sf_regular.get();

    public TextComponent(TextSetting setting) {
        super(setting);
        this.setting = setting;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        Matrix4f positionMatrix = context
                .getMatrices()
                .peek()
                .getPositionMatrix();

        String wrapped = StringUtil.wrap(setting.getDescription(), 70, 6);
        height = (int) (18 + fontRegular.getFontHeight(fontRegular,6) * (wrapped.split("\n").length - 1));

        this.rectX = x + width - 61.5F;
        this.rectY = y + 6.0F;
        this.rectWidth = 53.0F;
        this.rectHeight = 12.0F;

        Builder.rectangle()
                .size(new SizeState(rectWidth, rectHeight))
                .color(new QuadColorState(ColorUtility.fromHex("1C1A25")))
                .radius(new QuadRadiusState(4))
                .build()
                .render(rectX, rectY + 1);

        int min = setting.getMin();
        int max = setting.getMax();

        int color = (min > text.length() || max < text.length())
                ? 0xFF878894
                : 0xFF10C97B;

        Builder.texture()
                .size(new SizeState(5F, 5f))
                .color(new QuadColorState(new Color(color)))
                .svgTexture(0f, 0f, 1f, 1f, Identifier.of("blade", "textures/svg/gui/check.svg"))
                .radius(new QuadRadiusState(0f))
                .build()
                .render(rectX + rectWidth - 8.5f, rectY + (rectHeight / 2) - 1.25f);

        Builder.text()
                .font(fontRegular)
                .text(setting.getName())
                .size(6)
                .color(new Color(0xFFD4D6E1))
                .build()
                .render( x + 9, y + 8 + addJust());

        if(shouldRenderDescription)
            Builder.text()
                .font(fontRegular)
                .text(wrapped)
                .size(5)
                .color(new Color(0xFF878894))
                .build()
                .render( x + 9, y + 15);

        updateXOffset(cursorPosition);

        if (selectionStart != -1 && selectionEnd != -1 && selectionStart != selectionEnd) {
            int start = Math.max(0, Math.min(getStartOfSelection(), text.length()));
            int end = Math.max(0, Math.min(getEndOfSelection(), text.length()));
            if (start < end) {
                float selectionXStart = rectX + 3 - xOffset + fontRegular.getWidth(text.substring(0, start), 6);
                float selectionXEnd = rectX + 3 - xOffset + fontRegular.getWidth(text.substring(0, end), 6);
                float selectionWidth = selectionXEnd - selectionXStart;

                Builder.rectangle()
                        .size(new SizeState(selectionWidth + 0.5F, 10.0f))
                        .color(new QuadColorState(new Color(0xFF5585E8)))
                        .radius(new QuadRadiusState(0))
                        .build()
                        .render(selectionXStart - 0.5F, rectY + (rectHeight / 2) - 4f);
            }
        }

        Builder.text()
                .font(fontRegular)
                .text(text)
                .size(6)
                .color(new Color(typing ? -1 : 0xFF878894))
                .build()
                .render( rectX + 3 - xOffset, rectY + (rectHeight / 2) - 2.5f);

        if (!typing && text.isEmpty()) {
            Builder.text()
                    .font(fontRegular)
                    .text(setting.getText())
                    .size(6)
                    .color(new Color(0xFF878894))
                    .build()
                    .render( rectX + 3, rectY + (rectHeight / 2) - 3.5f);
        }

        long currentTime = System.currentTimeMillis();
        boolean focused = typing && (currentTime - lastInputTime < 500 || currentTime % 1000 < 500);

        if (focused && (selectionStart == -1 || selectionStart == selectionEnd)) {
            float cursorX = fontRegular.getWidth(text.substring(0, cursorPosition), 6);
            
            Builder.text()
                    .font(fontRegular)
                    .text("|")
                    .size(6)
                    .color(Color.WHITE)
                    .build()
                    .render( rectX + 3 - xOffset + cursorX, rectY + (rectHeight / 2) - 2.5f);
        }

        if (dragging) {
            cursorPosition = getCursorIndexAt(mouseX);

            if (selectionStart == -1) {
                selectionStart = cursorPosition + 1;
            }
            selectionEnd = cursorPosition;
        }
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        dragging = true;
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (MathUtility.isHovered(mouseX, mouseY, rectX, rectY, rectWidth, rectHeight) && button == 0) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastClickTime < 250) {
                selectionStart = 0;
                selectionEnd = text.length();
            } else {
                typing = true;
                dragging = true;
                lastClickTime = currentTime;
                cursorPosition = getCursorIndexAt(mouseX);
                selectionStart = cursorPosition;
                selectionEnd = cursorPosition;
            }
        } else {
//            typing = false;
            handleTextModification(GLFW.GLFW_KEY_ENTER);

            clearSelection();
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        dragging = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (typing && (text.length() < setting.getMax())) {
            deleteSelectedText();
            text = text.substring(0, cursorPosition) + chr + text.substring(cursorPosition);
            cursorPosition++;
            clearSelection();
            lastInputTime = System.currentTimeMillis();
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (typing) {
            if (Screen.hasControlDown()) {
                switch (keyCode) {
                    case GLFW.GLFW_KEY_A -> selectAllText();
                    case GLFW.GLFW_KEY_V -> pasteFromClipboard();
                    case GLFW.GLFW_KEY_C -> copyToClipboard();
                }
            } else {
                switch (keyCode) {
                    case GLFW.GLFW_KEY_BACKSPACE, GLFW.GLFW_KEY_ENTER -> handleTextModification(keyCode);
                    case GLFW.GLFW_KEY_LEFT, GLFW.GLFW_KEY_RIGHT -> moveCursor(keyCode);
                }
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void pasteFromClipboard() {
        String clipboardText = GLFW.glfwGetClipboardString(window.getHandle());
        if (clipboardText != null) {
            replaceText(cursorPosition, cursorPosition, clipboardText);
        }
    }

    private void copyToClipboard() {
        if (hasSelection()) {
            GLFW.glfwSetClipboardString(window.getHandle(), getSelectedText());
        }
    }

    private void selectAllText() {
        selectionStart = 0;
        selectionEnd = text.length();
    }

    private void handleTextModification(int keyCode) {
        if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            if (hasSelection()) {
                replaceText(getStartOfSelection(), getEndOfSelection(), "");
            } else if (cursorPosition > 0) {
                replaceText(cursorPosition - 1, cursorPosition, "");
            }
        } else if (keyCode == GLFW.GLFW_KEY_ENTER) {
            if (text.length() >= setting.getMin() && text.length() <= setting.getMax()) {
                setting.setText(text);
                typing = false;
            }
        }
    }

    private void moveCursor(int keyCode) {
        if (keyCode == GLFW.GLFW_KEY_LEFT && cursorPosition > 0) {
            cursorPosition--;
        } else if (keyCode == GLFW.GLFW_KEY_RIGHT && cursorPosition < text.length()) {
            cursorPosition++;
        }
        updateSelectionAfterCursorMove();
    }

    private void updateSelectionAfterCursorMove() {
        if (Screen.hasShiftDown()) {
            if (selectionStart == -1) selectionStart = cursorPosition;
            selectionEnd = cursorPosition;
        } else {
            clearSelection();
        }
        lastInputTime = System.currentTimeMillis();
    }

    private void replaceText(int start, int end, String replacement) {
        if (start < 0) start = 0;
        if (end > text.length()) end = text.length();
        if (start > end) start = end;

        text = text.substring(0, start) + replacement + text.substring(end);
        cursorPosition = start + replacement.length();
        clearSelection();
        lastInputTime = System.currentTimeMillis();
    }

    private boolean hasSelection() {
        return selectionStart != -1 && selectionEnd != -1 && selectionStart != selectionEnd;
    }

    private String getSelectedText() {
        return text.substring(getStartOfSelection(), getEndOfSelection());
    }

    private int getStartOfSelection() {
        return Math.min(selectionStart, selectionEnd);
    }

    private int getEndOfSelection() {
        return Math.max(selectionStart, selectionEnd);
    }

    private void clearSelection() {
        selectionStart = -1;
        selectionEnd = -1;
    }

    private int getCursorIndexAt(double mouseX) {
        float relativeX = (float) mouseX - rectX - 3 + xOffset;
        int position = 0;
        while (position < text.length()) {
            float textWidth = fontRegular.getWidth(text.substring(0, position + 1), 6);
            if (textWidth > relativeX) {
                break;
            }
            position++;
        }
        return position;
    }

    private void updateXOffset(int cursorPosition) {
        float cursorX = fontRegular.getWidth(text.substring(0, cursorPosition), 6);
        if (cursorX < xOffset) {
            xOffset = cursorX;
        } else if (cursorX - xOffset > rectWidth - 17) {
            xOffset = cursorX - (rectWidth - 17);
        }
    }

    private void deleteSelectedText() {
        if (hasSelection()) {
            replaceText(getStartOfSelection(), getEndOfSelection(), "");
        }
    }
}