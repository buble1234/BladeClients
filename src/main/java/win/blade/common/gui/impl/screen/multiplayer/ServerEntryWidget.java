package win.blade.common.gui.impl.screen.multiplayer;

import com.mojang.logging.LogUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerServerListWidget;
import net.minecraft.client.network.MultiplayerServerListPinger;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.option.ServerList;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.Util;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import win.blade.common.gui.impl.screen.window.EditServerScreen;
import win.blade.common.utils.color.ColorUtility;
import win.blade.common.utils.math.MathUtility;
import win.blade.common.utils.minecraft.MinecraftInstance;
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.builders.impl.TextBuilder;
import win.blade.common.utils.render.builders.states.QuadColorState;
import win.blade.common.utils.render.builders.states.QuadRadiusState;
import win.blade.common.utils.render.builders.states.SizeState;
import win.blade.common.utils.render.msdf.FontType;
import win.blade.common.utils.render.msdf.MsdfFont;
import win.blade.common.utils.render.renderers.impl.BuiltBorder;
import win.blade.common.utils.render.renderers.impl.BuiltRectangle;
import win.blade.common.utils.render.renderers.impl.BuiltTexture;

import java.awt.Color;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

public class ServerEntryWidget implements MinecraftInstance {
    private final MultiplayerScreen screen;
    private final ServerInfo sInfo;
    private final MultiplayerServerListPinger pinger;
    private final ServerList serverList;
    private final ThreadPoolExecutor pingerThreadPool;
    private Identifier iconTextureId;
    private boolean pinged = false;
    private BuiltTexture settingsIcon, trashIcon, placeholderIcon;
    private byte[] lastFaviconBytes;
    public Identifier statusIcon;

    public AbstractTexture pingTexture;

    public ServerEntryWidget(MultiplayerScreen screen, ServerInfo serverInfo, MultiplayerServerListPinger pinger, ServerList serverList, ThreadPoolExecutor pingerThreadPool) {
        this.screen = screen;
        this.sInfo = serverInfo;
        this.pinger = pinger;
        this.serverList = serverList;
        this.pingerThreadPool = pingerThreadPool;
        this.pingTexture = mc.getTextureManager().getTexture(Identifier.of("blade", "textures/mc_icons.png"));

        loadIcons();
    }


    private void loadIcons() {
        AbstractTexture settingTexture = mc.getTextureManager().getTexture(Identifier.of("blade", "textures/settings.png"));
        this.settingsIcon = Builder.texture().size(new SizeState(7, 7)).texture(0, 0, 1, 1, settingTexture).build();

        AbstractTexture trashTexture = mc.getTextureManager().getTexture(Identifier.of("blade", "textures/trashing.png"));
        this.trashIcon = Builder.texture().size(new SizeState(8.5f, 8.5f)).texture(0, 0, 1, 1, trashTexture).build();

        AbstractTexture placeholderTexture = mc.getTextureManager().getTexture(Identifier.of("blade", "textures/noneserver.png"));
        this.placeholderIcon = Builder.texture()
                .size(new SizeState(32, 32))
                .texture(0, 0, 1, 1, placeholderTexture)
                .radius(new QuadRadiusState(6))
                .smoothness(1)
                .build();

    }

    public boolean mouseClicked(double mouseX, double mouseY, int button, int entryX, int entryY) {
        float settingsX = entryX + 245;
        float settingsY = entryY + 25.7f;
        float trashX = entryX + 255;
        float trashY = entryY + 25;

        if (mouseX >= settingsX && mouseX <= settingsX + 7 && mouseY >= settingsY && mouseY <= settingsY + 7) {
            mc.setScreen(new EditServerScreen(this.screen, this.sInfo, this.serverList));
            return true;
        }

        if (mouseX >= trashX && mouseX <= trashX + 8.5f && mouseY >= trashY && mouseY <= trashY + 8.5f) {
            System.out.println("Delete " + sInfo.name);
            screen.deleteEntry(this);
            return true;
        }
        return false;
    }

    public ServerInfo getServerInfo() {
        return this.sInfo;
    }

    private void syncIcon() {
        if (this.iconTextureId != null) {
            mc.getTextureManager().destroyTexture(this.iconTextureId);
            this.iconTextureId = null;
        }

        byte[] favicon = sInfo.getFavicon();
        if (favicon != null) {
            try {
                NativeImage image = NativeImage.read(favicon);
                NativeImageBackedTexture texture = new NativeImageBackedTexture(image);
                this.iconTextureId = Identifier.of("multiplayer_icons/" + sInfo.address.hashCode());
                mc.getTextureManager().registerTexture(this.iconTextureId, texture);
            } catch (IOException e) {
                this.iconTextureId = null;
            }
        }
    }

    public void render(DrawContext context, int index, int mouseX, int mouseY, int x, int y, int width, int height, boolean isSelected) {
        if (!this.pinged) {
            this.pinged = true;
            this.pingerThreadPool.submit(() -> {
                try {
                    this.pinger.add(this.sInfo,
                            () -> mc.execute(this.serverList::saveFile),
                            () -> {}
                    );
                } catch (UnknownHostException e) {
                    mc.execute(() -> {
                        sInfo.label = Text.translatable("multiplayer.status.cannot_resolve").formatted(Formatting.DARK_RED);
                        sInfo.setFavicon(null);
                        serverList.saveFile();
                    });
                }
            });
        }

        var info = getIdentifier(index);

        int i = info.getKey();
        int k = info.getValue();

//        System.out.println(i + " " + k);

//        System.out.println(pingTexture == null);


//        context.drawTexture()


        byte[] currentFavicon = sInfo.getFavicon();
        if (!Arrays.equals(currentFavicon, this.lastFaviconBytes)) {
            syncIcon();
            this.lastFaviconBytes = currentFavicon == null ? null : currentFavicon.clone();
        }

        Color d = new Color(20, 18, 27, 255);
        BuiltRectangle rectangle = Builder.rectangle().size(new SizeState(width, height)).color(new QuadColorState(d)).radius(new QuadRadiusState(10)).smoothness(1.0f).build();
        rectangle.render(x, y);

        BuiltBorder border = Builder.border()
                .size(new SizeState(width,height))
                .color(new QuadColorState(new Color(255, 255, 255, 10)))
                .radius(new QuadRadiusState(10))
                .thickness(0.6f)
                .build();
        border.render(x,y);

        this.settingsIcon.render(context.getMatrices().peek().getPositionMatrix(), x + 245, y + 25.7f);
        this.trashIcon.render(context.getMatrices().peek().getPositionMatrix(), x + 255, y + 25);

        if (this.iconTextureId != null) {
            AbstractTexture customTexture = mc.getTextureManager().getTexture(this.iconTextureId);
            if (customTexture != null) {
                Builder.texture()
                        .size(new SizeState(24, 24))
                        .texture(0.0f, 0.0f, 1.0f, 1.0f, customTexture)
                        .radius(new QuadRadiusState(6))
                        .smoothness(1)
                        .build()
                        .render(x + 12, y + (height - 24) / 2f);
            }
        } else {
            this.placeholderIcon.render(x + 10, y + (height - 32) / 2f);
        }

        Builder.text().font(FontType.sf_regular.get()).text(sInfo.name).color(new Color(255, 255, 255)).size(10).build().render(x + 50, y + 10);

        Text motd = sInfo.label != null ? sInfo.label : Text.translatable("multiplayer.status.pinging").formatted(Formatting.GRAY);
        StringBuilder fullMotd = new StringBuilder();
        motd.visit(text -> {
            fullMotd.append(text);
            return java.util.Optional.empty();
        });
        String finalMotd = fullMotd.toString().trim().replaceAll("\\s+", " ");
        TextBuilder.renderWrapped(FontType.sf_regular.get(), finalMotd, " ", x + 50, y + 25, 185.0f, new Color(150, 150, 150).getRGB(), 6.5f);

//        System.out.println(sInfo.name);
//        System.out.println(sInfo.name + " ");
//        System.out.println(sInfo.players != null);
        var fontSize = 6.5f;
        if (sInfo.players != null) {
            String now = String.valueOf(sInfo.players.online());
            String max = " / " + sInfo.players.max();
            final MsdfFont fontRegular = FontType.sf_regular.get();
            Builder.text().font(fontRegular).text(max).color(new Color(120, 120, 120)).size(fontSize).build().render((x + width - 27) - fontRegular.getWidth(max, fontSize), y + 11);
            Builder.text().font(FontType.sf_regular.get()).text(now).color(new Color(200, 200, 200)).size(fontSize).build().render((x + width - 27) - fontRegular.getWidth(max, fontSize) - fontRegular.getWidth(now, fontSize), y + 11);
        } else {
            String none = "* / *";
            Builder.text().font(FontType.sf_regular.get()).text(none).color(new Color(200, 200, 200)).size(fontSize).build().render((x + width - 27) - FontType.sf_regular.get().getWidth(none, fontSize), y + 11);
        }

        boolean unReachable = sInfo.ping <= 0;

        if(sInfo.getStatus() == ServerInfo.Status.PINGING) return;

        float pingX = x + width - 45 / 2f;
        float pingY = y + 14.5f;
        float step = 1.165f;

        if(MathUtility.isHovered(mouseX, mouseY, pingX, pingY, 2.34f * 5, 8.26f) && !unReachable){
            String text = "Ping: " + sInfo.ping;
            double textWidth = FontType.popins_regular.get().getWidth(text, 7);

            Builder.border()
                    .size(new SizeState((float) textWidth, 9))
                    .color(new QuadColorState(new Color(255, 255, 255, 10)))
                    .outlineColor(new QuadColorState(ColorUtility.fromHex("14121B")))
                    .radius(new QuadRadiusState(3))
                    .thickness(0.75f)
                    .build()
                    .render(pingX - textWidth / 2, pingY - 13);

            Builder.text()
                    .text(text)
                    .color(-1)
                    .font(FontType.popins_regular.get())
                    .size(5)
                    .build()
                    .render(pingX - textWidth / 2 + 3, pingY - 12);

        }

        for(int l = 0; l < 5; l++){

            Color color = l < 5 - i ? ColorUtility.fromHex("5CDB50") : ColorUtility.fromHex("8C889A");

            Builder.rectangle()
                    .size(2.34f, 3.6f + l * step)
                    .radius(1)
                    .color(new QuadColorState(unReachable ? ColorUtility.fromHex("D83134") : color))
                    .build()
                    .render(pingX, pingY - l * step);

            pingX += 3.41f / 2f;
        }

    }

    private @NotNull Map.Entry<Integer, Integer> getIdentifier(int index) {
        int i, k = 0;

        if(sInfo.getStatus() == ServerInfo.Status.PINGING){
            i = (int) (Util.getMeasuringTimeMs() / 100L + (index * 2L) & 7L);

            if (i > 4) {
                i = 8 - i;
            }

            k = 1;
        } else {
            if (this.sInfo.ping < 0L) {
                i = 5;
            } else if (this.sInfo.ping < 150L) {
                i = 0;
            } else if (this.sInfo.ping < 300L) {
                i = 1;
            } else if (this.sInfo.ping < 600L) {
                i = 2;
            } else if (this.sInfo.ping < 1000L) {
                i = 3;
            } else {
                i = 4;
            }
        }

        return new AbstractMap.SimpleEntry<>(i, k);
    }

    public void close() {
        if (this.iconTextureId != null) {
            mc.getTextureManager().destroyTexture(this.iconTextureId);
        }
    }
}