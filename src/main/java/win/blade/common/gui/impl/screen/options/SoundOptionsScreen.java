package win.blade.common.gui.impl.screen.options;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import win.blade.common.gui.button.Button;
import win.blade.common.gui.button.Slider;
import win.blade.common.gui.impl.screen.BaseScreen;
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.builders.states.SizeState;
import win.blade.common.utils.render.msdf.FontType;
import win.blade.common.utils.render.msdf.MsdfFont;
import win.blade.common.utils.render.renderers.impl.BuiltTexture;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Автор: NoCap
 * Дата создания: 01.08.2025
 */
public class SoundOptionsScreen extends Screen {

    private final Screen parent;
    private final GameOptions options;

    private Map<SoundCategory, Slider> volumeSliders;

    private Button soundDeviceButton;
    private Button showSubtitlesButton;
    private Button directionalAudioButton;
    private boolean bg;

    public SoundOptionsScreen(Screen parent, GameOptions options, boolean bg) {
        super(Text.translatable("options.sounds.title"));
        this.parent = parent;
        this.options = options;
        this.bg = bg;
    }

    @Override
    protected void init() {
        super.init();

        this.volumeSliders = new HashMap<>();

        int topY = 60;
        int buttonsTopY = topY + 30;
        int buttonWidth = 150;
        int buttonHeight = 32;
        int initialRowGap = 20;
        int rowGap = 4;

        int centerX = this.width / 2;
        int col1X = centerX - buttonWidth - 5 / 2;
        int col2X = centerX + 5 / 2;
        int wideButtonWidth = buttonWidth * 2 + 5;

        int currentY = buttonsTopY;


        Slider masterSlider = new Slider(
                col1X,
                currentY,
                wideButtonWidth,
                buttonHeight,
                getVolumeText(SoundCategory.MASTER),
                this.options.getSoundVolume(SoundCategory.MASTER),
                (value) -> this.onVolumeChange(SoundCategory.MASTER, value)
        );
        this.addDrawableChild(masterSlider);
        this.volumeSliders.put(SoundCategory.MASTER, masterSlider);
        currentY += buttonHeight + rowGap;

        SoundCategory[] categories = SoundCategory.values();
        int sliderIndex = 0;
        for (SoundCategory category : categories) {
            if (category == SoundCategory.MASTER) {
                continue;
            }

            int x = (sliderIndex % 2 == 0) ? col1X : col2X;
            final SoundCategory currentCategory = category;

            Slider categorySlider = new Slider(
                    x,
                    currentY,
                    buttonWidth,
                    buttonHeight,
                    getVolumeText(currentCategory),
                    this.options.getSoundVolume(currentCategory),
                    (value) -> this.onVolumeChange(currentCategory, value)
            );
            this.addDrawableChild(categorySlider);
            this.volumeSliders.put(currentCategory, categorySlider);

            if (sliderIndex % 2 == 1) {
                currentY += buttonHeight + rowGap;
            }
            sliderIndex++;
        }

        if (sliderIndex % 2 != 0) {
            currentY += buttonHeight;
        } else {
            currentY -= rowGap;
        }

        currentY += initialRowGap;

        this.soundDeviceButton = new Button(col1X, currentY, wideButtonWidth, buttonHeight, getSoundDeviceText(), this::cycleSoundDevice);
        this.addDrawableChild(this.soundDeviceButton);
        currentY += buttonHeight + rowGap;

        this.showSubtitlesButton = new Button(col1X, currentY, buttonWidth, buttonHeight, getShowSubtitlesText(), this::toggleShowSubtitles);
        this.addDrawableChild(this.showSubtitlesButton);

        this.directionalAudioButton = new Button(col2X, currentY, buttonWidth, buttonHeight, getDirectionalAudioText(), this::toggleDirectionalAudio);
        this.addDrawableChild(this.directionalAudioButton);

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

    private void onVolumeChange(SoundCategory category, double value) {
        this.options.getSoundVolumeOption(category).setValue(value);

        Slider slider = this.volumeSliders.get(category);

        if (slider != null) {
            slider.setMessage(getVolumeText(category));
        }
    }

    private void cycleSoundDevice() {
        SoundManager soundManager = this.client.getSoundManager();
        List<String> availableDevices = new ArrayList<>(soundManager.getSoundDevices());
        availableDevices.add(0, "");

        String currentDevice = this.options.getSoundDevice().getValue();
        int currentIndex = availableDevices.indexOf(currentDevice);
        int nextIndex = (currentIndex + 1) % availableDevices.size();

        String nextDevice = availableDevices.get(nextIndex);
        this.options.getSoundDevice().setValue(nextDevice);

        soundManager.reloadSounds();
        soundManager.play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));

        this.soundDeviceButton.setMessage(getSoundDeviceText());
    }

    private void toggleShowSubtitles() {
        this.options.getShowSubtitles().setValue(!this.options.getShowSubtitles().getValue());
        this.showSubtitlesButton.setMessage(getShowSubtitlesText());
    }

    private void toggleDirectionalAudio() {
        this.options.getDirectionalAudio().setValue(!this.options.getDirectionalAudio().getValue());

        SoundManager soundManager = this.client.getSoundManager();
        soundManager.reloadSounds();
        soundManager.play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));

        this.directionalAudioButton.setMessage(getDirectionalAudioText());
    }

    private Text getVolumeText(SoundCategory category) {
        float volume = this.options.getSoundVolume(category);
        if (volume == 0.0f) {
            return Text.translatable("soundCategory." + category.getName()).append(": ").append(ScreenTexts.OFF);
        }
        return Text.translatable("soundCategory." + category.getName()).append(": " + (int) (volume * 100) + "%");
    }

    private Text getSoundDeviceText() {
        String device = this.options.getSoundDevice().getValue();
        if (device.isEmpty()) {
            return Text.translatable("options.audioDevice").append(": ").append(Text.translatable("options.audioDevice.default"));
        }
        return Text.translatable("options.audioDevice").append(": ").append(Text.literal(device));
    }


    private Text getShowSubtitlesText() {
        return ScreenTexts.composeToggleText(Text.translatable("options.showSubtitles"), this.options.getShowSubtitles().getValue());
    }

    private Text getDirectionalAudioText() {
        return ScreenTexts.composeToggleText(Text.translatable("options.directionalAudio"), this.options.getDirectionalAudio().getValue());
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