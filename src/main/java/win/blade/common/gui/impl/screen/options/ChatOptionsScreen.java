package win.blade.common.gui.impl.screen.options;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.NarratorMode;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.network.message.ChatVisibility;
import net.minecraft.text.Text;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import win.blade.common.gui.button.Button;
import win.blade.common.gui.button.Slider;
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.builders.states.SizeState;
import win.blade.common.utils.render.msdf.FontType;
import win.blade.common.utils.render.msdf.MsdfFont;
import win.blade.common.utils.render.renderers.impl.BuiltTexture;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Автор: NoCap
 * Дата создания: 01.08.2025
 */
public class ChatOptionsScreen extends Screen {

    private final Screen parent;
    private final GameOptions options;

    private static final List<ChatVisibility> CHAT_VISIBILITY_MODES = Arrays.asList(ChatVisibility.values());
    private static final List<NarratorMode> NARRATOR_MODES = Arrays.asList(NarratorMode.values());

    private Button chatVisibilityButton;
    private Button narratorButton;
    private Button chatColorsButton;
    private Button chatLinksButton;
    private Button chatLinksPromptButton;
    private Button autoSuggestionsButton;
    private Button hideMatchedNamesButton;
    private Button onlyShowSecureChatButton;
    private Slider chatOpacitySlider;
    private Slider textBackgroundOpacitySlider;
    private Slider chatScaleSlider;
    private Slider chatWidthSlider;
    private Slider focusedHeightSlider;
    private Slider unfocusedHeightSlider;
    private Slider chatLineSpacingSlider;
    private Slider chatDelaySlider;
    private boolean bg;

    public ChatOptionsScreen(Screen parent, GameOptions options, boolean bg) {
        super(Text.translatable("options.chat.title"));
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

        this.chatVisibilityButton = new Button(col1X, currentY, buttonWidth, buttonHeight, getChatVisibilityText(), this::cycleChatVisibility);
        this.addDrawableChild(this.chatVisibilityButton);
        this.narratorButton = new Button(col2X, currentY, buttonWidth, buttonHeight, getNarratorText(), this::cycleNarratorMode);
        this.addDrawableChild(this.narratorButton);
        currentY += buttonHeight + rowGap;

        this.chatColorsButton = new Button(col1X, currentY, buttonWidth, buttonHeight, getChatColorsText(), this::toggleChatColors);
        this.addDrawableChild(this.chatColorsButton);
        this.chatLinksButton = new Button(col2X, currentY, buttonWidth, buttonHeight, getChatLinksText(), this::toggleChatLinks);
        this.addDrawableChild(this.chatLinksButton);
        currentY += buttonHeight + rowGap;

        this.chatLinksPromptButton = new Button(col1X, currentY, buttonWidth, buttonHeight, getChatLinksPromptText(), this::toggleChatLinksPrompt);
        this.addDrawableChild(this.chatLinksPromptButton);
        this.onlyShowSecureChatButton = new Button(col2X, currentY, buttonWidth, buttonHeight, getOnlyShowSecureChatText(), this::toggleOnlyShowSecureChat);
        this.addDrawableChild(this.onlyShowSecureChatButton);
        currentY += buttonHeight + rowGap;

        this.autoSuggestionsButton = new Button(col1X, currentY, buttonWidth, buttonHeight, getAutoSuggestionsText(), this::toggleAutoSuggestions);
        this.addDrawableChild(this.autoSuggestionsButton);
        this.hideMatchedNamesButton = new Button(col2X, currentY, buttonWidth, buttonHeight, getHideMatchedNamesText(), this::toggleHideMatchedNames);
        this.addDrawableChild(this.hideMatchedNamesButton);
        currentY += buttonHeight + rowGap;

        this.chatOpacitySlider = new Slider(col1X, currentY, buttonWidth, buttonHeight, getChatOpacityText(), this.options.getChatOpacity().getValue(), this::onChatOpacityChange);
        this.addDrawableChild(this.chatOpacitySlider);
        this.textBackgroundOpacitySlider = new Slider(col2X, currentY, buttonWidth, buttonHeight, getTextBackgroundOpacityText(), this.options.getTextBackgroundOpacity().getValue(), this::onTextBackgroundOpacityChange);
        this.addDrawableChild(this.textBackgroundOpacitySlider);
        currentY += buttonHeight + rowGap;

        this.chatScaleSlider = new Slider(col1X, currentY, buttonWidth, buttonHeight, getChatScaleText(), this.options.getChatScale().getValue(), this::onChatScaleChange);
        this.addDrawableChild(this.chatScaleSlider);
        this.chatWidthSlider = new Slider(col2X, currentY, buttonWidth, buttonHeight, getChatWidthText(), this.options.getChatWidth().getValue(), this::onChatWidthChange);
        this.addDrawableChild(this.chatWidthSlider);
        currentY += buttonHeight + rowGap;

        this.focusedHeightSlider = new Slider(col1X, currentY, buttonWidth, buttonHeight, getFocusedHeightText(), this.options.getChatHeightFocused().getValue(), this::onFocusedHeightChange);
        this.addDrawableChild(this.focusedHeightSlider);
        this.unfocusedHeightSlider = new Slider(col2X, currentY, buttonWidth, buttonHeight, getUnfocusedHeightText(), this.options.getChatHeightUnfocused().getValue(), this::onUnfocusedHeightChange);
        this.addDrawableChild(this.unfocusedHeightSlider);
        currentY += buttonHeight + rowGap;

        this.chatLineSpacingSlider = new Slider(col1X, currentY, buttonWidth, buttonHeight, getChatLineSpacingText(), this.options.getChatLineSpacing().getValue(), this::onChatLineSpacingChange);
        this.addDrawableChild(this.chatLineSpacingSlider);
        this.chatDelaySlider = new Slider(col2X, currentY, buttonWidth, buttonHeight, getChatDelayText(), this.options.getChatDelay().getValue() / 6.0, this::onChatDelayChange);
        this.addDrawableChild(this.chatDelaySlider);

        this.addDrawableChild(new Button(
                centerX - buttonWidth / 2,
                currentY + buttonHeight + 15,
                buttonWidth,
                buttonHeight,
                ScreenTexts.DONE,
                this::close)
        );
    }


    private void cycleChatVisibility() {
        ChatVisibility current = this.options.getChatVisibility().getValue();
        int nextIndex = (CHAT_VISIBILITY_MODES.indexOf(current) + 1) % CHAT_VISIBILITY_MODES.size();
        this.options.getChatVisibility().setValue(CHAT_VISIBILITY_MODES.get(nextIndex));
        this.chatVisibilityButton.setMessage(getChatVisibilityText());
    }

    private void cycleNarratorMode() {
        NarratorMode current = this.options.getNarrator().getValue();
        int nextIndex = (NARRATOR_MODES.indexOf(current) + 1) % NARRATOR_MODES.size();
        this.options.getNarrator().setValue(NARRATOR_MODES.get(nextIndex));
        this.narratorButton.setMessage(getNarratorText());
    }

    private void toggleChatColors() {
        this.options.getChatColors().setValue(!this.options.getChatColors().getValue());
        this.chatColorsButton.setMessage(getChatColorsText());
    }

    private void toggleChatLinks() {
        this.options.getChatLinks().setValue(!this.options.getChatLinks().getValue());
        this.chatLinksButton.setMessage(getChatLinksText());
    }

    private void toggleChatLinksPrompt() {
        this.options.getChatLinksPrompt().setValue(!this.options.getChatLinksPrompt().getValue());
        this.chatLinksPromptButton.setMessage(getChatLinksPromptText());
    }

    private void toggleAutoSuggestions() {
        this.options.getAutoSuggestions().setValue(!this.options.getAutoSuggestions().getValue());
        this.autoSuggestionsButton.setMessage(getAutoSuggestionsText());
    }

    private void toggleHideMatchedNames() {
        this.options.getHideMatchedNames().setValue(!this.options.getHideMatchedNames().getValue());
        this.hideMatchedNamesButton.setMessage(getHideMatchedNamesText());
    }

    private void toggleOnlyShowSecureChat() {
        this.options.getOnlyShowSecureChat().setValue(!this.options.getOnlyShowSecureChat().getValue());
        this.onlyShowSecureChatButton.setMessage(getOnlyShowSecureChatText());
    }

    private void onChatOpacityChange(double value) {
        this.options.getChatOpacity().setValue(value);
        this.client.inGameHud.getChatHud().reset();
        this.chatOpacitySlider.setMessage(getChatOpacityText());
    }

    private void onTextBackgroundOpacityChange(double value) {
        this.options.getTextBackgroundOpacity().setValue(value);
        this.client.inGameHud.getChatHud().reset();
        this.textBackgroundOpacitySlider.setMessage(getTextBackgroundOpacityText());
    }

    private void onChatScaleChange(double value) {
        this.options.getChatScale().setValue(value);
        this.client.inGameHud.getChatHud().reset();
        this.chatScaleSlider.setMessage(getChatScaleText());
    }

    private void onChatWidthChange(double value) {
        this.options.getChatWidth().setValue(value);
        this.client.inGameHud.getChatHud().reset();
        this.chatWidthSlider.setMessage(getChatWidthText());
    }

    private void onFocusedHeightChange(double value) {
        this.options.getChatHeightFocused().setValue(value);
        this.client.inGameHud.getChatHud().reset();
        this.focusedHeightSlider.setMessage(getFocusedHeightText());
    }

    private void onUnfocusedHeightChange(double value) {
        this.options.getChatHeightUnfocused().setValue(value);
        this.client.inGameHud.getChatHud().reset();
        this.unfocusedHeightSlider.setMessage(getUnfocusedHeightText());
    }

    private void onChatLineSpacingChange(double value) {
        this.options.getChatLineSpacing().setValue(value);
        this.chatLineSpacingSlider.setMessage(getChatLineSpacingText());
    }

    private void onChatDelayChange(double value) {
        this.options.getChatDelay().setValue(value * 6.0);
        this.chatDelaySlider.setMessage(getChatDelayText());
    }

    private Text getChatVisibilityText() {
        return Text.translatable("options.chat.visibility").append(": ").append(Text.translatable(this.options.getChatVisibility().getValue().getTranslationKey()));
    }

    private Text getNarratorText() {
        return Text.translatable("options.narrator").append(": ").append(this.options.getNarrator().getValue().getName());
    }

    private Text getChatColorsText() {
        return ScreenTexts.composeToggleText(Text.translatable("options.chat.color"), this.options.getChatColors().getValue());
    }

    private Text getChatLinksText() {
        return ScreenTexts.composeToggleText(Text.translatable("options.chat.links"), this.options.getChatLinks().getValue());
    }

    private Text getChatLinksPromptText() {
        return ScreenTexts.composeToggleText(Text.translatable("options.chat.links.prompt"), this.options.getChatLinksPrompt().getValue());
    }

    private Text getAutoSuggestionsText() {
        return ScreenTexts.composeToggleText(Text.translatable("options.autoSuggestCommands"), this.options.getAutoSuggestions().getValue());
    }

    private Text getHideMatchedNamesText() {
        return ScreenTexts.composeToggleText(Text.translatable("options.hideMatchedNames"), this.options.getHideMatchedNames().getValue());
    }

    private Text getOnlyShowSecureChatText() {
        return ScreenTexts.composeToggleText(Text.translatable("options.onlyShowSecureChat"), this.options.getOnlyShowSecureChat().getValue());
    }

    private Text getChatOpacityText() {
        double value = this.options.getChatOpacity().getValue();
        int percent = (int)((value * 0.9 + 0.1) * 100.0);
        return Text.translatable("options.chat.opacity").append(": ").append(percent + "%");
    }

    private Text getTextBackgroundOpacityText() {
        double value = this.options.getTextBackgroundOpacity().getValue();
        int percent = (int)(value * 100.0);
        return Text.translatable("options.accessibility.text_background_opacity").append(": ").append(percent + "%");
    }

    private Text getChatScaleText() {
        double value = this.options.getChatScale().getValue();
        return Text.translatable("options.chat.scale").append(": ").append((value == 0.0 ? ScreenTexts.OFF : (int)(value * 100.0) + "%").toString());
    }

    private Text getChatWidthText() {
        double value = this.options.getChatWidth().getValue();
        return Text.translatable("options.chat.width").append(": ").append(ChatHud.getWidth(value) + "px");
    }

    private Text getFocusedHeightText() {
        double value = this.options.getChatHeightFocused().getValue();
        return Text.translatable("options.chat.height.focused").append(": ").append(ChatHud.getHeight(value) + "px");
    }

    private Text getUnfocusedHeightText() {
        double value = this.options.getChatHeightUnfocused().getValue();
        return Text.translatable("options.chat.height.unfocused").append(": ").append(ChatHud.getHeight(value) + "px");
    }

    private Text getChatLineSpacingText() {
        double value = this.options.getChatLineSpacing().getValue();
        return Text.translatable("options.chat.line_spacing").append(": ").append((int)(value * 100.0) + "%");
    }

    private Text getChatDelayText() {
        double value = this.options.getChatDelay().getValue();
        return value <= 0.0 ? Text.translatable("options.chat.delay_none") : Text.translatable("options.chat.delay", String.format(Locale.ROOT, "%.1f", value));
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