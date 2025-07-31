package win.blade.common.gui.impl.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import win.blade.common.gui.impl.screen.firstlaunch.FinishScreen;
import win.blade.common.utils.math.animation.Animation;
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.builders.states.SizeState;
import win.blade.common.utils.render.msdf.FontType;

import java.awt.*;

/**
 * Автор Ieo117
 * Дата создания: 31.07.2025, в 16:06:05
 */

public class ExitScreen extends BaseScreen{
    public boolean hasShown = false;
    private FinishScreen screen;
    Animation animation = new Animation();

    public ExitScreen() {
        super(Text.of("Hi hitler"));
        screen = new FinishScreen();
    }


    @Override
    public void init(){
        super.init();

        animation.run(1, 0.5);
    }

    @Override
    protected void renderContent(DrawContext context, int mouseX, int mouseY, float delta) {
        animation.update();


        if(animation.isFinished() && animation.get() > 0) {
            screen.width = this.width;
            screen.height = this.height;

            screen.render(context, mouseX, mouseY, delta);
        } else {
            var font = FontType.sf_regular.get();

            AbstractTexture iconTexture = MinecraftClient.getInstance()
                    .getTextureManager()
                    .getTexture(Identifier.of("blade", "textures/hello.png"));

            float iSize = 32f;
            float gap = 20f;
            float fontSize = 32f;

            String mainText = "До встреч!";

            float mainTW = font.getWidth(mainText, fontSize);

            float generalW = mainTW + gap + iSize;

            float mainTX = (this.width - generalW) / 2f;
            float mainTY = (this.height / 2f) - (fontSize / 2f);

            Builder.text()
                    .font(font)
                    .text(mainText)
                    .size(fontSize)
                    .color(new Color(1f, 1f, 1f, 1f))
                    .thickness(0.05f)
                    .build()
                    .render(mainTX, mainTY);

            float iconX = mainTX + mainTW + gap;
            float iconY = mainTY + (fontSize / 2f) - (iSize / 2f) + 2;

            Builder.texture()
                    .size(new SizeState(iSize, iSize))
                    .texture(0.0f, 0.0f, 1.0f, 1.0f, iconTexture)
                    .smoothness(3.0f)
                    .build()
                    .render(iconX, iconY);
        }
    }

    @Override
    public void close() {
        super.close();
        hasShown = true;
        MinecraftClient.getInstance().scheduleStop();
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}
