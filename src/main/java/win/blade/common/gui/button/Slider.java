package win.blade.common.gui.button;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import org.joml.Matrix4f;
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.builders.states.QuadColorState;
import win.blade.common.utils.render.builders.states.QuadRadiusState;
import win.blade.common.utils.render.builders.states.SizeState;
import win.blade.common.utils.render.msdf.FontType;
import win.blade.common.utils.render.renderers.impl.BuiltBorder;
import win.blade.common.utils.render.renderers.impl.BuiltRectangle;
import win.blade.common.utils.render.renderers.impl.BuiltText;

import java.awt.*;
import java.util.function.Consumer;

public class Slider extends ClickableWidget {
    private final int width;
    private final int height;
    private double value;
    private final Consumer<Double> onChange;
    private boolean dragging;

    public Slider(int x, int y, int width, int height, Text text, double initialValue, Consumer<Double> onChange) {
        super(x, y, width, height, text);
        this.width = width;
        this.height = height;
        this.value = Math.clamp(initialValue, 0.0, 1.0);
        this.onChange = onChange;
        this.dragging = false;
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();

        Color baseColor = new Color(20, 18, 27);
        Color borderColor = new Color(255, 255, 255, 15);
        Color textColor = this.active ? new Color(140, 136, 154, 255) : new Color(100, 100, 100, 255);
        Color filledColor = new Color(71, 47, 108, 150);

        BuiltRectangle background = Builder.rectangle()
                .size(new SizeState(this.width, this.height))
                .color(new QuadColorState(baseColor))
                .radius(new QuadRadiusState(10))
                .smoothness(1.0f)
                .build();
        background.render(matrix, this.getX(), this.getY());


        Color black  = new Color(20, 18, 27, 255);

        Color purple  = new Color(32, 22, 70, 255);

        int width = (int) (this.width * this.value);


            BuiltRectangle fill = Builder.rectangle()
                    .size(new SizeState(width, this.height))

                    .color(new QuadColorState(black,black,purple,purple))
                    .radius(new QuadRadiusState(10,10,10,10))
                    .smoothness(1.0f)
                    .build();
            fill.render(matrix, this.getX(), this.getY());


        BuiltBorder border = Builder.border()
                .size(new SizeState(this.width, this.height))
                .color(new QuadColorState(borderColor))
                .radius(new QuadRadiusState(10))
                .thickness(0.25f)
                .build();
        border.render(matrix, this.getX(), this.getY());

        String textToRender = this.getMessage().getString();
        float textSize = 7.5f;
        float textWidth = FontType.sf_regular.get().getWidth(textToRender, textSize);

        float textX = this.getX() + (this.width - textWidth) / 2.0f;
        float textY = this.getY() + (this.height / 2.0f) - 5;

        BuiltText render = Builder.text()
                .font(FontType.sf_regular.get())
                .text(textToRender)
                .color(textColor)
                .size(textSize)
                .thickness(0.05f)
                .build();
        render.render(matrix, textX, textY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.active && this.visible && button == 0 && this.isMouseOver(mouseX, mouseY)) {
            this.dragging = true;
            updateValue(mouseX);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (this.active && this.dragging && button == 0) {
            updateValue(mouseX);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            this.dragging = false;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return this.visible && mouseX >= (double)this.getX() && mouseY >= (double)this.getY() && mouseX < (double)(this.getX() + this.width) && mouseY < (double)(this.getY() + this.height);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        this.appendDefaultNarrations(builder);
    }

    public void updateValue(double mouseX) {
        double newValue = Math.clamp((mouseX - getX()) / (double) width, 0.0, 1.0);
        if (this.value != newValue) {
            this.value = newValue;
            if (onChange != null) {
                onChange.accept(this.value);
            }
        }
    }
}