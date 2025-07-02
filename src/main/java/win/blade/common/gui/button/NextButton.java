package win.blade.common.gui.button;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import win.blade.common.utils.other.TextAlign;
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.builders.states.QuadColorState;
import win.blade.common.utils.render.builders.states.QuadRadiusState;
import win.blade.common.utils.render.builders.states.SizeState;
import win.blade.common.utils.render.msdf.FontType;
import win.blade.common.utils.render.msdf.MsdfFont;

import java.awt.Color;

public class NextButton extends ClickableWidget {
    private final Runnable onClick;
    private final MsdfFont fontRegular = FontType.sf_regular.get();
    public float alpha = 1.0f;

    public NextButton(int x, int y, int width, int height, Runnable onClick) {
        super(x, y, width, height, Text.of("Продолжить"));
        this.onClick = onClick;
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        if (alpha < 0.01f) return;

        Color backgroundColor = new Color(102, 60, 255, (int)(255 * alpha));

        Builder.rectangle()
                .size(new SizeState(getWidth(), getHeight()))
                .color(new QuadColorState(backgroundColor))
                .radius(new QuadRadiusState(5))
                .smoothness(1)
                .build()
                .render(getX(), getY());
        

        Builder.text()
                .font(fontRegular)
                .text(getMessage().getString())
                .size(9)
                .color(new Color(1,1,1,alpha))
                .thickness(0.05f)
                .align(TextAlign.CENTER)
                .build()
                .render(getX() + getWidth() / 2f, getY() + (getHeight() - 9) / 2f);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (this.onClick != null && this.active) {
            this.onClick.run();
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.active && this.visible && this.isMouseOver(mouseX, mouseY) && button == 0) {
            this.onClick(mouseX, mouseY);
            return true;
        }
        return false;
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        this.appendDefaultNarrations(builder);
    }
}