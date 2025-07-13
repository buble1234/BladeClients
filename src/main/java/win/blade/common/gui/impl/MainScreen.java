package win.blade.common.gui.impl;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.text.Text;
import org.joml.Matrix4f;
import win.blade.common.gui.button.Button;
import win.blade.common.gui.impl.screen.BaseScreen;
import win.blade.common.gui.impl.screen.OptionsScreen;
import win.blade.common.gui.impl.screen.account.AccountScreen;
import win.blade.common.gui.impl.screen.multiplayer.MultiplayerScreen;
import win.blade.common.gui.impl.screen.singleplayer.SingleplayerScreen;
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.msdf.FontType;
import win.blade.common.utils.render.renderers.impl.BuiltText;

import java.awt.*;

public class MainScreen extends BaseScreen {

    public MainScreen() {
        super(Text.of(""));
    }

    AccountScreen accountScreen = new AccountScreen();
    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        this.addDrawableChild(new Button(centerX - 130, centerY - 68, 249, 32, Text.of("Singleplayer"), () -> mc.setScreen(new SingleplayerScreen())));
        this.addDrawableChild(new Button(centerX - 130, centerY - 32, 249, 32, Text.of("Multiplayer"), () -> mc.setScreen(new MultiplayerScreen())));
        this.addDrawableChild(new Button(centerX - 130, centerY + 4, 249, 32, Text.of("Accounts"), () -> mc.setScreen(accountScreen)));
        this.addDrawableChild(new Button(centerX - 130, centerY + 40, 121, 32, Text.of("Options"), () -> mc.setScreen(new OptionsScreen(this, mc.options))));
        this.addDrawableChild(new Button(centerX - 2, centerY + 40, 121, 32, Text.of("Quit"), () -> MinecraftClient.getInstance().scheduleStop()));
    }

    @Override
    protected void renderContent(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        float Twidth = FontType.sf_regular.get().getWidth("Blade Client", 16);

        BuiltText bladeIcon = Builder.text()
                .font(FontType.icon2.get())
                .text("a")
                .color(new Color(102, 60, 255, 255))
                .size(24)
                .thickness(0.05f)
                .build();

        float iX = centerX - (Twidth / 2.0f) - FontType.icon2.get().getWidth("a", 24) - 5;
        bladeIcon.render(matrix, iX, centerY - 130);


        BuiltText text = Builder.text()
                .font(FontType.sf_regular.get())
                .text("Blade Client")
                .color(Color.WHITE)
                .size(16)
                .thickness(0.05f)
                .build();
        text.render(matrix, centerX - Twidth / 2.0f + 5, centerY - 125);
    }


    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void close() {
        super.close();
    }
}