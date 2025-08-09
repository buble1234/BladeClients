package win.blade.common.gui.button;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import org.joml.Matrix4f;
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.builders.states.QuadColorState;
import win.blade.common.utils.render.builders.states.QuadRadiusState;
import win.blade.common.utils.render.builders.states.SizeState;
import win.blade.common.utils.render.msdf.FontType;
import win.blade.common.utils.render.msdf.MsdfFont;
import win.blade.common.utils.render.renderers.impl.BuiltBorder;
import win.blade.common.utils.render.renderers.impl.BuiltRectangle;
import win.blade.common.utils.render.renderers.impl.BuiltText;

import java.awt.*;

public class Button extends ClickableWidget {
    private final Runnable onClick;

    private RenderAction onRender = (context, mouseX, mouseY, delta) -> {
        Color black = new Color(20, 18, 27);

        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();

        BuiltRectangle rectangle = Builder.rectangle()
                .size(new SizeState(getWidth(), getHeight()))
                .color(new QuadColorState(black))
                .radius(new QuadRadiusState(10, 10, 10, 10))
                .smoothness(1.0f)
                .build();
        rectangle.render(matrix, getX(), getY());

        BuiltBorder border = Builder.border()
                .size(new SizeState(getWidth(), getHeight()))
                .color(new QuadColorState(new Color(255, 255, 255, 15)))
                .radius(new QuadRadiusState(10))
                .thickness(0.25f)
                .build();
        border.render(matrix, getX(), getY());

        String textToRender = getMessage().getString();
        float textWidth = FontType.sf_regular.get().getWidth(textToRender, 7.5f);

        float textX = getX() + (getWidth() - textWidth) / 2.0f;
        float textY = getY() + getHeight() / 2.0f - 5;

        BuiltText render = Builder.text()
                .font(FontType.sf_regular.get())
                .text(textToRender)
                .color(this.active ? new Color(140, 136, 154, 255) : new Color(100,100,100,255))
                .size(7.5f)
                .thickness(0.05f)
                .build();
        render.render(matrix, textX, textY);
    };

    public Button(int x, int y, int width, int height, Text text, Runnable onClick) {
        super(x, y, width, height, text);
        this.onClick = onClick;
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        onRender.render(context, mouseX, mouseY, delta);
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
            this.playDownSound(MinecraftClient.getInstance().getSoundManager());
            this.onClick(mouseX, mouseY);
            return true;
        }
        return false;
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        this.appendDefaultNarrations(builder);
    }

    public Button addRender(boolean copyOrig, RenderAction action){
       var orig = onRender;

        this.onRender = ((context, mouseX, mouseY, delta) -> {
            if(copyOrig){
                orig.render(context, mouseX, mouseY, delta);
            }

            action.render(context, mouseX, mouseY, delta);
        });

        return this;
    }

}