package win.blade.common.gui.impl.screen.options;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.*;
import net.minecraft.client.render.ChunkBuilderMode;
import net.minecraft.client.util.Monitor;
import net.minecraft.client.util.VideoMode;
import net.minecraft.particle.ParticlesMode;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import org.joml.Matrix4f;
import win.blade.common.gui.button.Button;
import win.blade.common.gui.button.Slider;
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.msdf.FontType;
import win.blade.common.utils.render.msdf.MsdfFont;

import java.awt.Color;
import java.util.Optional;

/**
 * Автор: NoCap
 * Дата создания: 02.08.2025
 */
public class VideoOptionsScreen extends Screen {

    private final Screen parent;
    private final GameOptions options;
    private final int mipmapLevels;

    // Widgets
    private Button graphicsButton, cloudsButton, vsyncButton, fullscreenButton, entityShadowsButton, aoButton,
            bobViewButton, showAutosaveIndicatorButton, particlesButton, attackIndicatorButton,
            chunkBuilderModeButton, guiScaleButton, fullscreenResolutionButton, inactivityFpsLimitButton;

    private Slider viewDistanceSlider, simulationDistanceSlider, maxFpsSlider, gammaSlider, mipmapLevelsSlider,
            entityDistanceScalingSlider, distortionEffectSlider, fovEffectSlider, glintSpeedSlider,
            glintStrengthSlider, menuBackgroundBlurrinessSlider, biomeBlendRadiusSlider;

    private int currentResolutionIndex;

    public VideoOptionsScreen(Screen parent, GameOptions options) {
        super(Text.translatable("options.videoTitle"));
        this.parent = parent;
        this.options = options;
        this.mipmapLevels = options.getMipmapLevels().getValue();
    }

    @Override
    protected void init() {
        super.init();

        int topY = 60;
        int buttonsTopY = topY + 30;
        int buttonWidth = 150;
        int buttonHeight = 32;
        int rowGap = 4;

        int centerX = this.width / 2;
        int col1X = centerX - buttonWidth - 5 / 2;
        int col2X = centerX + 5 / 2;

        int currentY = buttonsTopY;

        // Row 1: Graphics, Clouds
        graphicsButton = new Button(col1X, currentY, buttonWidth, buttonHeight, getGraphicsText(), this::cycleGraphics);
        addDrawableChild(graphicsButton);
        cloudsButton = new Button(col2X, currentY, buttonWidth, buttonHeight, getCloudsText(), this::cycleClouds);
        addDrawableChild(cloudsButton);
        currentY += buttonHeight + rowGap;

        // Row 2: View Distance, Simulation Distance
        boolean highMemory = Runtime.getRuntime().maxMemory() >= 1000000000L;
        int maxViewDist = highMemory ? 32 : 16;
        int maxSimDist = highMemory ? 32 : 16;
        viewDistanceSlider = new Slider(col1X, currentY, buttonWidth, buttonHeight, getViewDistanceText(), (options.getViewDistance().getValue() - 2.0) / (maxViewDist - 2.0), v -> onViewDistanceChange(v, maxViewDist));
        addDrawableChild(viewDistanceSlider);
        simulationDistanceSlider = new Slider(col2X, currentY, buttonWidth, buttonHeight, getSimulationDistanceText(), (options.getSimulationDistance().getValue() - 5.0) / (maxSimDist - 5.0), v -> onSimDistanceChange(v, maxSimDist));
        addDrawableChild(simulationDistanceSlider);
        currentY += buttonHeight + rowGap;

        // Row 3: Max FPS, Gamma
        maxFpsSlider = new Slider(col1X, currentY, buttonWidth, buttonHeight, getMaxFpsText(), options.getMaxFps().getValue() >= 260 ? 1.0 : (options.getMaxFps().getValue() / 10.0 - 1.0) / 25.0, this::onMaxFpsChange);
        addDrawableChild(maxFpsSlider);
        gammaSlider = new Slider(col2X, currentY, buttonWidth, buttonHeight, getGammaText(), options.getGamma().getValue(), this::onGammaChange);
        addDrawableChild(gammaSlider);
        currentY += buttonHeight + rowGap;

        // Row 4: VSync, Fullscreen
        vsyncButton = new Button(col1X, currentY, buttonWidth, buttonHeight, getVsyncText(), this::toggleVsync);
        addDrawableChild(vsyncButton);
        fullscreenButton = new Button(col2X, currentY, buttonWidth, buttonHeight, getFullscreenText(), this::toggleFullscreen);
        addDrawableChild(fullscreenButton);
        currentY += buttonHeight + rowGap;

        // Row 5: Fullscreen Resolution, GUI Scale
        Monitor monitor = this.client.getWindow().getMonitor();
        currentResolutionIndex = monitor == null ? -1 : this.client.getWindow().getFullscreenVideoMode().map(monitor::findClosestVideoModeIndex).orElse(-1);
        fullscreenResolutionButton = new Button(col1X, currentY, buttonWidth, buttonHeight, getFullscreenResolutionText(), this::cycleFullscreenResolution);
        fullscreenResolutionButton.active = monitor != null;
        addDrawableChild(fullscreenResolutionButton);
        guiScaleButton = new Button(col2X, currentY, buttonWidth, buttonHeight, getGuiScaleText(), this::cycleGuiScale);
        addDrawableChild(guiScaleButton);
        currentY += buttonHeight + rowGap;

        // Row 6: Mipmap Levels, Biome Blend
        mipmapLevelsSlider = new Slider(col1X, currentY, buttonWidth, buttonHeight, getMipmapLevelsText(), options.getMipmapLevels().getValue() / 4.0, this::onMipmapLevelsChange);
        addDrawableChild(mipmapLevelsSlider);
        biomeBlendRadiusSlider = new Slider(col2X, currentY, buttonWidth, buttonHeight, getBiomeBlendText(), options.getBiomeBlendRadius().getValue() / 7.0, this::onBiomeBlendChange);
        addDrawableChild(biomeBlendRadiusSlider);
        currentY += buttonHeight + rowGap;

        // Row 7: Entity Shadows, AO
        entityShadowsButton = new Button(col1X, currentY, buttonWidth, buttonHeight, getEntityShadowsText(), this::toggleEntityShadows);
        addDrawableChild(entityShadowsButton);
        aoButton = new Button(col2X, currentY, buttonWidth, buttonHeight, getAoText(), this::toggleAo);
        addDrawableChild(aoButton);
        currentY += buttonHeight + rowGap;

        // Row 8: Particles, Attack Indicator
        particlesButton = new Button(col1X, currentY, buttonWidth, buttonHeight, getParticlesText(), this::cycleParticles);
        addDrawableChild(particlesButton);
        attackIndicatorButton = new Button(col2X, currentY, buttonWidth, buttonHeight, getAttackIndicatorText(), this::cycleAttackIndicator);
        addDrawableChild(attackIndicatorButton);
        currentY += buttonHeight + rowGap;

        // Row 9: Chunk Builder, Bobbing
        chunkBuilderModeButton = new Button(col1X, currentY, buttonWidth, buttonHeight, getChunkBuilderModeText(), this::cycleChunkBuilderMode);
        addDrawableChild(chunkBuilderModeButton);
        bobViewButton = new Button(col2X, currentY, buttonWidth, buttonHeight, getBobViewText(), this::toggleBobView);
        addDrawableChild(bobViewButton);
        currentY += buttonHeight + rowGap;

        // Row 10: Entity Distance, Distortion Effect
        entityDistanceScalingSlider = new Slider(col1X, currentY, buttonWidth, buttonHeight, getEntityDistanceText(), (options.getEntityDistanceScaling().getValue() - 0.5) / 4.5, this::onEntityDistanceChange);
        addDrawableChild(entityDistanceScalingSlider);
        distortionEffectSlider = new Slider(col2X, currentY, buttonWidth, buttonHeight, getDistortionEffectText(), options.getDistortionEffectScale().getValue(), this::onDistortionEffectChange);
        addDrawableChild(distortionEffectSlider);
        currentY += buttonHeight + rowGap;

        // Row 11: FOV Effect, Glint Speed
        fovEffectSlider = new Slider(col1X, currentY, buttonWidth, buttonHeight, getFovEffectText(), options.getFovEffectScale().getValue(), this::onFovEffectChange);
        addDrawableChild(fovEffectSlider);
        glintSpeedSlider = new Slider(col2X, currentY, buttonWidth, buttonHeight, getGlintSpeedText(), options.getGlintSpeed().getValue(), this::onGlintSpeedChange);
        addDrawableChild(glintSpeedSlider);
        currentY += buttonHeight + rowGap;

        // Row 12: Glint Strength, Menu Blur
        glintStrengthSlider = new Slider(col1X, currentY, buttonWidth, buttonHeight, getGlintStrengthText(), options.getGlintStrength().getValue(), this::onGlintStrengthChange);
        addDrawableChild(glintStrengthSlider);
        menuBackgroundBlurrinessSlider = new Slider(col2X, currentY, buttonWidth, buttonHeight, getMenuBlurText(), options.getMenuBackgroundBlurriness().getValue() / 10.0, this::onMenuBlurChange);
        addDrawableChild(menuBackgroundBlurrinessSlider);
        currentY += buttonHeight + rowGap;

        // Row 13: Autosave Indicator, Inactivity FPS
        showAutosaveIndicatorButton = new Button(col1X, currentY, buttonWidth, buttonHeight, getShowAutosaveIndicatorText(), this::toggleShowAutosaveIndicator);
        addDrawableChild(showAutosaveIndicatorButton);
        inactivityFpsLimitButton = new Button(col2X, currentY, buttonWidth, buttonHeight, getInactivityFpsText(), this::cycleInactivityFps);
        addDrawableChild(inactivityFpsLimitButton);
        currentY += buttonHeight;

        // Done button
        this.addDrawableChild(new Button(
                centerX - buttonWidth / 2,
                currentY + 15,
                buttonWidth,
                buttonHeight,
                ScreenTexts.DONE,
                this::close)
        );
    }

    // --- Handlers & Text Getters ---

    private void cycleGraphics() {
        GraphicsMode[] modes = GraphicsMode.values();
        GraphicsMode current = options.getGraphicsMode().getValue();
        options.getGraphicsMode().setValue(modes[(current.ordinal() + 1) % modes.length]);
        client.worldRenderer.reload();
        graphicsButton.setMessage(getGraphicsText());
    }
    private Text getGraphicsText() { return Text.translatable("options.graphics").append(": ").append(Text.translatable(options.getGraphicsMode().getValue().getTranslationKey())); }

    private void cycleClouds() {
        CloudRenderMode[] modes = CloudRenderMode.values();
        CloudRenderMode current = options.getCloudRenderMode().getValue();
        options.getCloudRenderMode().setValue(modes[(current.ordinal() + 1) % modes.length]);
        cloudsButton.setMessage(getCloudsText());
    }
    private Text getCloudsText() { return Text.translatable("options.renderClouds").append(": ").append(options.getCloudRenderMode().getValue().getText()); }

    private void onViewDistanceChange(double value, int max) { options.getViewDistance().setValue((int)Math.round(2 + value * (max - 2))); viewDistanceSlider.setMessage(getViewDistanceText()); }
    private Text getViewDistanceText() { return Text.translatable("options.renderDistance").append(": ").append(Text.translatable("options.chunks", options.getViewDistance().getValue())); }

    private void onSimDistanceChange(double value, int max) { options.getSimulationDistance().setValue((int)Math.round(5 + value * (max - 5))); simulationDistanceSlider.setMessage(getSimulationDistanceText()); }
    private Text getSimulationDistanceText() { return Text.translatable("options.simulationDistance").append(": ").append(Text.translatable("options.chunks", options.getSimulationDistance().getValue())); }

    private void onMaxFpsChange(double value) { int val = (int)Math.round(1 + value * 25) * 10; if(val > 250) val = 260; options.getMaxFps().setValue(val); client.getInactivityFpsLimiter().setMaxFps(val); maxFpsSlider.setMessage(getMaxFpsText()); }
    private Text getMaxFpsText() { int val = options.getMaxFps().getValue(); return Text.translatable("options.framerateLimit").append(": ").append(val >= 260 ? Text.translatable("options.framerateLimit.max") : Text.translatable("options.framerate", val)); }

    private void onGammaChange(double value) { options.getGamma().setValue(value); gammaSlider.setMessage(getGammaText()); }
    private Text getGammaText() { int i = (int)(options.getGamma().getValue() * 100.0); if(i == 50) return GameOptions.getGenericValueText(Text.translatable("options.gamma"), Text.translatable("options.gamma.default")); if(i == 0) return GameOptions.getGenericValueText(Text.translatable("options.gamma"), Text.translatable("options.gamma.min")); if(i == 100) return GameOptions.getGenericValueText(Text.translatable("options.gamma"), Text.translatable("options.gamma.max")); return GameOptions.getGenericValueText(Text.translatable("options.gamma"), Text.literal(String.valueOf(i))); }

    private void toggleVsync() { options.getEnableVsync().setValue(!options.getEnableVsync().getValue()); client.getWindow().setVsync(options.getEnableVsync().getValue()); vsyncButton.setMessage(getVsyncText()); }
    private Text getVsyncText() { return ScreenTexts.composeToggleText(Text.translatable("options.vsync"), options.getEnableVsync().getValue()); }

    private void toggleFullscreen() { options.getFullscreen().setValue(!options.getFullscreen().getValue()); if (client.getWindow().isFullscreen() != options.getFullscreen().getValue()) { client.getWindow().toggleFullscreen(); options.getFullscreen().setValue(client.getWindow().isFullscreen()); } fullscreenButton.setMessage(getFullscreenText()); }
    private Text getFullscreenText() { return ScreenTexts.composeToggleText(Text.translatable("options.fullscreen"), options.getFullscreen().getValue()); }

    private void cycleFullscreenResolution() { Monitor monitor = client.getWindow().getMonitor(); if (monitor == null) return; currentResolutionIndex = (currentResolutionIndex + 2) % (monitor.getVideoModeCount() + 1) - 1; if (currentResolutionIndex == -1) client.getWindow().setFullscreenVideoMode(Optional.empty()); else client.getWindow().setFullscreenVideoMode(Optional.of(monitor.getVideoMode(currentResolutionIndex))); fullscreenResolutionButton.setMessage(getFullscreenResolutionText()); }
    private Text getFullscreenResolutionText() { Monitor monitor = client.getWindow().getMonitor(); if (monitor == null) return Text.translatable("options.fullscreen.unavailable"); if (currentResolutionIndex == -1) return GameOptions.getGenericValueText(Text.translatable("options.fullscreen.resolution"), Text.translatable("options.fullscreen.current")); VideoMode mode = monitor.getVideoMode(currentResolutionIndex); return GameOptions.getGenericValueText(Text.translatable("options.fullscreen.resolution"), Text.translatable("options.fullscreen.entry", mode.getWidth(), mode.getHeight(), mode.getRefreshRate(), mode.getRedBits() + mode.getGreenBits() + mode.getBlueBits())); }

    private void cycleGuiScale() { options.getGuiScale().setValue((options.getGuiScale().getValue() + 1) % (client.getWindow().calculateScaleFactor(0, client.forcesUnicodeFont()) + 1)); client.onResolutionChanged(); guiScaleButton.setMessage(getGuiScaleText()); }
    private Text getGuiScaleText() { int scale = options.getGuiScale().getValue(); return Text.translatable("options.guiScale").append(": ").append(scale == 0 ? Text.translatable("options.guiScale.auto") : Text.literal(Integer.toString(scale))); }

    private void onMipmapLevelsChange(double value) { options.getMipmapLevels().setValue((int)Math.round(value * 4.0)); mipmapLevelsSlider.setMessage(getMipmapLevelsText()); }
    private Text getMipmapLevelsText() { int val = options.getMipmapLevels().getValue(); return Text.translatable("options.mipmapLevels").append(": ").append(val == 0 ? ScreenTexts.OFF : Text.literal(String.valueOf(val))); }

    private void onBiomeBlendChange(double value) { options.getBiomeBlendRadius().setValue((int)Math.round(value * 7.0)); client.worldRenderer.reload(); biomeBlendRadiusSlider.setMessage(getBiomeBlendText()); }
    private Text getBiomeBlendText() { int i = options.getBiomeBlendRadius().getValue() * 2 + 1; return Text.translatable("options.biomeBlendRadius").append(": ").append(Text.translatable("options.biomeBlendRadius." + i)); }

    private void toggleEntityShadows() { options.getEntityShadows().setValue(!options.getEntityShadows().getValue()); entityShadowsButton.setMessage(getEntityShadowsText()); }
    private Text getEntityShadowsText() { return ScreenTexts.composeToggleText(Text.translatable("options.entityShadows"), options.getEntityShadows().getValue()); }

    private void toggleAo() { options.getAo().setValue(!options.getAo().getValue()); client.worldRenderer.reload(); aoButton.setMessage(getAoText()); }
    private Text getAoText() { return ScreenTexts.composeToggleText(Text.translatable("options.ao"), options.getAo().getValue()); }

    private void cycleParticles() {
        ParticlesMode[] modes = ParticlesMode.values();
        ParticlesMode current = options.getParticles().getValue();
        options.getParticles().setValue(modes[(current.ordinal() + 1) % modes.length]);
        particlesButton.setMessage(getParticlesText());
    }
    private Text getParticlesText() { return Text.translatable("options.particles").append(": ").append(options.getParticles().getValue().getText()); }

    private void cycleAttackIndicator() {
        AttackIndicator[] modes = AttackIndicator.values();
        AttackIndicator current = options.getAttackIndicator().getValue();
        options.getAttackIndicator().setValue(modes[(current.ordinal() + 1) % modes.length]);
        attackIndicatorButton.setMessage(getAttackIndicatorText());
    }
    private Text getAttackIndicatorText() { return Text.translatable("options.attackIndicator").append(": ").append(options.getAttackIndicator().getValue().getText()); }

    private void cycleChunkBuilderMode() {
        ChunkBuilderMode[] modes = ChunkBuilderMode.values();
        ChunkBuilderMode current = options.getChunkBuilderMode().getValue();
        options.getChunkBuilderMode().setValue(modes[(current.ordinal() + 1) % modes.length]);
        chunkBuilderModeButton.setMessage(getChunkBuilderModeText());
    }
    private Text getChunkBuilderModeText() { return Text.translatable("options.prioritizeChunkUpdates").append(": ").append(options.getChunkBuilderMode().getValue().getText()); }

    private void toggleBobView() { options.getBobView().setValue(!options.getBobView().getValue()); bobViewButton.setMessage(getBobViewText()); }
    private Text getBobViewText() { return ScreenTexts.composeToggleText(Text.translatable("options.viewBobbing"), options.getBobView().getValue()); }

    private void onEntityDistanceChange(double value) { options.getEntityDistanceScaling().setValue(0.5 + value * 4.5); entityDistanceScalingSlider.setMessage(getEntityDistanceText()); }
    private Text getEntityDistanceText() { return getPercentText(Text.translatable("options.entityDistanceScaling"), options.getEntityDistanceScaling().getValue()); }

    private void onDistortionEffectChange(double value) { options.getDistortionEffectScale().setValue(value); distortionEffectSlider.setMessage(getDistortionEffectText()); }
    private Text getDistortionEffectText() { return getPercentText(Text.translatable("options.screenEffectScale"), options.getDistortionEffectScale().getValue()); }

    private void onFovEffectChange(double value) { options.getFovEffectScale().setValue(value); fovEffectSlider.setMessage(getFovEffectText()); }
    private Text getFovEffectText() { return getPercentText(Text.translatable("options.fovEffectScale"), options.getFovEffectScale().getValue()); }

    private void onGlintSpeedChange(double value) { options.getGlintSpeed().setValue(value); glintSpeedSlider.setMessage(getGlintSpeedText()); }
    private Text getGlintSpeedText() { return getPercentText(Text.translatable("options.glintSpeed"), options.getGlintSpeed().getValue()); }

    private void onGlintStrengthChange(double value) { options.getGlintStrength().setValue(value); glintStrengthSlider.setMessage(getGlintStrengthText()); }
    private Text getGlintStrengthText() { return getPercentText(Text.translatable("options.glintStrength"), options.getGlintStrength().getValue()); }

    private void onMenuBlurChange(double value) { options.getMenuBackgroundBlurriness().setValue((int)Math.round(value * 10.0)); menuBackgroundBlurrinessSlider.setMessage(getMenuBlurText()); }
    private Text getMenuBlurText() { int val = options.getMenuBackgroundBlurriness().getValue(); return Text.translatable("options.accessibility.menu_background_blurriness").append(": ").append(val == 0 ? ScreenTexts.OFF : Text.literal(val + "%")); }

    private void toggleShowAutosaveIndicator() { options.getShowAutosaveIndicator().setValue(!options.getShowAutosaveIndicator().getValue()); showAutosaveIndicatorButton.setMessage(getShowAutosaveIndicatorText()); }
    private Text getShowAutosaveIndicatorText() { return ScreenTexts.composeToggleText(Text.translatable("options.autosaveIndicator"), options.getShowAutosaveIndicator().getValue()); }

    private void cycleInactivityFps() {
        InactivityFpsLimit[] modes = InactivityFpsLimit.values();
        InactivityFpsLimit current = options.getInactivityFpsLimit().getValue();
        options.getInactivityFpsLimit().setValue(modes[(current.ordinal() + 1) % modes.length]);
        inactivityFpsLimitButton.setMessage(getInactivityFpsText());
    }
    private Text getInactivityFpsText() { return Text.translatable("options.inactivityFpsLimit").append(": ").append(options.getInactivityFpsLimit().getValue().getText()); }

    private Text getPercentText(Text prefix, double value) { return value == 0.0 ? prefix.copy().append(": ").append(ScreenTexts.OFF) : prefix.copy().append(": " + (int)(value * 100.0) + "%"); }


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
    public void removed() {
        if (options.getMipmapLevels().getValue() != this.mipmapLevels) {
            client.setMipmapLevels(options.getMipmapLevels().getValue());
            client.reloadResourcesConcurrently();
        }
        client.getWindow().applyFullscreenVideoMode();
        options.write();
    }

    @Override
    public void close() {
        client.setScreen(this.parent);
    }
}