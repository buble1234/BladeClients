package win.blade.common.gui.impl.screen.firstlaunch;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import win.blade.common.gui.impl.MainScreen;
import win.blade.common.gui.impl.screen.BaseScreen;
import win.blade.common.utils.math.TimerUtil;
import win.blade.common.utils.math.animation.Animation;
import win.blade.common.utils.math.animation.Easing;
import win.blade.common.utils.other.TextAlign;
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.msdf.FontType;
import win.blade.common.utils.render.msdf.MsdfFont;

import java.awt.Color;

public class FinishScreen extends BaseScreen {

    private final Animation alphaAnimation = new Animation();
    private final TimerUtil timer = new TimerUtil();
    private int stage = 0;

    public FinishScreen() {
        super(Text.of(""));
    }

    @Override
    protected void init() {
        super.init();
        alphaAnimation.set(0);
        timer.reset();
        stage = 0;
    }

    @Override
    protected void renderContent(DrawContext context, int mouseX, int mouseY, float delta) {
        alphaAnimation.update();

        switch (stage) {
            case 0:
                alphaAnimation.run(1.0, 1.5, Easing.EASE_OUT_CUBIC);
                stage = 1;
                break;
            case 1:
                if (alphaAnimation.isFinished()) {
                    timer.reset();
                    stage = 2;
                }
                break;
            case 2:
                if (timer.hasReached(1200)) {
                    alphaAnimation.run(0.0, 1.0, Easing.EASE_IN_CUBIC);
                    stage = 3;
                }
                break;
            case 3:
                if (alphaAnimation.isFinished()) {
                    close();
                    stage = 4;
                }
                break;
        }


        float currentAlpha = alphaAnimation.get();
        if (currentAlpha < 0.01f) return;

        MsdfFont font = FontType.sf_regular.get();
        float fz = 48f;

        float textX = this.width / 2f;
        float textY = (this.height / 2f) - (fz / 2f);

        Builder.text()
                .font(font)
                .text("Blade client")
                .size(fz)
                .color(new Color(1f, 1f, 1f, currentAlpha))
                .align(TextAlign.CENTER)
                .thickness(0.05f)
                .build()
                .render(textX, textY);

        Builder.text()
                .font(FontType.icon2.get())
                .text("a")
                .size(fz)
                .color(new Color(102, 60, 255, (int)(currentAlpha * 255)))
                .align(TextAlign.CENTER)
                .thickness(0.05f)
                .build()
                .render(textX - 140, textY);
    }

    @Override
    protected void renderFooter(DrawContext context, int screenWidth, int screenHeight) {
    }
}