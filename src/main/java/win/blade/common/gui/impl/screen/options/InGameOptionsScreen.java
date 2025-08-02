package win.blade.common.gui.impl.screen.options;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.pack.PackScreen;
import net.minecraft.client.gui.screen.option.*;
import net.minecraft.network.packet.c2s.play.UpdateDifficultyC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateDifficultyLockC2SPacket;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.Difficulty;
import org.joml.Matrix4f;
import win.blade.common.gui.button.Button;
import win.blade.common.gui.button.Slider;
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.builders.states.SizeState;
import win.blade.common.utils.render.msdf.FontType;
import win.blade.common.utils.render.msdf.MsdfFont;

import java.awt.Color;

/**
 * Автор: NoCap
 * Дата создания: 01.08.2025
 */
public class InGameOptionsScreen extends Screen {

    private final Screen parent;
    private Slider fovSlider;
    private Button difficultyButton;
    private Button lockButton;

    private static final Difficulty[] DIFFICULTIES = Difficulty.values();

    public InGameOptionsScreen(Screen parent) {
        super(Text.translatable("options.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        int topY = 60;
        int buttonsTopY = topY + 30;
        int buttonWidth = 150;
        int buttonHeight = 32;

        int initialRowGap = 20;
        int subsequentRowGap = 4;

        int centerX = this.width / 2;
        int col1X = centerX - buttonWidth - 5 / 2;
        int col2X = centerX + 5 / 2;

        int currentY = buttonsTopY;

        boolean showDifficultyOptions = this.client.world != null && this.client.isIntegratedServerRunning() && !this.client.world.getLevelProperties().isHardcore();
        boolean isDifficultyLocked = showDifficultyOptions && this.client.world.getLevelProperties().isDifficultyLocked();

        double initialFov = (this.client.options.getFov().getValue() - 30.0) / 80.0;
        this.fovSlider = new Slider(col1X, currentY, buttonWidth, buttonHeight, getFovText(), initialFov, this::onFovChange);
        this.addDrawableChild(this.fovSlider);

        if (showDifficultyOptions) {
            int columnPadding = 4;
            int lockButtonWidth = buttonHeight;
            int difficultyButtonWidth = buttonWidth - lockButtonWidth - columnPadding;

            this.difficultyButton = new Button(col2X, currentY, difficultyButtonWidth, buttonHeight, Text.empty(), this::cycleDifficulty) {
                @Override
                public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
                    super.renderWidget(context, mouseX, mouseY, delta);
                    Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();
                    MsdfFont font = FontType.sf_regular.get();
                    float fontSize = 7.5f;

                    String label = Text.translatable("options.difficulty").getString();
                    String slash = " / ";
                    Difficulty currentDifficulty = client.world.getDifficulty();
                    String value = currentDifficulty.getTranslatableName().getString();
                    Color valueColor = difficultyColor(currentDifficulty);
                    Color labelColor = new Color(140, 136, 154, 255);

                    float totalWidth = font.getWidth(label, fontSize) + font.getWidth(slash, fontSize) + font.getWidth(value, fontSize);

                    float currentX = this.getX() + (this.width - totalWidth) / 2.0f;
                    float textY = this.getY() + (this.height - fontSize) / 2.0f;

                    Builder.text().font(font).text(label).color(labelColor).size(fontSize).thickness(0.05f).build().render(matrix, currentX, textY);

                    currentX += font.getWidth(label, fontSize);

                    Builder.text().font(font).text(slash).color(labelColor).size(fontSize).thickness(0.05f).build().render(matrix, currentX, textY);

                    currentX += font.getWidth(slash, fontSize);

                    Builder.text().font(font).text(value).color(valueColor).size(fontSize).thickness(0.05f).build().render(matrix, currentX, textY);
                }
            };
            this.difficultyButton.active = !isDifficultyLocked;
            this.addDrawableChild(this.difficultyButton);

            this.lockButton = new Button(col2X + difficultyButtonWidth + columnPadding, currentY, lockButtonWidth, buttonHeight, Text.empty(), this::lockDifficulty) {
                @Override
                public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
                    super.renderWidget(context, mouseX, mouseY, delta);

                    boolean isLocked = client.world != null && client.world.getLevelProperties().isDifficultyLocked();
                    Identifier iconIdentifier = Identifier.of("blade", isLocked ? "textures/lock.svg" : "textures/unlock.svg");

                    float iconSize = 16;
                    float iconX = this.getX() + (this.width - iconSize) / 2.0f;
                    float iconY = this.getY() + (this.height - iconSize) / 2.0f;

                    Builder.texture()
                            .size(new SizeState(iconSize, iconSize))
                            .svgTexture(iconIdentifier)
                            .build()
                            .render(iconX, iconY);
                }
            };
            this.lockButton.active = !isDifficultyLocked;
            this.addDrawableChild(this.lockButton);
        } else {
            this.addDrawableChild(new Button(col2X, currentY, buttonWidth, buttonHeight, Text.translatable("options.accessibility"), () -> this.client.setScreen(new AccessibilityOptionsScreen(this, this.client.options))));
        }

        currentY += buttonHeight + initialRowGap;

        this.addDrawableChild(new Button(col1X, currentY, buttonWidth, buttonHeight, Text.translatable("options.skinCustomization"), () -> this.client.setScreen(new SkinOptionsScreen(this, this.client.options))));
        this.addDrawableChild(new Button(col2X, currentY, buttonWidth, buttonHeight, Text.translatable("options.sounds"), () -> this.client.setScreen(new SoundOptionsScreen(this, this.client.options))));

        currentY += buttonHeight + subsequentRowGap;

        this.addDrawableChild(new Button(col1X, currentY, buttonWidth, buttonHeight, Text.translatable("options.video"), () -> this.client.setScreen(new VideoOptionsScreen(this, this.client.options))));
        this.addDrawableChild(new Button(col2X, currentY, buttonWidth, buttonHeight, Text.translatable("options.controls"), () -> this.client.setScreen(new ControlsOptionsScreen(this, this.client.options))));

        currentY += buttonHeight + subsequentRowGap;

        this.addDrawableChild(new Button(col1X, currentY, buttonWidth, buttonHeight, Text.translatable("options.language"), () -> this.client.setScreen(new LanguageOptionsScreen(this, this.client.options, this.client.getLanguageManager()))));
        this.addDrawableChild(new Button(col2X, currentY, buttonWidth, buttonHeight, Text.translatable("options.chat"), () -> this.client.setScreen(new ChatOptionsScreen(this, this.client.options))));

        currentY += buttonHeight + subsequentRowGap;

        this.addDrawableChild(new Button(col1X, currentY, buttonWidth, buttonHeight, Text.translatable("options.resourcepack"), () -> this.client.setScreen(new PackScreen(this.client.getResourcePackManager(), this::refreshResourcePacks, this.client.getResourcePackDir(), Text.translatable("resourcePack.title")))));
        if(showDifficultyOptions) {
            this.addDrawableChild(new Button(col2X, currentY, buttonWidth, buttonHeight, Text.translatable("options.accessibility"), () -> this.client.setScreen(new AccessibilityOptionsScreen(this, this.client.options))));
        } else {
            this.addDrawableChild(new Button(col2X, currentY, buttonWidth, buttonHeight, Text.of("Special features"), () -> {}));
        }

        currentY += buttonHeight + subsequentRowGap;

        this.addDrawableChild(new Button(
                centerX - buttonWidth / 2,
                currentY + 15,
                buttonWidth,
                buttonHeight,
                Text.translatable("gui.done"),
                () -> this.client.setScreen(this.parent))
        );
    }

    private void cycleDifficulty() {
        if (this.client.world == null) return;
        Difficulty current = this.client.world.getDifficulty();
        Difficulty next = DIFFICULTIES[(current.getId() + 1) % DIFFICULTIES.length];
        this.client.getNetworkHandler().sendPacket(new UpdateDifficultyC2SPacket(next));
    }

    private void lockDifficulty() {
        this.client.setScreen(new ConfirmScreen(this::confirmLock, Text.translatable("difficulty.lock.title"), Text.translatable("difficulty.lock.question", this.client.world.getDifficulty().getTranslatableName())));
    }

    private void confirmLock(boolean confirmed) {
        this.client.setScreen(this);
        if (confirmed && this.client.world != null) {
            this.client.getNetworkHandler().sendPacket(new UpdateDifficultyLockC2SPacket(true));
            if(this.difficultyButton != null) this.difficultyButton.active = false;
            if(this.lockButton != null) this.lockButton.active = false;
        }
    }

    private void refreshResourcePacks(ResourcePackManager m) {
        this.client.options.refreshResourcePacks(m);
        this.client.setScreen(this);
    }

    private Text getFovText() {
        int fov = this.client.options.getFov().getValue();
        return Text.translatable("options.fov").append(": ").append(fov == 70 ? Text.translatable("options.fov.normal") : (fov == 110 ? Text.translatable("options.fov.quake_pro") : Text.of(String.valueOf(fov))));
    }

    private void onFovChange(double value) {
        int fov = 30 + (int) Math.round(value * 80.0);
        this.client.options.getFov().setValue(fov);
        this.fovSlider.setMessage(getFovText());
    }

    private Color difficultyColor(Difficulty difficulty) {
        return switch (difficulty) {
            case PEACEFUL -> new Color(80, 255, 255);
            case EASY -> new Color(80, 255, 80);
            case NORMAL -> new Color(255, 165, 0);
            case HARD -> new Color(255, 80, 80);
            default -> Color.WHITE;
        };
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();
        MsdfFont font = FontType.sf_regular.get();
        float fontSize = 16f;
        String titleText = this.title.getString();
        float textWidth = font.getWidth(titleText, fontSize);
        float textX = (this.width - textWidth) / 2.0f;

        Builder.text().font(font).text(titleText).color(Color.WHITE).size(fontSize).thickness(0.05f).build().render(matrix, textX, 40);

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        this.client.setScreen(this.parent);
    }
}