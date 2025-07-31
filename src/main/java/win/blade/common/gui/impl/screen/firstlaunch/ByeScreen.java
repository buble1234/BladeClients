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
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.builders.states.SizeState;
import win.blade.common.utils.render.msdf.FontType;
import win.blade.common.utils.render.msdf.MsdfFont;

import java.awt.Color;

public class ByeScreen extends BaseScreen {
    private final Animation textAlpha = new Animation();
    private final TimerUtil timer = new TimerUtil();
    private boolean animationStarted = false;
    public boolean russian = true;

    public ByeScreen() {
        super(Text.of(""));
    }

    public ByeScreen(boolean isRussian){
        this();
        russian = isRussian;
    }

    @Override
    protected void init() {
        super.init();
        textAlpha.set(0);
        animationStarted = false;
        timer.reset();
    }

    @Override
    protected void renderContent(DrawContext context, int mouseX, int mouseY, float delta) {
        if (!animationStarted) {
            textAlpha.run(1.0, 1.0, Easing.EASE_OUT_CUBIC);
            animationStarted = true;
        }

        textAlpha.update();

        if (animationStarted && textAlpha.isFinished() && timer.hasReached(1200)) {
            this.client.setScreen(new FinishScreen());
            return;
        }

        float currentAlpha = textAlpha.get();
        if (currentAlpha < 0.01f) return;

        MsdfFont font = FontType.sf_regular.get();
        float fz = 32;

        float iSize = 32;
        float gap = 20f;


        String text = russian ? "Приятной игры, боец" : "Have a nice game, fighter";

        float textWidth = font.getWidth(text, fz);
        float totalWidth = textWidth + gap + iSize;

        float startX = (this.width - totalWidth) / 2f;
        float textY = (this.height - fz) / 2f;

        Builder.text()
                .font(font)
                .text(text)
                .size(fz)
                .color(new Color(1f, 1f, 1f, currentAlpha))
                .thickness(0.05f)
                .build()
                .render(startX, textY);


        AbstractTexture iconTexture = MinecraftClient.getInstance().getTextureManager().getTexture(Identifier.of("blade", "textures/streng.png"));
        float iconX = startX + textWidth + gap;
        float iconY = textY + (fz / 2f) - (iSize / 2f) + 3;

        Builder.texture()
                .size(new SizeState(iSize, iSize))
                .texture(0.0f, 0.0f, 1.0f, 1.0f, iconTexture)
                .smoothness(3.0f)
                .build()
                .render(iconX, iconY);
    }

    @Override
    protected void renderFooter(DrawContext context, int screenWidth, int screenHeight) {
    }
}