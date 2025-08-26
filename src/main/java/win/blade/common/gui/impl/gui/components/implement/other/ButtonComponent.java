package win.blade.common.gui.impl.gui.components.implement.other;

import net.minecraft.client.gui.DrawContext;
import org.joml.Matrix4f;
import win.blade.common.gui.impl.gui.components.AbstractComponent;
import win.blade.common.utils.math.MathUtility;
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.builders.states.QuadColorState;
import win.blade.common.utils.render.builders.states.QuadRadiusState;
import win.blade.common.utils.render.builders.states.SizeState;
import win.blade.common.utils.render.msdf.FontType;
import win.blade.common.utils.render.msdf.MsdfFont;

import java.awt.Color;

public class ButtonComponent extends AbstractComponent {
    private String text;
    private Runnable runnable;
    private int color = new Color(102,60,255).getRGB();

    private final MsdfFont fontRegular = FontType.sf_regular.get();

    public ButtonComponent setText(String text) {
        this.text = text;
        return this;
    }

    public ButtonComponent setRunnable(Runnable runnable) {
        this.runnable = runnable;
        return this;
    }

    public ButtonComponent setColor(int color) {
        this.color = color;
        return this;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        Matrix4f positionMatrix = context
                .getMatrices()
                .peek()
                .getPositionMatrix();

        width = FontType.popins_regular.get().getWidth(text, 6) +3;
        height = 9.5f;

        Builder.rectangle()
                .size(new SizeState(21, height))
                .color(new QuadColorState(color))
                .radius(new QuadRadiusState(1.5f))
                .build()
                .render(x+7, y);

        float textWidth = fontRegular.getWidth(text, 6);
        float centeredX = x + (width - textWidth) / 2;

        Builder.text()
                .font(FontType.popins_regular.get())
                .text(text)
                .size(4)
                .color(Color.WHITE)
                .build()
                .render( centeredX +7f, y + 1.5f);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (MathUtility.isHovered(mouseX, mouseY, x, y, width, height) && button == 0) {
            runnable.run();
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}