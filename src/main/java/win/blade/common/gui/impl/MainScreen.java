package win.blade.common.gui.impl;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.text.Text;
import org.joml.Matrix4f;
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.builders.states.QuadColorState;
import win.blade.common.utils.render.builders.states.QuadRadiusState;
import win.blade.common.utils.render.builders.states.SizeState;
import win.blade.common.utils.render.msdf.FontType;
import win.blade.common.utils.render.renderers.impl.BuiltRectangle;
import win.blade.common.gui.button.Button;
import win.blade.common.utils.minecraft.MinecraftInstance;

import java.awt.*;

public class MainScreen extends Screen implements MinecraftInstance {
    private Button singleplayerButton;
    private Button multiplayerButton;
    private Button accountManagerButton;
    private Button optionsButton;
    private Button quitButton;

    public MainScreen() {
        super(Text.of(""));
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        singleplayerButton = new Button(centerX - 100, centerY - 48, 200, 20, Text.of("Singleplayer"), () -> mc.setScreen(new SelectWorldScreen(this)), FontType.biko.get(), 15);
        multiplayerButton = new Button(centerX - 100, centerY - 23, 200, 20, Text.of("Multiplayer"), () -> mc.setScreen(new MultiplayerScreen(this)), FontType.biko.get(), 15);
        accountManagerButton = new Button(centerX - 100, centerY + 2, 200, 20, Text.of("Accounts"), () -> mc.setScreen(new AccountScreen()), FontType.biko.get(), 15);
        optionsButton = new Button(centerX - 100, centerY + 27, 98, 20, Text.of("Options"), () -> mc.setScreen(new OptionsScreen(this, mc.options)), FontType.biko.get(), 15);
        quitButton = new Button(centerX + 2, centerY + 27, 98, 20, Text.of("Quit"), () -> MinecraftClient.getInstance().scheduleStop(), FontType.biko.get(), 15);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();
        BuiltRectangle background = Builder.rectangle()
                .size(new SizeState(this.width, this.height))
                .color(new QuadColorState(new Color(19, 19, 19), new Color(14, 55, 131, 150), new Color(19, 19, 19), new Color(14, 55, 131, 150)))
                .radius(new QuadRadiusState(0f, 0f, 0f, 0f))
                .smoothness(1.0f)
                .build();
        background.render(matrix, 0, 0);

        singleplayerButton.render(context, mouseX, mouseY);
        multiplayerButton.render(context, mouseX, mouseY);
        accountManagerButton.render(context, mouseX, mouseY);
        optionsButton.render(context, mouseX, mouseY);
        quitButton.render(context, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            if (singleplayerButton.isMouseOver((int) mouseX, (int) mouseY)) {
                singleplayerButton.onClick();
                return true;
            }
            if (multiplayerButton.isMouseOver((int) mouseX, (int) mouseY)) {
                multiplayerButton.onClick();
                return true;
            }

            if (accountManagerButton.isMouseOver((int) mouseX, (int) mouseY)) {
                accountManagerButton.onClick();
                return true;
            }
            if (optionsButton.isMouseOver((int) mouseX, (int) mouseY)) {
                optionsButton.onClick();
                return true;
            }
            if (quitButton.isMouseOver((int) mouseX, (int) mouseY)) {
                quitButton.onClick();
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}