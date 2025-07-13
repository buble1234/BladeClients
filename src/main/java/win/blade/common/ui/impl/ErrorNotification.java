package win.blade.common.ui.impl;

import net.minecraft.client.gui.DrawContext;
import org.joml.Matrix4f;
import win.blade.common.ui.Notification;
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.builders.states.QuadColorState;
import win.blade.common.utils.render.builders.states.QuadRadiusState;
import win.blade.common.utils.render.builders.states.SizeState;

import java.awt.Color;

public class ErrorNotification extends Notification {

    public ErrorNotification(String content, long delay, int index) {
        super(content, delay, index);
    }

    @Override
    public void render(DrawContext context) {
        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();

        float slide = slideAnimation.get();
        if (slide <= 0.01f) return;

        float alpha = fadeAnimation.get();
        float yPos = yOffsetAnimation.get();

        float tW = 120.5f;
        float tH = 32f;
        float radius = 12.5f;

        float paddingH = 19.5f;

        float xPos = mc.getWindow().getScaledWidth() - tW - 10 + (tW * (1.0f - slide));

        Builder.rectangle()
                .size(new SizeState(tW, tH))
                .color(new QuadColorState(
                        new Color(112, 34, 40, (int) (255 * alpha)),
                        new Color(20, 18, 27, (int) (255 * alpha)),
                        new Color(20, 18, 27, (int) (255 * alpha)),
                        new Color(45, 25, 34, (int) (255 * alpha))
                ))
                .radius(new QuadRadiusState(radius))
                .smoothness(1.0f)
                .build()
                .render(matrix, xPos, yPos);

        Builder.border()
                .size(new SizeState(tW, tH))
                .color(new QuadColorState(new Color(170, 160, 200, (int) (25 * alpha))))
                .radius(new QuadRadiusState(radius))
                .thickness(1)
                .build()
                .render(matrix, xPos, yPos);

        float tBH = 6 + 1 + 6;
        float tYStart = yPos + (tH - tBH) / 2f;

        Builder.text()
                .font(font)
                .text("Ooops...")
                .size(6)
                .color(new Color(216, 49, 51, (int) (255 * alpha)))
                .build()
                .render(matrix, xPos + paddingH, tYStart);

        Builder.text()
                .font(font)
                .text(content)
                .size(6)
                .color(new Color(165, 162, 178, (int) (255 * alpha)))
                .build()
                .render(matrix, xPos + paddingH, tYStart + 6 + 1);
    }
}