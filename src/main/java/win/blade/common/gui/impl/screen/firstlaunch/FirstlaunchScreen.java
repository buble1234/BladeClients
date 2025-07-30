package win.blade.common.gui.impl.screen.firstlaunch;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import win.blade.common.gui.impl.screen.BaseScreen;
import win.blade.common.utils.math.TimerUtil;
import win.blade.common.utils.math.animation.Animation;
import win.blade.common.utils.math.animation.Easing;
import win.blade.common.utils.other.TextAlign;
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.builders.states.SizeState;
import win.blade.common.utils.render.msdf.FontType;
import win.blade.common.utils.render.msdf.MsdfFont;

import java.awt.Color;
import java.time.LocalDateTime;

public class FirstlaunchScreen extends BaseScreen {

    private final Runnable onClose;
    private final TimerUtil timer = new TimerUtil();

    private final Animation yOffset = new Animation();
    private final Animation mAlpha = new Animation();
    private final Animation subAlpha = new Animation();

    private int stage = 0;

    public FirstlaunchScreen(Runnable onClose) {
        super(Text.translatable("accessibility.onboarding.screen.title"));
        this.onClose = onClose;
    }

    @Override
    protected void init() {
        yOffset.set(0);
        mAlpha.set(0);
        subAlpha.set(0);
        timer.reset();
        stage = 0;
    }

    @Override
    protected void renderContent(DrawContext context, int mouseX, int mouseY, float delta) {
        switch (stage) {
            case 0:
                if (timer.hasReached(1000)) {
                    mAlpha.run(1.0, 0.7, Easing.EASE_OUT_CUBIC);
                    stage = 1;
                }
                break;
            case 1:
                if (mAlpha.isFinished()) {
                    timer.reset();
                    stage = 2;
                }
                break;
            case 2:
                if (timer.hasReached(2000)) {
                    yOffset.run(-20.0, 0.7, Easing.EASE_OUT_CUBIC);
                    subAlpha.run(1.0, 0.7, Easing.EASE_OUT_CUBIC);
                    stage = 3;
                }
                break;
            case 3:
                if (subAlpha.isFinished()) {
                    timer.reset();
                    stage = 4;
                }
                break;
            case 4:
                if (timer.hasReached(2500)) {
                    mAlpha.run(0.0, 0.5, Easing.EASE_IN_CUBIC);
                    subAlpha.run(0.0, 0.5, Easing.EASE_IN_CUBIC);
                    stage = 5;
                }
                break;
            case 5:
                if (mAlpha.isFinished()) {
                    this.client.setScreen(new SelectlanguageScreen(this.onClose));
                    stage = 6;
                    return;
                }
                break;
        }

        mAlpha.update();
        subAlpha.update();
        yOffset.update();

        MsdfFont font = FontType.sf_regular.get();

        if (mAlpha.get() > 0.01f) {
            String mainTextStr =  getGreeting() + ", " + this.client.getSession().getUsername();
            float fz = 32f;

            float mainTW = font.getWidth(mainTextStr, fz);

            AbstractTexture iconTexture = MinecraftClient.getInstance()
                    .getTextureManager()
                    .getTexture(Identifier.of("blade", "textures/hello.png"));
            float iSize = 32f;
            float gap = 25f;

            float generalW = mainTW + gap + iSize;

            float mainTX = (this.width - generalW) / 2f;
            float mainTY = (this.height / 2f) - (fz / 2f) + yOffset.get();

            Builder.text()
                    .font(font)
                    .text(mainTextStr)
                    .size(fz)
                    .color(new Color(1f, 1f, 1f, mAlpha.get()))
                    .thickness(0.05f)
                    .build()
                    .render(mainTX, mainTY);

            float iconX = mainTX + mainTW + gap;
            float iconY = mainTY + (fz / 2f) - (iSize / 2f) + 2;

            Builder.texture()
                    .size(new SizeState(iSize, iSize))
                    .texture(0.0f, 0.0f, 1.0f, 1.0f, iconTexture)
                    .smoothness(3.0f)
                    .build()
                    .render(iconX, iconY);

            if (subAlpha.get() > 0.01f) {
                float subTSize = 14f;
                float subX = this.width / 2f;
                float subY = mainTY + fz + 8;

                Builder.text()
                        .font(font)
                        .text("Не бойся мечтать, ведь с Blade Client все мечты\n становятся ближе!")
                        .size(subTSize)
                        .color(new Color(0.65f, 0.65f, 0.65f, subAlpha.get()))
                        .align(TextAlign.CENTER)
                        .thickness(0.05f)
                        .build()
                        .render(subX, subY);
            }
        }
    }

    @SuppressWarnings("all")
    public String getGreeting(){
        LocalDateTime time = LocalDateTime.now();

        StringBuilder builder = new StringBuilder();

        if(time.getHour() > 22 || time.getHour() < 7){
            builder.append("Доброй ночи");
        } else if (time.getHour() >= 7 && time.getHour() <= 12) {
            builder.append("Доброе утро");
        } else if (time.getHour() >= 13 && time.getHour() <= 16) {
            builder.append("Добрый день");
        } else if(time.getHour() >= 17 && time.getHour() <= 22){
            builder.append("Добрый вечер");
        }

        return builder.toString();
    }

    @Override
    public void close() {
        this.onClose.run();
    }

    @Override
    protected void renderFooter(DrawContext context, int screenWidth, int screenHeight) {
    }
}
