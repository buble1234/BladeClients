package win.blade.common.ui.element.hud;

import dev.redstones.mediaplayerinfo.IMediaSession;
import dev.redstones.mediaplayerinfo.MediaPlayerInfo;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import win.blade.common.ui.element.InteractiveUIElement;
import win.blade.common.utils.color.ColorUtility;
import win.blade.common.utils.math.TimerUtil;
import win.blade.common.utils.math.animation.Animation;
import win.blade.common.utils.math.animation.Easing;
import win.blade.common.utils.minecraft.MinecraftInstance;
import win.blade.common.utils.render.Stencil;
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.builders.states.QuadColorState;
import win.blade.common.utils.render.builders.states.QuadRadiusState;
import win.blade.common.utils.render.builders.states.SizeState;
import win.blade.common.utils.render.msdf.FontType;
import win.blade.common.utils.render.renderers.impl.BuiltRectangle;
import win.blade.core.event.controllers.EventHandler;
import win.blade.core.event.impl.input.InputEvents;
import win.blade.core.event.impl.render.RenderEvents;
import win.blade.core.module.api.Category;
import win.blade.core.module.api.Module;
import win.blade.core.module.api.ModuleInfo;
import win.blade.core.module.api.NonRegistrable;
import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.List;

@ModuleInfo(
        name = "Music",
        category = Category.RENDER,
        desc = "Отображает информацию о проигрываемой музыке"
)
public class MusicHud extends Module implements MinecraftInstance, NonRegistrable {

    private wUIElement wUIElement;

    public MusicHud() {
        this.wUIElement = new MusicHud.wUIElement("MusicHud", 10, 35, 100, 45);
        this.wUIElement.setAnimation(0.4, Easing.EASE_OUT_CUBIC);
    }

    @EventHandler
    public void onRenderScreen(RenderEvents.Screen.POST e) {
        if (e == null || e.getDrawContext() == null || mc.player == null) {
            return;
        }

        wUIElement.update(true);
        wUIElement.render(e.getDrawContext());
    }

    @EventHandler
    public void onMouse(InputEvents.Mouse event) {
        if (wUIElement != null) {
            wUIElement.onMouse(event);
        }
    }

    private class wUIElement extends InteractiveUIElement {
        private final MediaPlayerInfo mediaPlayerInfo = MediaPlayerInfo.Instance;
        private final TimerUtil updateTimer = new TimerUtil();
        private final Animation scaleAnimation = new Animation();
        private final Animation widthAnimation = new Animation(100);

        private String trackName = "Ожидаю...";
        private String artist = "";
        private long posit = 0;
        private long durat = 0;
        private boolean sessionActive = true;
        private Identifier playerIcon;
        private byte[] artworkBytes;
        private byte[] lastArtworkBytes;
        private Identifier artworkTexture;
        private IMediaSession session;

        public wUIElement(String id, float x, float y, float width, float height) {
            super(id, x, y, width, height);
            this.playerIcon = Identifier.of("blade", "textures/svg/music/untitled.svg");
        }

        @Override
        public void renderContent(DrawContext context) {
            if (updateTimer.hasReached(1000)) {
                updateTimer.reset();
                updateTrackTitle();
            }

            boolean isInChat = mc.currentScreen instanceof ChatScreen;
            boolean hasMusic = !sessionActive || isInChat;
            boolean shouldShow = hasMusic || isInChat;

            if (shouldShow && scaleAnimation.getToValue() != 1.0) {
                scaleAnimation.run(1.0, 0.5, Easing.EASE_OUT_BACK);
                if (hasMusic) {
                    widthAnimation.run(122, 0.5, Easing.EASE_OUT_BACK);
                } else {
                    widthAnimation.run(100, 0.5, Easing.EASE_OUT_BACK);
                }
            } else if (!shouldShow && scaleAnimation.getToValue() != 0.0) {
                scaleAnimation.run(0.0, 0.5, Easing.EASE_IN_BACK);
                widthAnimation.run(100, 0.5, Easing.EASE_IN_BACK);
            }

            if (sessionActive && !hasMusic && widthAnimation.getToValue() != 100) {
                widthAnimation.run(100, 0.5, Easing.EASE_OUT_BACK);
            } else if (!sessionActive && widthAnimation.getToValue() != 122) {
                widthAnimation.run(122, 0.5, Easing.EASE_OUT_BACK);
            }

            scaleAnimation.update();
            widthAnimation.update();

            if (scaleAnimation.get() <= 0.01f) return;

            Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();

            float scale = scaleAnimation.get();
            float animatedWidth = widthAnimation.get();

            float centerX = getX() + getWidth() / 2f;
            float centerY = getY() + getHeight() / 2f;

            context.getMatrices().push();
            context.getMatrices().translate(centerX, centerY, 0);
            context.getMatrices().scale(scale, scale, 1f);
            context.getMatrices().translate(-centerX, -centerY, 0);

            matrix = context.getMatrices().peek().getPositionMatrix();

            setWidth(animatedWidth);
            setHeight(47);

            BuiltRectangle background = Builder.rectangle()
                    .size(new SizeState(getWidth(), getHeight()))
                    .color(new QuadColorState(
                            new Color(23, 20, 35),
                            new Color(20, 18, 27),
                            new Color(20, 18, 27),
                            new Color(19, 17, 29)
                    ))
                    .radius(new QuadRadiusState(6.5f))
                    .smoothness(1.0f)
                    .build();
            Stencil.push();
            background.render(matrix, getX(), getY());
            Stencil.read(1);
            background.render(matrix, getX(), getY());

            String displayText;
            String artistText;
            long displayPosition;
            long displayDuration;
            boolean showArtwork;
            boolean showControls;

            if (isInChat && sessionActive) {
                displayText = "Example Song";
                artistText = "Example Artist";
                displayPosition = 45;
                displayDuration = 180;
                showArtwork = true;
                showControls = true;
            } else if (!sessionActive) {
                displayText = trackName;
                artistText = artist;
                displayPosition = posit;
                displayDuration = durat;
                showArtwork = true;
                showControls = true;
            } else {
                displayText = "Ожидаю...";
                artistText = "";
                displayPosition = 0;
                displayDuration = 0;
                showArtwork = false;
                showControls = false;
            }

            if (showArtwork) {
                renderArtwork(context, getX(), getY(), isInChat && sessionActive);
            }

            float textStartX = showArtwork ? getX() + 32 : getX() + 8;

            Builder.text().font(FontType.sf_medium.get()).text(displayText).color(-1).size(6).build()
                    .render(matrix, textStartX, getY() + 8);

            if (artistText != null && !artistText.isEmpty()) {
                Builder.text().font(FontType.sf_regular.get()).text(artistText).color(ColorUtility.fromHex("EEEEEE")).size(5).build()
                        .render(matrix, textStartX, getY() + 16);
            }

            float progressBarY = getY() + 37;

            BuiltRectangle progressBarBg = Builder.rectangle()
                    .size(new SizeState(getWidth() - 13, 3))
                    .color(new QuadColorState(ColorUtility.fromHex("1C1A25")))
                    .radius(new QuadRadiusState(0.5f))
                    .build();
            progressBarBg.render(matrix, getX() + 6.5f, progressBarY);

            float progress = getProgressValue(displayPosition, displayDuration);
            if (progress > 0) {
                BuiltRectangle progressBar = Builder.rectangle()
                        .size(new SizeState((getWidth() - 13) * progress, 3))
                        .color(new QuadColorState(ColorUtility.fromHex("663CFF")))
                        .radius(new QuadRadiusState(0.5f))
                        .build();
                progressBar.render(matrix, getX() + 6.5f, progressBarY);
            }

            Builder.text().font(FontType.sf_regular.get()).text(formatDuration(displayPosition)).color(ColorUtility.fromHex("EEEEEE")).size(5).build()
                    .render(matrix, getX() + 7, progressBarY - 8);

            float allTimeWidth = FontType.sf_regular.get().getWidth(formatDuration(displayDuration), 5);
            Builder.text().font(FontType.sf_regular.get()).text(formatDuration(displayDuration)).color(ColorUtility.fromHex("EEEEEE")).size(5).build()
                    .render(matrix, getX() + getWidth() - allTimeWidth - 7.5f, progressBarY - 8);

            Identifier currentPlayerIcon = (isInChat && sessionActive) ?
                    Identifier.of("blade", "textures/svg/music/spotify.svg") : playerIcon;

            if (showArtwork && currentPlayerIcon != null) {
                Builder.texture()
                        .size(new SizeState(10, 10))
                        .color(new QuadColorState(Color.WHITE))
                        .svgTexture(0f, 0f, 1f, 1f, currentPlayerIcon)
                        .radius(new QuadRadiusState(0f))
                        .build()
                        .render(matrix, getX() + getWidth() - 18, getY() + 6);
            }

            if (showControls) {
                float buttonSize = 6;
                float buttonGap = 5.5f;
                float totalControlsWidth = buttonSize * 3 + buttonGap * 2;
                float controlsStartX = getX() + (getWidth() - totalControlsWidth) / 2f;
                float controlsY = getY() + getHeight() - buttonSize - 13.5f;

                Builder.texture()
                        .size(new SizeState(buttonSize, buttonSize))
                        .svgTexture(0,0,1,1, Identifier.of("blade", "textures/svg/music/back.svg"))
                        .color(new QuadColorState(Color.WHITE))
                        .build().render(matrix, controlsStartX, controlsY);

                Identifier playPauseIcon = (session != null && session.getMedia().getPlaying()) || (isInChat && sessionActive) ?
                        Identifier.of("blade", "textures/svg/music/pause.svg") :
                        Identifier.of("blade", "textures/svg/music/play.svg");
                Builder.texture()
                        .size(new SizeState(buttonSize, buttonSize))
                        .svgTexture(0,0,1,1, playPauseIcon)
                        .color(new QuadColorState(Color.WHITE))
                        .build().render(matrix, controlsStartX + buttonSize + buttonGap, controlsY);

                Builder.texture()
                        .size(new SizeState(buttonSize, buttonSize))
                        .svgTexture(0,0,1,1, Identifier.of("blade", "textures/svg/music/next.svg"))
                        .color(new QuadColorState(Color.WHITE))
                        .build().render(matrix, controlsStartX + buttonSize * 2 + buttonGap * 2, controlsY);
            }

            Stencil.pop();
            Builder.border()
                    .size(new SizeState(getWidth(), getHeight()))
                    .color(new QuadColorState(new Color(170, 160, 200, 25)))
                    .radius(new QuadRadiusState(6.5f))
                    .thickness(1)
                    .build()
                    .render(matrix, getX(), getY());

            context.getMatrices().pop();
        }

        private void renderArtwork(DrawContext context, float x, float y, boolean isExample) {
            float artworkSize = 20.5f;
            float artworkX = x + 6.5f;
            float artworkY = y + 5.5f;

            if (isExample) {
                BuiltRectangle exampleArtwork = Builder.rectangle()
                        .size(new SizeState(artworkSize, artworkSize))
                        .color(new QuadColorState(ColorUtility.fromHex("663CFF")))
                        .radius(new QuadRadiusState(3f))
                        .build();
                exampleArtwork.render(context.getMatrices().peek().getPositionMatrix(), artworkX, artworkY);

                Builder.text().font(FontType.sf_medium.get()).text("♪").color(-1).size(8).build()
                        .render(context.getMatrices().peek().getPositionMatrix(), artworkX + 7.5f, artworkY + 6);
                return;
            }

            if (artworkBytes != null && artworkBytes.length > 0) {
                if (lastArtworkBytes == null || !Arrays.equals(artworkBytes, lastArtworkBytes)) {
                    try {
                        if (artworkTexture != null) {
                            mc.getTextureManager().destroyTexture(artworkTexture);
                        }

                        NativeImage nativeImage = NativeImage.read(new ByteArrayInputStream(artworkBytes));
                        NativeImageBackedTexture dynamicTexture = new NativeImageBackedTexture(nativeImage);

                        artworkTexture = Identifier.of("blade", "music_artwork/" + System.currentTimeMillis());
                        mc.getTextureManager().registerTexture(artworkTexture, dynamicTexture);

                        lastArtworkBytes = artworkBytes.clone();
                    } catch (Exception e) {
                        artworkTexture = null;
                        e.printStackTrace();
                    }
                }

                if (artworkTexture != null) {
                    AbstractTexture texture = mc.getTextureManager().getTexture(artworkTexture);
                    if (texture != null) {
                        Builder.texture()
                                .size(new SizeState(artworkSize, artworkSize))
                                .texture(0.0f, 0.0f, 1.0f, 1.0f, texture)
                                .radius(new QuadRadiusState(3f))
                                .build()
                                .render(context.getMatrices().peek().getPositionMatrix(), artworkX, artworkY);
                    }
                }
            } else {
                if (artworkTexture != null) {
                    mc.getTextureManager().destroyTexture(artworkTexture);
                    artworkTexture = null;
                    lastArtworkBytes = null;
                }
            }
        }

        private float getProgressValue(long position, long duration) {
            if (duration > 0) {
                float progress = (float) position / (float) duration;
                return Math.max(0.0f, Math.min(1.0f, progress));
            }
            return 0.0F;
        }

        private void updateTrackTitle() {
            new Thread(() -> {
                try {
                    List<IMediaSession> sessions = mediaPlayerInfo.getMediaSessions();
                    sessionActive = sessions == null || sessions.isEmpty();

                    if (!sessionActive) {
                        session = sessions.get(0);

                        trackName = session.getMedia() == null || session.getMedia().getTitle() == null ? "Нет данных" : session.getMedia().getTitle();
                        artist = session.getMedia() == null || session.getMedia().getArtist() == null ? "" : session.getMedia().getArtist();
                        posit = session.getMedia() == null ? 0 : session.getMedia().getPosition();
                        durat = session.getMedia() == null ? 0 : session.getMedia().getDuration();

                        if (session.getMedia() != null && session.getMedia().getArtwork() != null) {
                            artworkBytes = session.getMedia().getArtworkPng();
                        } else {
                            artworkBytes = null;
                        }

                        String owner = session.getOwner().toLowerCase();
                        if (owner.contains("spotify")) {
                            playerIcon = Identifier.of("blade", "textures/svg/music/spotify.svg");
                        } else if (owner.contains("yandex")) {
                            playerIcon = Identifier.of("blade", "textures/svg/music/yandex.svg");
                        } else if (owner.contains("soundcloud")) {
                            playerIcon = Identifier.of("blade", "textures/svg/music/sc.svg");
                        } else {
                            playerIcon = Identifier.of("blade", "textures/svg/music/untitled.svg");
                        }

                    } else {
                        trackName = "Ожидаю...";
                        artist = "";
                        posit = 0;
                        durat = 0;
                        artworkBytes = null;
                        playerIcon = null;
                        session = null;
                    }
                } catch (Exception e) {
                    trackName = "Ошибка";
                    artist = "";
                    artworkBytes = null;
                    playerIcon = Identifier.of("blade", "textures/svg/music/untitled.svg");
                    session = null;
                }
            }).start();
        }

        private String formatDuration(long duration) {
            if (duration < 0) return "00:00";
            long seconds = duration % 60;
            long minutes = (duration / 60) % 60;
            return String.format("%02d:%02d", minutes, seconds);
        }

        private float getProgress() {
            if (!sessionActive && durat > 0) {
                float progress = (float) posit / (float) durat;
                return Math.max(0.0f, Math.min(1.0f, progress));
            }
            return 0.0F;
        }

        @Override
        public void update() {
            super.update();
        }

        @Override
        public void onMouse(InputEvents.Mouse event) {
            super.onMouse(event);

            if (mc.currentScreen == null) {
                return;
            }

            if (event.getAction() == 1 && event.getButton() == 0 && session != null) {
                double mouseX = event.getX();
                double mouseY = event.getY();

                boolean isHoveringWidget = mouseX >= getX() && mouseX <= getX() + getWidth() &&
                        mouseY >= getY() && mouseY <= getY() + getHeight();

                if (isHoveringWidget) {
                    float buttonSize = 6;
                    float buttonGap = 5.5f;
                    float totalControlsWidth = buttonSize * 3 + buttonGap * 2;
                    float controlsStartX = getX() + (getWidth() - totalControlsWidth) / 2f;
                    float controlsY = getY() + getHeight() - buttonSize - 13.5f;

                    float backX = controlsStartX;
                    float playPauseX = controlsStartX + buttonSize + buttonGap;
                    float nextX = controlsStartX + buttonSize * 2 + buttonGap * 2;

                    if (mouseX >= backX && mouseX <= backX + buttonSize && mouseY >= controlsY && mouseY <= controlsY + buttonSize) {
                        new Thread(session::previous).start();
                    } else if (mouseX >= playPauseX && mouseX <= playPauseX + buttonSize && mouseY >= controlsY && mouseY <= controlsY + buttonSize) {
                        new Thread(session::playPause).start();
                    } else if (mouseX >= nextX && mouseX <= nextX + buttonSize && mouseY >= controlsY && mouseY <= controlsY + buttonSize) {
                        new Thread(session::next).start();
                    }
                }
            }
        }
    }
}
