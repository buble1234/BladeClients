package win.blade.common.gui.button;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import org.joml.Matrix4f;
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.builders.states.QuadColorState;
import win.blade.common.utils.render.builders.states.QuadRadiusState;
import win.blade.common.utils.render.builders.states.SizeState;
import win.blade.common.utils.render.msdf.FontType;
import win.blade.common.utils.render.msdf.MsdfFont;
import win.blade.common.utils.render.renderers.impl.BuiltRectangle;
import win.blade.common.utils.render.renderers.impl.BuiltText;

import java.awt.*;

public class Button {
    private final int x;
    private final int y;
    private final int width;
    private final int height;
    private final Text text;
    private final Runnable onClick;
    private final MsdfFont msdfFont;
    private final float fontSize;

    public Button(int x, int y, int width, int height, Text text, Runnable onClick, MsdfFont msdfFont, float fontSize) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.text = text;
        this.onClick = onClick;
        this.msdfFont = msdfFont;
        this.fontSize = fontSize;
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

        String textToRender = text.getString();
        float textWidth = msdfFont.getWidth(textToRender, fontSize);

        float textX = x + (width - textWidth) / 2.0f;

        float textY = y + height / 2.0f - 7;

        BuiltText render = Builder.text()
                .font(FontType.biko.get())
                .text(textToRender)
                .color(Color.WHITE)
                .size(fontSize)
                .thickness(0.05f)
                .build();
        render.render(matrix, textX, textY);
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