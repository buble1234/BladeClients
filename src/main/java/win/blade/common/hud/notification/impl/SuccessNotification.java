package win.blade.common.hud.notification.impl;

import net.minecraft.client.gui.DrawContext;
import org.joml.Matrix4f;
import win.blade.common.hud.notification.Notification;
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.builders.states.QuadColorState;
import win.blade.common.utils.render.builders.states.QuadRadiusState;
import win.blade.common.utils.render.builders.states.SizeState;

import java.awt.Color;

public class SuccessNotification extends Notification {

    public SuccessNotification(String content, long delay, int index) {
        super(content, delay, index);
    }

    @Override
    public void render(DrawContext context) {
        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();

        float slide = slideAnimation.get();
        if (slide <= 0.01f) return;

        float alpha = fadeAnimation.get();
        float yPos = yOffsetAnimation.get();

        Builder.blur()
                .size(new SizeState(10 + font.getWidth(content, fontSize), 10 + fontSize))
                .radius(new QuadRadiusState(4f))
                .color(new QuadColorState(new Color(34, 75, 34, (int) (230 * alpha))))
                .blurRadius(10)
                .build()
                .render(matrix, mc.getWindow().getScaledWidth() - (10 + font.getWidth(content, fontSize)) - 5 +
                        (10 + font.getWidth(content, fontSize)) * (1.0f - slide), yPos);

        Builder.text()
                .font(font)
                .text(content)
                .size(fontSize)
                .color(new Color(100, 255, 100, (int) (255 * alpha)))
                .build()
                .render(matrix, mc.getWindow().getScaledWidth() - (10 + font.getWidth(content, fontSize)) - 5 +
                        (10 + font.getWidth(content, fontSize)) * (1.0f - slide) + 5, yPos + 5);
    }
}
