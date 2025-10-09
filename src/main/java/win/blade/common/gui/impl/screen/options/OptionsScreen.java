package win.blade.common.gui.impl.screen.options;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.*;
import net.minecraft.client.gui.screen.pack.PackScreen;
import net.minecraft.client.option.GameOptions;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.text.Text;
import org.joml.Matrix4f;
import win.blade.common.gui.button.Button;
import win.blade.common.gui.button.Slider;
import win.blade.common.gui.impl.screen.BaseScreen;
import win.blade.common.gui.impl.screen.options.resourcepack.ResourcePackScreen;
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.msdf.FontType;
import win.blade.common.utils.render.renderers.impl.BuiltText;
import java.awt.*;

/**
 * Автор: NoCap
 * Дата создания: 01.08.2025
 */
public class OptionsScreen extends BaseScreen {

    private final Screen parent;
    private final GameOptions gameOptions;

    public OptionsScreen(Screen parent, GameOptions options) {
        super(Text.translatable("options.title"));
        this.parent = parent;
        this.gameOptions = options;
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;


        int topY = 60;
        int buttonsTopY = topY + 30;
        int buttonWidth = 150;
        int buttonHeight = 32;
        int rowH = buttonHeight + 10;

        int col1X = centerX - buttonWidth - 5 / 2;
        int col2X = centerX + 5 / 2;

        int row = 0;
        Slider fovSlider = new Slider(
                col1X, buttonsTopY + row * rowH, buttonWidth, buttonHeight,
                Text.translatable("options.fov"),
                (this.gameOptions.getFov().getValue() - 30) / 80.0,
                value -> this.gameOptions.getFov().setValue((int) (value * 80 + 30))
        );
        this.addDrawableChild(fovSlider);
        this.addDrawableChild(new Button(col2X, buttonsTopY + row * rowH, buttonWidth, buttonHeight, Text.translatable("options.accessibility"), () -> this.client.setScreen(new AccessibilityOptionsScreen(this, this.gameOptions, true))));
        row++;

        this.addDrawableChild(new Button(col1X, buttonsTopY + row * rowH, buttonWidth, buttonHeight, Text.translatable("options.skinCustomization"), () -> this.client.setScreen(new SkinOptionsScreen(this, this.gameOptions))));
        this.addDrawableChild(new Button(col2X, buttonsTopY + row * rowH, buttonWidth, buttonHeight, Text.translatable("options.sounds"), () -> this.client.setScreen(new SoundOptionsScreen(this, this.gameOptions, true))));
        row++;

        this.addDrawableChild(new Button(col1X, buttonsTopY + row * rowH, buttonWidth, buttonHeight, Text.translatable("options.video"), () -> this.client.setScreen(new VideoOptionsScreen(this, this.gameOptions, true))));
        this.addDrawableChild(new Button(col2X, buttonsTopY + row * rowH, buttonWidth, buttonHeight, Text.translatable("options.controls"), () -> this.client.setScreen(new ControlsOptionsScreen(this, this.gameOptions))));
        row++;

        this.addDrawableChild(new Button(col1X, buttonsTopY + row * rowH, buttonWidth, buttonHeight, Text.translatable("options.language"), () -> this.client.setScreen(new LanguageOptionsScreen(this, this.gameOptions, this.client.getLanguageManager()))));
        this.addDrawableChild(new Button(col2X, buttonsTopY + row * rowH, buttonWidth, buttonHeight, Text.translatable("options.chat"), () -> this.client.setScreen(new ChatOptionsScreen(this, this.gameOptions, true))));
        row++;

        this.addDrawableChild(new Button(col1X, buttonsTopY + row * rowH, buttonWidth, buttonHeight, Text.translatable("options.resourcepack"),
        () -> this.client.setScreen(new ResourcePackScreen(
                this,
                this.client.getResourcePackManager(),
                this::refreshResourcePacks,
                this.client.getResourcePackDir(),
                Text.translatable("resourcePack.title"), true)
        )));
        this.addDrawableChild(new Button(col2X, buttonsTopY + row * rowH, buttonWidth, buttonHeight, Text.of("Special features"), () -> {}));
        row++;

        this.addDrawableChild(new Button(centerX - buttonWidth / 2, buttonsTopY + row * rowH + 15, buttonWidth, buttonHeight, Text.translatable("gui.done"), () -> this.client.setScreen(this.parent)));
    }

    @Override
    protected void renderContent(DrawContext context, int mouseX, int mouseY, float delta) {
        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();

        String TString = Text.translatable("options.title").getString();
        float Twidth = FontType.sf_regular.get().getWidth(TString, 16);
        BuiltText titleRender = Builder.text()
                .font(FontType.sf_regular.get())
                .text(TString)
                .color(Color.WHITE)
                .size(16)
                .thickness(0.05f)
                .build();

        titleRender.render(matrix, this.width / 2.0f - Twidth / 2.0f, 40);

    }


    private void refreshResourcePacks(ResourcePackManager m) {
        this.client.options.refreshResourcePacks(m);
        this.client.setScreen(this);
    }


    @Override
    public void close() {
        this.client.setScreen(this.parent);
    }
}