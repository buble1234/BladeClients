package win.blade.common.gui.impl.screen.firstlaunch;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import win.blade.common.gui.button.NextButton;
import win.blade.common.gui.impl.screen.BaseScreen;
import win.blade.common.gui.button.LanguageButton;
import win.blade.common.utils.math.TimerUtil;
import win.blade.common.utils.math.animation.Animation;
import win.blade.common.utils.math.animation.Easing;
import win.blade.common.utils.other.TextAlign;
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.msdf.FontType;
import win.blade.common.utils.render.msdf.MsdfFont;

import java.awt.*;

public class SelectlanguageScreen extends BaseScreen {
    private final Runnable onClose;
    private final TimerUtil timer = new TimerUtil();
    private final Animation yOffsetAnimation = new Animation();
    private final Animation textAlpha = new Animation();
    private final Animation buttonsAlpha = new Animation();

    private int stage = 0;
    private LanguageButton ru;
    private LanguageButton eng;
    private NextButton nextButton;

    private LanguageButton selectedButton;

    public SelectlanguageScreen(Runnable onClose) {
        super(Text.translatable("accessibility.onboarding.screen.title"));
        this.onClose = onClose;
    }

    @Override
    protected void init() {
        yOffsetAnimation.set(0);
        textAlpha.set(0);
        buttonsAlpha.set(0);
        timer.reset();
        stage = 0;
        selectedButton = null;

        float gap = 8;

        float totalW = 151 * 2 + gap;
        float sX = (this.width - totalW) / 2f;
        float sY = (this.height / 2f) - (57 / 2f) + 30;

        ru = new LanguageButton((int)sX, (int)sY, 151, 57, "Русский язык", "RU", "textures/rus.png", () -> selectedButton = ru);
        eng = new LanguageButton((int)(sX + 151 + gap), (int)sY, 151, 57, "Английский язык", "ENG", "textures/us.png", () -> selectedButton = eng);

        float nextX = (this.width - 158) / 2f;
        float nextY = sY + 57 + 20;

        nextButton = new NextButton((int)nextX, (int)nextY, 158, 33, this::close);
    }

    @Override
    protected void renderContent(DrawContext context, int mouseX, int mouseY, float delta) {
        switch (stage) {
            case 0:
                textAlpha.run(1.0, 0.5, Easing.EASE_OUT_CUBIC);
                stage = 1;
                break;
            case 1:
                if (timer.hasReached(1500)) {
                    yOffsetAnimation.run(-80.0, 0.7, Easing.EASE_OUT_CUBIC);
                    buttonsAlpha.run(1.0, 0.7, Easing.EASE_OUT_CUBIC);
                    stage = 2;
                }
                break;
        }

        textAlpha.update();
        yOffsetAnimation.update();
        buttonsAlpha.update();

        ru.isSelected = (selectedButton == ru);
        eng.isSelected = (selectedButton == eng);
        nextButton.active = (selectedButton != null);

        MsdfFont font = FontType.sf_regular.get();
        var fz = 32f;

        float textX = this.width / 2f;
        float textY = (this.height / 2f) - (fz / 2f) + yOffsetAnimation.get();

        Builder.text()
                .font(font)
                .text("Выберите язык, чтобы\nпродолжить")
                .size(fz)
                .color(new Color(1f, 1f, 1f, textAlpha.get()))
                .align(TextAlign.CENTER)
                .thickness(0.05f)
                .build()
                .render(textX, textY);

        if (buttonsAlpha.get() > 0.01f) {
            ru.alpha = buttonsAlpha.get();
            eng.alpha = buttonsAlpha.get();
            nextButton.alpha = buttonsAlpha.get();

            ru.render(context, mouseX, mouseY, delta);
            eng.render(context, mouseX, mouseY, delta);
            nextButton.render(context, mouseX, mouseY, delta);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (ru.mouseClicked(mouseX, mouseY, button)) return true;
        if (eng.mouseClicked(mouseX, mouseY, button)) return true;
        if (nextButton.mouseClicked(mouseX, mouseY, button)) return true;
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void close() {
        if (selectedButton != null) {
            this.client.setScreen(new ByeScreen());
        }
    }

    @Override
    protected void renderFooter(DrawContext context, int screenWidth, int screenHeight) {
    }
}