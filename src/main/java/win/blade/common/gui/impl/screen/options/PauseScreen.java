package win.blade.common.gui.impl.screen.options;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.*;
import net.minecraft.client.gui.screen.advancement.AdvancementsScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.multiplayer.SocialInteractionsScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.realms.gui.screen.RealmsMainScreen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import win.blade.common.gui.button.Button;
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.builders.states.SizeState;

import java.awt.*;

/**
 * Автор: NoCap
 * Дата создания: 31.07.2025
 */
public class PauseScreen extends Screen {

    private final Screen parent;
    private int buttonsTopY;

    public PauseScreen(Screen parent) {
        super(Text.translatable("menu.game"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        final int buttonWidth = 498 / 2 - 3;
//        249 /2;
        final int buttonHeight = 65 / 2;
        final int buttonPadding = 4;
        final int halfButtonWidth = 243 / 2;

        final int numberOfRows = 4;
        final int totalButtonsHeight = (numberOfRows * buttonHeight) + ((numberOfRows - 1) * buttonPadding);

        this.buttonsTopY = (this.height / 2) - (totalButtonsHeight / 2) + 15;
        int currentY = this.buttonsTopY;

        final int centerX = this.width / 2 - 3;
        final int rowHeight = buttonHeight + buttonPadding;

        this.addDrawableChild(new Button(centerX - halfButtonWidth, currentY, buttonWidth, buttonHeight, Text.translatable("menu.returnToGame"), () -> {
            this.client.setScreen(null);
            this.client.mouse.lockCursor();
        }));

        currentY += rowHeight;

        this.addDrawableChild(new Button(centerX - halfButtonWidth, currentY, halfButtonWidth - 2, buttonHeight, Text.translatable("gui.advancements"), () -> {
            this.client.setScreen(new AdvancementsScreen(this.client.player.networkHandler.getAdvancementHandler(), this));
        }));
        this.addDrawableChild(new Button(centerX + 6, currentY, halfButtonWidth - 2, buttonHeight, Text.translatable("gui.stats"), () -> {
            this.client.setScreen(new StatsScreen(this, this.client.player.getStatHandler()));
        }));

        currentY += rowHeight;

        this.addDrawableChild(new Button(centerX - halfButtonWidth, currentY, halfButtonWidth - 2, buttonHeight, Text.translatable("menu.options"), () -> {
            this.client.setScreen(new InGameOptionsScreen(this));
        }));

        if (this.client.isIntegratedServerRunning() && !this.client.getServer().isRemote()) {
            this.addDrawableChild(new Button(centerX + 6, currentY, halfButtonWidth - 2, buttonHeight, Text.translatable("menu.shareToLan"), () -> {
                this.client.setScreen(new OpenToLanScreen(this));
            }));
        } else {
            this.addDrawableChild(new Button(centerX + 6, currentY, halfButtonWidth - 2, buttonHeight, Text.translatable("menu.playerReporting"), () -> {
                this.client.setScreen(new SocialInteractionsScreen(this));
            }));
        }

        currentY += rowHeight;

        this.addDrawableChild(new Button(centerX - halfButtonWidth, currentY, buttonWidth, buttonHeight, Text.translatable("menu.returnToMenu"), this::disconnect));
    }

    private void disconnect() {
        boolean bl = this.client.isInSingleplayer();
        ServerInfo serverInfo = this.client.getCurrentServerEntry();
        this.client.world.disconnect();
        if (bl) {
            this.client.disconnect(new MessageScreen(Text.translatable("menu.savingLevel")));
        } else {
            this.client.disconnect();
        }

        TitleScreen titleScreen = new TitleScreen();
        if (bl) {
            this.client.setScreen(titleScreen);
        } else if (serverInfo != null && serverInfo.isRealm()) {
            this.client.setScreen(new RealmsMainScreen(titleScreen));
        } else {
            this.client.setScreen(new MultiplayerScreen(titleScreen));
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        final int tileWidth = 220 * 2;
        final int tileHeight = 48 * 2;

        var title = Builder.texture()
                .size(new SizeState(tileWidth, tileHeight))
                .svgTexture(Identifier.of("blade", "textures/bladetitle.svg"))
                .build();

        float x = (this.width - tileWidth) / 2.0f - 19.5f;
        float y = this.buttonsTopY - tileHeight + 18;

        title.render(x, y);
    }

    @Override
    public void close() {
        this.client.setScreen(this.parent);
    }
}