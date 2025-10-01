package win.blade.common.ui.element.hud;

import dev.redstones.mediaplayerinfo.IMediaSession;
import dev.redstones.mediaplayerinfo.MediaInfo;
import dev.redstones.mediaplayerinfo.MediaPlayerInfo;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
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
import win.blade.common.utils.render.ScissorManager;
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
import java.util.Comparator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@ModuleInfo(name = "Music", category = Category.RENDER, desc = "Отображает информацию о проигрываемой музыке")
public class MusicHud extends Module implements MinecraftInstance, NonRegistrable {

    public static MusicHud getInstance() {
        return new MusicHud();
    }

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final wUIElement wUIElement = new wUIElement("MusicHud", 10, 35, 122, 47);
    private MediaInfo mediaInfo = new MediaInfo("Example Song", "Example Artist", new byte[0], 45, 180, false);
    private final TimerUtil lastMedia = new TimerUtil();
    public IMediaSession session;
    private Identifier playerIcon = Identifier.of("blade", "textures/svg/music/untitled.svg");
    private Identifier artworkTexture;
    private byte[] lastArtworkBytes;

    @EventHandler
    public void onRenderScreen(RenderEvents.Screen.POST e) {
        if (e.getDrawContext() == null || mc.player == null) return;
        wUIElement.update(visible());
        wUIElement.render(e.getDrawContext());
    }

    @EventHandler
    public void onMouse(InputEvents.Mouse event) {
        if (wUIElement != null) {
            wUIElement.onMouse(event);
        }
    }

    private boolean visible() {
        return !lastMedia.hasReached(2000) || mc.currentScreen instanceof ChatScreen;
    }

    private void tick() {
        if (mc.player.age % 20 == 0) executorService.submit(() -> {
            IMediaSession currentSession = session = MediaPlayerInfo.Instance.getMediaSessions().stream()
                    .max(Comparator.comparing(s -> s.getMedia().getPlaying())).orElse(null);
            if (currentSession != null) {
                MediaInfo info = currentSession.getMedia();
                if (!info.getTitle().isEmpty() || !info.getArtist().isEmpty()) {
                    if (mediaInfo.getTitle().equals("Example Song") || !Arrays.equals(mediaInfo.getArtworkPng(), info.getArtworkPng())) {
                        updateArtwork(info.getArtworkPng());
                        updatePlayerIcon(currentSession.getOwner().toLowerCase());
                    }
                    mediaInfo = info;
                    lastMedia.reset();
                }
            }
        });
    }

    private void updateArtwork(byte[] artworkBytes) {
        if (artworkBytes != null && artworkBytes.length > 0 && !Arrays.equals(artworkBytes, lastArtworkBytes)) {
            try {
                if (artworkTexture != null) mc.getTextureManager().destroyTexture(artworkTexture);
                NativeImage nativeImage = NativeImage.read(new ByteArrayInputStream(artworkBytes));
                artworkTexture = Identifier.of("blade", "music_artwork/" + System.currentTimeMillis());
                mc.getTextureManager().registerTexture(artworkTexture, new NativeImageBackedTexture(nativeImage));
                lastArtworkBytes = artworkBytes.clone();
            } catch (Exception ignored) {}
        }
    }

    private void updatePlayerIcon(String owner) {
        playerIcon = owner.contains("spotify") ? Identifier.of("blade", "textures/svg/music/spotify.svg") :
                owner.contains("yandex") ? Identifier.of("blade", "textures/svg/music/yandex.svg") :
                        owner.contains("soundcloud") ? Identifier.of("blade", "textures/svg/music/sc.svg") :
                                Identifier.of("blade", "textures/svg/music/untitled.svg");
    }

    private class wUIElement extends InteractiveUIElement {
        private final Animation scaleAnimation = new Animation();

        public wUIElement(String id, float x, float y, float width, float height) {
            super(id, x, y, width, height);
            setAnimation(0.4, Easing.EASE_OUT_CUBIC);
        }

        public void update(boolean shouldShow) {
            super.update(shouldShow);
            tick();
            if (shouldShow && scaleAnimation.getToValue() != 1.0) {
                scaleAnimation.run(1.0, 0.5, Easing.EASE_OUT_BACK);
            } else if (!shouldShow && scaleAnimation.getToValue() != 0.0) {
                scaleAnimation.run(0.0, 0.5, Easing.EASE_IN_BACK);
            }
            scaleAnimation.update();
            setWidth(122);
        }

        @Override
        public void renderContent(DrawContext context) {
            if (scaleAnimation.get() <= 0.01f) return;

            Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();
            float scale = scaleAnimation.get();
            float centerX = getX() + getWidth() / 2f;
            float centerY = getY() + getHeight() / 2f;

            context.getMatrices().push();
            context.getMatrices().translate(centerX, centerY, 0);
            context.getMatrices().scale(scale, scale, 1f);
            context.getMatrices().translate(-centerX, -centerY, 0);
            matrix = context.getMatrices().peek().getPositionMatrix();

            drawBackground(matrix);
            drawContent(matrix);
            drawBorder(matrix);

            context.getMatrices().pop();
        }

        private void drawBackground(Matrix4f matrix) {
            BuiltRectangle background = Builder.rectangle()
                    .size(new SizeState(getWidth(), getHeight()))
                    .color(new QuadColorState(new Color(23, 20, 35), new Color(20, 18, 27), new Color(20, 18, 27), new Color(19, 17, 29)))
                    .radius(new QuadRadiusState(6.5f))
                    .smoothness(1.0f)
                    .build();
            ScissorManager.push(matrix, getX(), getY(), getWidth(), getHeight());
            background.render(matrix, getX(), getY());
            background.render(matrix, getX(), getY());
        }

        private void drawContent(Matrix4f matrix) {
            boolean hasSession = session != null;
            int sizeArtwork = 21;
            int sizeButton = 6;

            String title = hasSession ? mediaInfo.getTitle() : "Example Song";
            String artist = hasSession ? mediaInfo.getArtist() : "Example Artist";
            long position = hasSession ? mediaInfo.getPosition() : 45;
            long duration = hasSession ? mediaInfo.getDuration() : 180;

            drawArtwork(matrix, !hasSession, sizeArtwork);

            float textX = getX() + 32;
            Builder.text().font(FontType.sf_medium.get()).text(title).color(-1).size(6).build().render(matrix, textX, getY() + 8);
            if (!artist.isEmpty()) {
                Builder.text().font(FontType.sf_regular.get()).text(artist).color(ColorUtility.fromHex("EEEEEE")).size(5).build().render(matrix, textX, getY() + 16);
            }

            drawProgressBar(matrix, position, duration);
            drawTimeLabels(matrix, position, duration);
            drawPlayerIcon(matrix);
            drawControls(matrix, sizeButton);
        }

        private void drawArtwork(Matrix4f matrix, boolean isExample, int sizeArtwork) {
            float artworkX = getX() + 6.5f;
            float artworkY = getY() + 5.5f;

            if (isExample) {
                Builder.rectangle().size(new SizeState(sizeArtwork, sizeArtwork)).color(new QuadColorState(ColorUtility.fromHex("663CFF")))
                        .radius(new QuadRadiusState(3f)).build().render(matrix, artworkX, artworkY);
            } else if (artworkTexture != null && mc.getTextureManager().getTexture(artworkTexture) != null) {
                Builder.texture().size(new SizeState(sizeArtwork, sizeArtwork))
                        .texture(0.0f, 0.0f, 1.0f, 1.0f, mc.getTextureManager().getTexture(artworkTexture))
                        .radius(new QuadRadiusState(3f)).build().render(matrix, artworkX, artworkY);
            } else {
                Builder.rectangle().size(new SizeState(sizeArtwork, sizeArtwork)).color(new QuadColorState(ColorUtility.fromHex("2A2A2A")))
                        .radius(new QuadRadiusState(3f)).build().render(matrix, artworkX, artworkY);
            }
        }

        private void drawProgressBar(Matrix4f matrix, long position, long duration) {
            float progressY = getY() + 37;
            float progressWidth = getWidth() - 13;

            Builder.rectangle().size(new SizeState(progressWidth, 3)).color(new QuadColorState(ColorUtility.fromHex("1C1A25")))
                    .radius(new QuadRadiusState(0.5f)).build().render(matrix, getX() + 6.5f, progressY);

            if (duration > 0) {
                float progress = Math.max(0.0f, Math.min(1.0f, (float) position / duration));
                Builder.rectangle().size(new SizeState(progressWidth * progress, 3)).color(new QuadColorState(ColorUtility.fromHex("663CFF")))
                        .radius(new QuadRadiusState(0.5f)).build().render(matrix, getX() + 6.5f, progressY);
            }
        }

        private void drawTimeLabels(Matrix4f matrix, long position, long duration) {
            String positionText = formatTime(position);
            String durationText = formatTime(duration);
            float timeY = getY() + 29;

            Builder.text().font(FontType.sf_regular.get()).text(positionText).color(ColorUtility.fromHex("EEEEEE")).size(5).build()
                    .render(matrix, getX() + 7, timeY);

            float durationWidth = FontType.sf_regular.get().getWidth(durationText, 5);
            Builder.text().font(FontType.sf_regular.get()).text(durationText).color(ColorUtility.fromHex("EEEEEE")).size(5).build()
                    .render(matrix, getX() + getWidth() - durationWidth - 7.5f, timeY);
        }

        private void drawPlayerIcon(Matrix4f matrix) {
            Builder.texture().size(new SizeState(10, 10)).color(new QuadColorState(Color.WHITE))
                    .svgTexture(0f, 0f, 1f, 1f, playerIcon).radius(new QuadRadiusState(0f)).build()
                    .render(matrix, getX() + getWidth() - 18, getY() + 6);
        }

        private void drawControls(Matrix4f matrix, int sizeButton) {
            float buttonGap = 5.5f;
            float controlsX = getX() + (getWidth() - (sizeButton * 3 + buttonGap * 2)) / 2f;
            float controlsY = getY() + getHeight() - sizeButton - 13.5f;

            Builder.texture().size(new SizeState(sizeButton, sizeButton))
                    .svgTexture(0, 0, 1, 1, Identifier.of("blade", "textures/svg/music/back.svg"))
                    .color(new QuadColorState(Color.WHITE)).build().render(matrix, controlsX, controlsY);

            Identifier playIcon = (session != null && session.getMedia().getPlaying()) ?
                    Identifier.of("blade", "textures/svg/music/pause.svg") :
                    Identifier.of("blade", "textures/svg/music/play.svg");
            Builder.texture().size(new SizeState(sizeButton, sizeButton))
                    .svgTexture(0, 0, 1, 1, playIcon).color(new QuadColorState(Color.WHITE)).build()
                    .render(matrix, controlsX + sizeButton + buttonGap, controlsY);

            Builder.texture().size(new SizeState(sizeButton, sizeButton))
                    .svgTexture(0, 0, 1, 1, Identifier.of("blade", "textures/svg/music/next.svg"))
                    .color(new QuadColorState(Color.WHITE)).build()
                    .render(matrix, controlsX + sizeButton * 2 + buttonGap * 2, controlsY);
        }

        private void drawBorder(Matrix4f matrix) {
            Builder.border().size(new SizeState(getWidth(), getHeight()))
                    .color(new QuadColorState(new Color(170, 160, 200, 25)))
                    .radius(new QuadRadiusState(6.5f)).thickness(1).build().render(matrix, getX(), getY());
            ScissorManager.pop();
        }

        private String formatTime(long seconds) {
            if (seconds < 0) return "00:00";
            return String.format("%02d:%02d", (seconds / 60) % 60, seconds % 60);
        }

        @Override
        public void onMouse(InputEvents.Mouse event) {
            super.onMouse(event);

            if (event.getAction() != 1 || event.getButton() != 0 || session == null) return;

            double mouseX = event.getX();
            double mouseY = event.getY();

            if (mouseX < getX() || mouseX > getX() + getWidth() || mouseY < getY() || mouseY > getY() + getHeight()) return;

            int sizeButton = 6;
            float buttonGap = 5.5f;
            float controlsX = getX() + (getWidth() - (sizeButton * 3 + buttonGap * 2)) / 2f;
            float controlsY = getY() + getHeight() - sizeButton - 13.5f;

            if (mouseY >= controlsY && mouseY <= controlsY + sizeButton) {
                if (mouseX >= controlsX && mouseX <= controlsX + sizeButton) {
                    session.previous();
                } else if (mouseX >= controlsX + sizeButton + buttonGap && mouseX <= controlsX + sizeButton * 2 + buttonGap) {
                    session.playPause();
                } else if (mouseX >= controlsX + sizeButton * 2 + buttonGap * 2 && mouseX <= controlsX + sizeButton * 3 + buttonGap * 2) {
                    session.next();
                }
            }
        }
    }
}