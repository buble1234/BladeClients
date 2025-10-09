package win.blade.common.gui.impl.screen.options;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import win.blade.common.gui.button.Button;
import win.blade.common.gui.button.Slider;
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.builders.states.SizeState;
import win.blade.common.utils.render.msdf.FontType;
import win.blade.common.utils.render.msdf.MsdfFont;
import win.blade.common.utils.render.renderers.impl.BuiltTexture;

import java.awt.*;

/**
 * Автор: NoCap
 * Дата создания: 01.08.2025
 */
public class AccessibilityOptionsScreen extends Screen {

    private final Screen parent;
    private final GameOptions options;
    
    private Button autoJumpButton;
    private Button bobViewButton;
    private Button sneakToggledButton;
    private Button sprintToggledButton;
    private Button hideLightningFlashesButton;
    private Button monochromeLogoButton;
    private Button hideSplashTextsButton;
    private Button rotateWithMinecartButton;
    private Slider distortionEffectSlider;
    private Slider fovEffectSlider;
    private Slider darknessEffectSlider;
    private Slider damageTiltSlider;
    private Slider glintSpeedSlider;
    private Slider glintStrengthSlider;
    private Slider notificationTimeSlider;
    private Slider panoramaSpeedSlider;
    private boolean bg;

    public AccessibilityOptionsScreen(Screen parent, GameOptions options, boolean bg) {
        super(Text.translatable("options.accessibility.title"));
        this.parent = parent;
        this.options = options;
        this.bg = bg;
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

        autoJumpButton = new Button(col1X, currentY, buttonWidth, buttonHeight, getAutoJumpText(), this::toggleAutoJump);
        addDrawableChild(autoJumpButton);
        bobViewButton = new Button(col2X, currentY, buttonWidth, buttonHeight, getBobViewText(), this::toggleBobView);
        addDrawableChild(bobViewButton);
        currentY += buttonHeight + rowGap;
        
        sneakToggledButton = new Button(col1X, currentY, buttonWidth, buttonHeight, getSneakToggledText(), this::toggleSneak);
        addDrawableChild(sneakToggledButton);
        sprintToggledButton = new Button(col2X, currentY, buttonWidth, buttonHeight, getSprintToggledText(), this::toggleSprint);
        addDrawableChild(sprintToggledButton);
        currentY += buttonHeight + rowGap;

        distortionEffectSlider = new Slider(col1X, currentY, buttonWidth, buttonHeight, getDistortionEffectText(), options.getDistortionEffectScale().getValue(), this::onDistortionEffectChange);
        addDrawableChild(distortionEffectSlider);
        fovEffectSlider = new Slider(col2X, currentY, buttonWidth, buttonHeight, getFovEffectText(), options.getFovEffectScale().getValue(), this::onFovEffectChange);
        addDrawableChild(fovEffectSlider);
        currentY += buttonHeight + rowGap;

        darknessEffectSlider = new Slider(col1X, currentY, buttonWidth, buttonHeight, getDarknessEffectText(), options.getDarknessEffectScale().getValue(), this::onDarknessEffectChange);
        addDrawableChild(darknessEffectSlider);
        damageTiltSlider = new Slider(col2X, currentY, buttonWidth, buttonHeight, getDamageTiltText(), options.getDamageTiltStrength().getValue(), this::onDamageTiltChange);
        addDrawableChild(damageTiltSlider);
        currentY += buttonHeight + rowGap;

        glintSpeedSlider = new Slider(col1X, currentY, buttonWidth, buttonHeight, getGlintSpeedText(), options.getGlintSpeed().getValue(), this::onGlintSpeedChange);
        addDrawableChild(glintSpeedSlider);
        glintStrengthSlider = new Slider(col2X, currentY, buttonWidth, buttonHeight, getGlintStrengthText(), options.getGlintStrength().getValue(), this::onGlintStrengthChange);
        addDrawableChild(glintStrengthSlider);
        currentY += buttonHeight + rowGap;

        notificationTimeSlider = new Slider(col1X, currentY, buttonWidth, buttonHeight, getNotificationTimeText(), (options.getNotificationDisplayTime().getValue() - 0.5) / 9.5, this::onNotificationTimeChange);
        addDrawableChild(notificationTimeSlider);
        panoramaSpeedSlider = new Slider(col2X, currentY, buttonWidth, buttonHeight, getPanoramaSpeedText(), options.getPanoramaSpeed().getValue(), this::onPanoramaSpeedChange);
        addDrawableChild(panoramaSpeedSlider);
        currentY += buttonHeight + rowGap;
        
        hideLightningFlashesButton = new Button(col1X, currentY, buttonWidth, buttonHeight, getHideLightningText(), this::toggleHideLightning);
        addDrawableChild(hideLightningFlashesButton);
        monochromeLogoButton = new Button(col2X, currentY, buttonWidth, buttonHeight, getMonochromeLogoText(), this::toggleMonochromeLogo);
        addDrawableChild(monochromeLogoButton);
        currentY += buttonHeight + rowGap;

        hideSplashTextsButton = new Button(col1X, currentY, buttonWidth, buttonHeight, getHideSplashTextsText(), this::toggleHideSplashTexts);
        addDrawableChild(hideSplashTextsButton);
        rotateWithMinecartButton = new Button(col2X, currentY, buttonWidth, buttonHeight, getRotateWithMinecartText(), this::toggleRotateWithMinecart);
        addDrawableChild(rotateWithMinecartButton);
        rotateWithMinecartButton.active = isMinecartImprovementsExperimentEnabled();
        currentY += buttonHeight;
        
        this.addDrawableChild(new Button(
                centerX - buttonWidth / 2,
                currentY + 15,
                buttonWidth,
                buttonHeight,
                ScreenTexts.DONE,
                this::close)
        );
    }

    private void toggleAutoJump() { options.getAutoJump().setValue(!options.getAutoJump().getValue()); autoJumpButton.setMessage(getAutoJumpText()); }
    private void toggleBobView() { options.getBobView().setValue(!options.getBobView().getValue()); bobViewButton.setMessage(getBobViewText()); }
    private void toggleSneak() { options.getSneakToggled().setValue(!options.getSneakToggled().getValue()); sneakToggledButton.setMessage(getSneakToggledText()); }
    private void toggleSprint() { options.getSprintToggled().setValue(!options.getSprintToggled().getValue()); sprintToggledButton.setMessage(getSprintToggledText()); }
    private void toggleHideLightning() { options.getHideLightningFlashes().setValue(!options.getHideLightningFlashes().getValue()); hideLightningFlashesButton.setMessage(getHideLightningText()); }
    private void toggleMonochromeLogo() { options.getMonochromeLogo().setValue(!options.getMonochromeLogo().getValue()); monochromeLogoButton.setMessage(getMonochromeLogoText()); }
    private void toggleHideSplashTexts() { options.getHideSplashTexts().setValue(!options.getHideSplashTexts().getValue()); hideSplashTextsButton.setMessage(getHideSplashTextsText()); }
    private void toggleRotateWithMinecart() { options.getRotateWithMinecart().setValue(!options.getRotateWithMinecart().getValue()); rotateWithMinecartButton.setMessage(getRotateWithMinecartText()); }

    private void onDistortionEffectChange(double value) { options.getDistortionEffectScale().setValue(value); distortionEffectSlider.setMessage(getDistortionEffectText()); }
    private void onFovEffectChange(double value) { options.getFovEffectScale().setValue(value); fovEffectSlider.setMessage(getFovEffectText()); }
    private void onDarknessEffectChange(double value) { options.getDarknessEffectScale().setValue(value); darknessEffectSlider.setMessage(getDarknessEffectText()); }
    private void onDamageTiltChange(double value) { options.getDamageTiltStrength().setValue(value); damageTiltSlider.setMessage(getDamageTiltText()); }
    private void onGlintSpeedChange(double value) { options.getGlintSpeed().setValue(value); glintSpeedSlider.setMessage(getGlintSpeedText()); }
    private void onGlintStrengthChange(double value) { options.getGlintStrength().setValue(value); glintStrengthSlider.setMessage(getGlintStrengthText()); }
    private void onPanoramaSpeedChange(double value) { options.getPanoramaSpeed().setValue(value); panoramaSpeedSlider.setMessage(getPanoramaSpeedText()); }
    private void onNotificationTimeChange(double value) { options.getNotificationDisplayTime().setValue(0.5 + value * 9.5); notificationTimeSlider.setMessage(getNotificationTimeText()); }

    private Text getAutoJumpText() { return ScreenTexts.composeToggleText(Text.translatable("options.autoJump"), options.getAutoJump().getValue()); }
    private Text getBobViewText() { return ScreenTexts.composeToggleText(Text.translatable("options.viewBobbing"), options.getBobView().getValue()); }
    private Text getSneakToggledText() { return Text.translatable("key.sneak").append(": ").append(options.getSneakToggled().getValue() ? Text.translatable("options.key.toggle") : Text.translatable("options.key.hold")); }
    private Text getSprintToggledText() { return Text.translatable("key.sprint").append(": ").append(options.getSprintToggled().getValue() ? Text.translatable("options.key.toggle") : Text.translatable("options.key.hold")); }
    private Text getHideLightningText() { return ScreenTexts.composeToggleText(Text.translatable("options.hideLightningFlashes"), options.getHideLightningFlashes().getValue()); }
    private Text getMonochromeLogoText() { return ScreenTexts.composeToggleText(Text.translatable("options.darkMojangStudiosBackgroundColor"), options.getMonochromeLogo().getValue()); }
    private Text getHideSplashTextsText() { return ScreenTexts.composeToggleText(Text.translatable("options.hideSplashTexts"), options.getHideSplashTexts().getValue()); }
    private Text getRotateWithMinecartText() { return ScreenTexts.composeToggleText(Text.translatable("options.rotateWithMinecart"), options.getRotateWithMinecart().getValue()); }
    private Text getPercentText(Text prefix, double value) { return value == 0.0 ? prefix.copy().append(": ").append(ScreenTexts.OFF) : prefix.copy().append(": " + (int)(value * 100.0) + "%"); }
    private Text getDistortionEffectText() { return getPercentText(Text.translatable("options.screenEffectScale"), options.getDistortionEffectScale().getValue()); }
    private Text getFovEffectText() { return getPercentText(Text.translatable("options.fovEffectScale"), options.getFovEffectScale().getValue()); }
    private Text getDarknessEffectText() { return getPercentText(Text.translatable("options.darknessEffectScale"), options.getDarknessEffectScale().getValue()); }
    private Text getDamageTiltText() { return getPercentText(Text.translatable("options.damageTiltStrength"), options.getDamageTiltStrength().getValue()); }
    private Text getGlintSpeedText() { return getPercentText(Text.translatable("options.glintSpeed"), options.getGlintSpeed().getValue()); }
    private Text getGlintStrengthText() { return getPercentText(Text.translatable("options.glintStrength"), options.getGlintStrength().getValue()); }
    private Text getPanoramaSpeedText() { return getPercentText(Text.translatable("options.accessibility.panorama_speed"), options.getPanoramaSpeed().getValue()); }
    private Text getNotificationTimeText() { return Text.translatable("options.notifications.display_time").append(": ").append(String.format("%.1fx", options.getNotificationDisplayTime().getValue())); }

    private boolean isMinecartImprovementsExperimentEnabled() {
        return this.client.world != null && this.client.world.getEnabledFeatures().contains(FeatureFlags.MINECART_IMPROVEMENTS);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (bg == true) {
            Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();

            AbstractTexture customTexture = MinecraftClient.getInstance().getTextureManager().getTexture(Identifier.of("blade", "textures/mainmenu.png"));
            BuiltTexture customIcon = Builder.texture()
                    .size(new SizeState(this.width, this.height))
                    .texture(0.0f, 0.0f, 1.0f, 1.0f, customTexture)
                    .smoothness(3.0f)
                    .build();
            customIcon.render(matrix, 0, 0);
        }
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
        this.options.write();
    }

    @Override
    public void close() {
        this.client.setScreen(this.parent);
    }
}