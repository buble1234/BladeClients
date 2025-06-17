package win.blade.common.screen.impl;

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
import win.blade.common.utils.render.renderers.impl.BuiltRectangle;
import win.blade.common.screen.button.Button;
import win.blade.common.utils.minecraft.MinecraftInstance;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MainScreen extends Screen implements MinecraftInstance {
    private final List<Button> buttons = new ArrayList<>();

    public MainScreen() {
        super(Text.of(""));
    }

    @Override
    protected void init() {
        buttons.clear();
        int centerX = mc.getWindow().getWidth() / 2;
        int centerY = mc.getWindow().getHeight() / 2;

        buttons.add(new Button(centerX - 100, centerY - 48, 200, 20, Text.of("Singleplayer"), () -> mc.setScreen(new SelectWorldScreen(this)), textRenderer));
        buttons.add(new Button(centerX - 100, centerY - 23, 200, 20, Text.of("Multiplayer"), () -> mc.setScreen(new MultiplayerScreen(this)), textRenderer));
        //buttons.add(new Button(centerX - 100, centerY + 2, 200, 20, Text.of("Accounts"), () -> mc.setScreen(new AccountScreen()), textRenderer));
        buttons.add(new Button(centerX - 100, centerY + 27, 98, 20, Text.of("Options"), () -> mc.setScreen(new OptionsScreen(this, mc.options)), textRenderer));
        buttons.add(new Button(centerX + 2, centerY + 27, 98, 20, Text.of("Quit"), () -> MinecraftClient.getInstance().scheduleStop(), textRenderer));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();
        BuiltRectangle background = Builder.rectangle()
                .size(new SizeState(mc.getWindow().getWidth(), mc.getWindow().getHeight()))
                .color(new QuadColorState(new Color(19, 19, 19), new Color(14, 55, 131, 150), new Color(19, 19, 19), new Color(14, 55, 131, 150)))
                .radius(new QuadRadiusState(0f, 0f, 0f, 0f))
                .smoothness(1.0f)
                .build();
        background.render(matrix, 0, 0);

        for (Button button : buttons) {
            button.render(context, mouseX, mouseY);
        }

        context.drawCenteredTextWithShadow(textRenderer, "1.21.5", mc.getWindow().getWidth() / 2, mc.getWindow().getHeight() / 2 - 75, Color.WHITE.getRGB());
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            for (Button btn : buttons) {
                if (btn.isMouseOver((int) mouseX, (int) mouseY)) {
                    btn.onClick();
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}