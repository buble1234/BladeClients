package win.blade.common.gui.impl.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.builders.states.QuadColorState;
import win.blade.common.utils.render.builders.states.QuadRadiusState;
import win.blade.common.utils.render.builders.states.SizeState;
import win.blade.common.utils.render.msdf.FontType;

import java.awt.Color;

public abstract class BaseWindowScreen extends BaseScreen {

    protected final int windowWidth;
    protected final int windowHeight;

    protected BaseWindowScreen(Text title, int windowWidth, int windowHeight) {
        super(title);
        this.windowWidth = windowWidth;
        this.windowHeight = windowHeight;
    }

    @Override
    protected void renderContent(DrawContext context, int mouseX, int mouseY, float delta) {
        int windowX = (this.width - this.windowWidth) / 2;
        int windowY = (this.height - this.windowHeight) / 2;

        Builder.rectangle()
                .size(new SizeState(windowWidth, windowHeight))
                .color(new QuadColorState(25, 22, 32, 220))
                .radius(new QuadRadiusState(12))
                .build()
                .render(context.getMatrices().peek().getPositionMatrix(), windowX, windowY);

        Builder.rectangle()
                .size(new SizeState(306, 122))
                .color(new QuadColorState(new Color(23, 20, 38, 255), new Color(20, 18, 27, 255), new Color(17, 15, 23, 255), new Color(20, 18, 27, 255)))
                .radius(new QuadRadiusState(10))
                .build()
                .render(windowX, windowY);

        Builder.text()
                .font(FontType.sf_regular.get())
                .text(this.getTitle().getString())
                .color(Color.WHITE)
                .size(10f)
                .thickness(0.05f)
                .build()
                .render(windowX + 20, windowY + 20);
    }
}