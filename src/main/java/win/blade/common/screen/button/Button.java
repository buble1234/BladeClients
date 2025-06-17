package win.blade.common.screen.button;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import org.joml.Matrix4f;
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.builders.states.QuadColorState;
import win.blade.common.utils.render.builders.states.QuadRadiusState;
import win.blade.common.utils.render.builders.states.SizeState;
import win.blade.common.utils.render.renderers.impl.BuiltRectangle;

import java.awt.*;

public class Button {
    private final int x;
    private final int y;
    private final int width;
    private final int height;
    private final Text text;
    private final Runnable onClick;
    private final TextRenderer textRenderer;

    public Button(int x, int y, int width, int height, Text text, Runnable onClick, TextRenderer textRenderer) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.text = text;
        this.onClick = onClick;
        this.textRenderer = textRenderer;
    }

    public void render(DrawContext context, int mouseX, int mouseY) {
        Color baseColor = isMouseOver(mouseX, mouseY) ? new Color(50, 50, 50) : new Color(30, 30, 30);

        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();
        BuiltRectangle rectangle = Builder.rectangle()
                .size(new SizeState(width, height))
                .color(new QuadColorState(baseColor, baseColor, baseColor, baseColor))
                .radius(new QuadRadiusState(0f, 0f, 0f, 0f))
                .smoothness(1.0f)
                .build();
        rectangle.render(matrix, x, y);

        int textX = x + (width - textRenderer.getWidth(text)) / 2;
        int textY = y + (height - textRenderer.fontHeight) / 2;
        context.drawText(textRenderer, text, textX, textY, Color.WHITE.getRGB(), false);
    }

    public boolean isMouseOver(int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    public void onClick() {
        if (onClick != null) {
            onClick.run();
        }
    }
}