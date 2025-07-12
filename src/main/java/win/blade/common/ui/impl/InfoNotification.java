package win.blade.common.ui.impl;

import net.minecraft.client.gui.DrawContext;
import org.joml.Matrix4f;
import win.blade.common.ui.Notification;
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.builders.states.QuadColorState;
import win.blade.common.utils.render.builders.states.QuadRadiusState;
import win.blade.common.utils.render.builders.states.SizeState;

import java.awt.Color;

public class InfoNotification extends Notification {

    public InfoNotification(String content, long delay, int index) {
        super(content, delay, index);
    }

    @Override
    public void render(DrawContext context) {
        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();

        float slide = slideAnimation.get();
        if (slide <= 0.01f) return;

        float alpha = fadeAnimation.get();
        float yPos = yOffsetAnimation.get();

        float width = 10 + font.getWidth(content, fontSize);
        float height = 10 + fontSize;

        float screenWidth = mc.getWindow().getScaledWidth();
        float xOffset = width * (1.0f - slide);
        float xPos = screenWidth - width - 5 + xOffset;

        Builder.blur()
                .size(new SizeState(width, height))
                .radius(new QuadRadiusState(4f))
                .color(new QuadColorState(new Color(24, 25, 34, (int) (230 * alpha))))
                .blurRadius(10)
                .build()
                .render(matrix, xPos, yPos);

        Builder.text()
                .font(font)
                .text(content)
                .size(fontSize)
                .color(new Color(255, 255, 255, (int) (255 * alpha)))
                .build()
                .render(matrix, xPos + 5, yPos + 5);
    }
}