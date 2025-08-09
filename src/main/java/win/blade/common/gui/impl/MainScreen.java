package win.blade.common.gui.impl;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.joml.Matrix4f;
import win.blade.common.gui.button.Button;
import win.blade.common.gui.impl.screen.BaseScreen;
import win.blade.common.gui.impl.screen.account.AccountScreen;
import win.blade.common.gui.impl.screen.multiplayer.MultiplayerScreen;
import win.blade.common.gui.impl.screen.options.OptionsScreen;
import win.blade.common.gui.impl.screen.singleplayer.SingleplayerScreen;
import win.blade.common.utils.math.MathUtility; // Убедись, что импорт правильный
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.builders.states.QuadColorState;
import win.blade.common.utils.render.builders.states.QuadRadiusState;
import win.blade.common.utils.render.builders.states.SizeState;
import win.blade.common.utils.render.msdf.FontType;
import win.blade.common.utils.render.renderers.impl.BuiltText;
import win.blade.core.Manager;

import java.awt.*;

public class MainScreen extends BaseScreen {

    private float discordX, telegramX, websiteX, iconsY, iconSize;

    public MainScreen() {
        super(Text.of(""));
    }

    public AccountScreen accountScreen = new AccountScreen();
    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        this.addDrawableChild(new Button(centerX - 130, centerY - 68, 249, 32, Text.of("Singleplayer"), () -> mc.setScreen(new SingleplayerScreen())));
        this.addDrawableChild(new Button(centerX - 130, centerY - 32, 249, 32, Text.of("Multiplayer"), () -> mc.setScreen(new MultiplayerScreen())));
        this.addDrawableChild(new Button(centerX - 130, centerY + 4, 249, 32, Text.of("Alt Manager"), () -> mc.setScreen(accountScreen)));
        this.addDrawableChild(new Button(centerX - 130, centerY + 40, 121, 32, Text.of("Options"), () -> mc.setScreen(new OptionsScreen(this, mc.options))));
        this.addDrawableChild(new Button(centerX - 2, centerY + 40, 121, 32, Text.of("Quit"), () -> MinecraftClient.getInstance().scheduleStop()));

        this.iconSize = 10f;
        float gap = 22f;

        float totalWidth = (iconSize * 3) + (gap * 2);
        float startX = (this.width / 2f) - (totalWidth / 2f);

        this.iconsY = this.height - 55f;
        this.discordX = startX;
        this.telegramX = startX + iconSize + gap;
        this.websiteX = startX + 2 * (iconSize + gap);
    }

    @Override
    protected void renderContent(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        float iX = centerX - 115;

        Builder.texture()
                .size(new SizeState(180, 180))
                .svgTexture(Identifier.of("blade", "textures/svg/bladetitle.svg"))
                .build()
                .render(iX, centerY - 205);


        Color color = new Color(200, 200, 200);

        Builder.texture()
                .size(new SizeState(iconSize, iconSize))
                .svgTexture(Identifier.of("blade", "textures/svg/icons/discord.svg"))
                .color(new QuadColorState(color))
                .build().render(matrix, discordX, iconsY);

        Builder.texture()
                .size(new SizeState(iconSize, iconSize))
                .svgTexture(Identifier.of("blade", "textures/svg/icons/telegram.svg"))
                .color(new QuadColorState(color))
                .build().render(matrix, telegramX, iconsY);

        Builder.texture()
                .size(new SizeState(iconSize, iconSize))
                .svgTexture(Identifier.of("blade", "textures/svg/icons/website.svg"))
                .color(new QuadColorState(color))
                .build().render(matrix, websiteX, iconsY);
    }


    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (MathUtility.isHovered(mouseX, mouseY, discordX, iconsY, iconSize, iconSize)) {
            Util.getOperatingSystem().open("https://discord.gg/YOUR_INVITE");
            return true;
        }

        if (MathUtility.isHovered(mouseX, mouseY, telegramX, iconsY, iconSize, iconSize)) {
            Util.getOperatingSystem().open("https://t.me/YOUR_CHANNEL");
            return true;
        }

        if (MathUtility.isHovered(mouseX, mouseY, websiteX, iconsY, iconSize, iconSize)) {
            Util.getOperatingSystem().open("https://your.website.com");
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void close() {
        super.close();
    }
}