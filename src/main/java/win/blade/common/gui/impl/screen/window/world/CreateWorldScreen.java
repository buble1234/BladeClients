package win.blade.common.gui.impl.screen.window.world;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.resource.DataConfiguration;
import net.minecraft.server.integrated.IntegratedServerLoader;
import net.minecraft.text.Text;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import net.minecraft.world.dimension.DimensionOptionsRegistryHolder;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.gen.WorldPreset;
import net.minecraft.world.gen.WorldPresets;
import net.minecraft.world.level.LevelInfo;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.glfw.GLFW;
import win.blade.common.gui.button.Button;
import win.blade.common.gui.impl.menu.helpers.TextBox;
import win.blade.common.gui.impl.screen.BaseWindowScreen;
import win.blade.common.utils.math.MathUtility;
import win.blade.common.utils.other.TextAlign;
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.builders.states.QuadColorState;
import win.blade.common.utils.render.builders.states.QuadRadiusState;
import win.blade.common.utils.render.builders.states.SizeState;
import win.blade.common.utils.render.msdf.FontType;
import win.blade.common.utils.render.msdf.MsdfFont;

import java.awt.Color;
import java.util.function.Function;

public class CreateWorldScreen extends BaseWindowScreen {

    private final Screen parent;
    private TextBox worldT;

    private int windowX;
    private int windowY;

    private boolean cheats;
    private int modeIndex;
    private int difficultyIndex;

    private static final String[] mode = {"Survival", "Creative", "Hardcore", "Spectator"};
    private static final GameMode[] gameMode = {GameMode.SURVIVAL, GameMode.CREATIVE, GameMode.SURVIVAL, GameMode.SPECTATOR};
    private static final Difficulty[] difficulties = {Difficulty.PEACEFUL, Difficulty.EASY, Difficulty.NORMAL, Difficulty.HARD};

    MsdfFont font = FontType.sf_regular.get();

    public CreateWorldScreen(Screen parent) {
        super(Text.of("New World"), 306, 170);
        this.parent = parent;
        this.cheats = true;
        this.modeIndex = 0;
        this.difficultyIndex = 2;
    }

    @Override
    protected void init() {
        windowX = (this.width - 306) / 2;
        windowY = (this.height - 170) / 2;

        if (this.worldT == null) {
            this.worldT = new TextBox(
                    windowX + 22, windowY + 49, 256,
                    FontType.sf_regular.get(), 10f, Color.WHITE.getRGB(), TextAlign.LEFT,
                    "Enter the name of the world..", false, false
            );
        } else {
            this.worldT.x = windowX + 22;
            this.worldT.y = windowY + 49;
        }

        this.addDrawableChild(new Button(windowX, windowY + 130, 148, 30, Text.of("Create"), this::createWorld));
        this.addDrawableChild(new Button(windowX + 158, windowY + 130, 148, 30, Text.of("Exit"), this::close));
    }

    private void createWorld() {
        IntegratedServerLoader loader = this.client.createIntegratedServerLoader();
        
        
        String worldN = this.worldT.getText().trim();
        
        
        if (StringUtils.isEmpty(worldN)) {
            worldN = "New World";
        }

        GameMode selectedMode = gameMode[this.modeIndex];
        boolean isHard = this.modeIndex == 2;
        GameRules gameRules = new GameRules(DataConfiguration.SAFE_MODE.enabledFeatures());
        LevelInfo levelInfo = new LevelInfo(worldN, selectedMode, isHard, difficulties[this.difficultyIndex], this.cheats, gameRules, DataConfiguration.SAFE_MODE);
        GeneratorOptions generatorOptions = new GeneratorOptions(GeneratorOptions.getRandomSeed(), true, false);

        Function<RegistryWrapper.WrapperLookup, DimensionOptionsRegistryHolder> registrySupplier = (wrapperLookup) -> {
            RegistryEntry.Reference<WorldPreset> preset = wrapperLookup.getOrThrow(RegistryKeys.WORLD_PRESET).getOrThrow(WorldPresets.DEFAULT);
            return preset.value().createDimensionsRegistryHolder();
        };

        loader.createAndStart(worldN, levelInfo, generatorOptions, registrySupplier, this);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        Builder.rectangle()
                .size(new SizeState(276, 29.5f))
                .color(new QuadColorState(new Color(28, 26, 37)))
                .radius(new QuadRadiusState(8))
                .build()
                .render(windowX + 15, windowY + 40);

        worldT.draw(context, 1.0f);

        float currentX = windowX + 15;

        final float optionY = windowY + 75;

        renderComponent(
                context,
                currentX,
                optionY,
                (276f - 10f) / 3f,
                29.5f,
                "Cheats",
                cheats ? "ON" : "OFF",
                cheatColor(cheats)
        );

        currentX += (276f - 10f) / 3f + 5f;

        String modeName = mode[this.modeIndex];
        renderComponent(
                context,
                currentX,
                optionY,
                (276f - 10f) / 3f,
                29.5f,
                "Mode",
                StringUtils.capitalize(modeName.toLowerCase()),
                gameColor(gameMode[modeIndex], modeIndex == 2)
        );

        currentX += (276f - 10f) / 3f + 5f;

        Difficulty difficulty = difficulties[this.difficultyIndex];
        renderComponent(
                context,
                currentX,
                optionY,
                (276f - 10f) / 3f,
                29.5f,
                "Difficulty",
                StringUtils.capitalize(difficulty.getName().toLowerCase()),
                difficultyColor(difficulty)
        );
    }

    private void renderComponent(DrawContext context, float x, float y, float width, float height, String label, String value, Color valueColor) {
        Builder.rectangle()
                .size(new SizeState(width, height))
                .color( new QuadColorState( new Color(15, 13, 20, 220)))
                .radius(new QuadRadiusState(8))
                .build()
                .render(context.getMatrices().peek().getPositionMatrix(), x, y);

        float fontSize = 8;
        String slash;

        slash = " / ";

        float totalWidth = font.getWidth(label, fontSize) + font.getWidth(slash, fontSize) + font.getWidth(value, fontSize);

        float currentX = x + (width - totalWidth) / 2f;
        float tY = y + (height - fontSize) / 2f;

        Builder.text().font(font)
                .text(label)
                .color(Color.GRAY)
                .size(fontSize)
                .build()
                .render(currentX, tY);

        currentX += font.getWidth(label, fontSize);
        Builder.text().font(font)
                .text(slash)
                .color(Color.GRAY)
                .size(fontSize)
                .build()
                .render(currentX, tY);

        currentX += font.getWidth(slash, fontSize);
        Builder.text().font(font)
                .text(value)
                .color(valueColor)
                .size(fontSize)
                .build()
                .render(currentX, tY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            worldT.selected = MathUtility.isHovered(mouseX, mouseY, windowX + 15, windowY + 40, 276, 29.5f);
            if (worldT.selected) return super.mouseClicked(mouseX, mouseY, button);

            float currentX = windowX + 15;

            if (MathUtility.isHovered(mouseX, mouseY, currentX, windowY + 75, (276f - 10f) / 3f, 29.5f)) {
                cheats = !cheats;
                return true;
            }

            currentX += (276f - 10f) / 3f + 5f;

            if (MathUtility.isHovered(mouseX, mouseY, currentX, windowY + 75, (276f - 10f) / 3f, 29.5f)) {
                modeIndex = (modeIndex + 1) % mode.length;
                return true;
            }

            currentX += (276f - 10f) / 3f + 5f;

            if (MathUtility.isHovered(mouseX, mouseY, currentX, windowY + 75, (276f - 10f) / 3f, 29.5f)) {
                difficultyIndex = (difficultyIndex + 1) % difficulties.length;
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (worldT.selected) {
            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                createWorld();
                return true;
            }
            worldT.keyPressed(keyCode, scanCode, modifiers);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (worldT.selected) {
            worldT.charTyped(chr, modifiers);
            return true;
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public void close() {
        this.client.setScreen(this.parent);
    }

    private Color cheatColor(boolean enabled) {
        return enabled ? new Color(80, 255, 80) : new Color(255, 80, 80);
    }

    private Color gameColor(GameMode mode, boolean isHardcore) {
        if (isHardcore) return new Color(255, 80, 80);
        switch (mode) {
            case SURVIVAL: return new Color(255, 165, 0);
            case CREATIVE: case SPECTATOR: return new Color(80, 255, 80);
            default: return Color.WHITE;
        }
    }

    private Color difficultyColor(Difficulty difficulty) {
        switch (difficulty) {
            case PEACEFUL: case EASY: case NORMAL: return new Color(80, 255, 80);
            case HARD: return new Color(255, 80, 80);
            default: return Color.WHITE;
        }
    }
}