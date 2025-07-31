package win.blade.common.gui.impl.screen.singleplayer;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.world.level.storage.LevelSummary;
import win.blade.common.utils.minecraft.MinecraftInstance;
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.builders.states.QuadColorState;
import win.blade.common.utils.render.builders.states.QuadRadiusState;
import win.blade.common.utils.render.builders.states.SizeState;
import win.blade.common.utils.render.msdf.FontType;
import win.blade.common.utils.render.renderers.impl.BuiltBorder;
import win.blade.common.utils.render.renderers.impl.BuiltRectangle;
import win.blade.common.utils.render.renderers.impl.BuiltTexture;

import java.awt.Color;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class WorldEntryWidget implements MinecraftInstance {
    private final SingleplayerScreen screen;
    private final LevelSummary summary;
    private final Identifier iconId;
    private BuiltTexture settingsIcon, trashIcon;

    public WorldEntryWidget(SingleplayerScreen screen, LevelSummary summary) {
        this.screen = screen;
        this.summary = summary;

        StringBuilder validNameBuilder = new StringBuilder();
        for (char c : summary.getName().toCharArray()) {
            if (Identifier.isPathCharacterValid(c)) {
                validNameBuilder.append(c);
            }
        }
        String worldName = validNameBuilder.toString();
        Identifier dynamicIconId = Identifier.of("blade", "worlds/" + worldName + "/icon");

        Identifier determinedIconId;
        Path iconPath = summary.getIconPath();

        if (Files.isRegularFile(iconPath)) {
            try (InputStream inputStream = Files.newInputStream(iconPath)) {
                NativeImage nativeImage = NativeImage.read(inputStream);
                NativeImageBackedTexture texture = new NativeImageBackedTexture(nativeImage);
                mc.getTextureManager().registerTexture(dynamicIconId, texture);
                determinedIconId = dynamicIconId;
            } catch (Exception e) {
                determinedIconId = Identifier.of("textures/misc/unknown_server.png");
                e.printStackTrace();
            }
        } else {
            determinedIconId = Identifier.of("textures/misc/unknown_server.png");
        }

        this.iconId = determinedIconId;

        loadIcons();
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button, int entryX, int entryY) {
        float trashX = entryX + 255;
        float trashY = entryY + 9.5f;

        if (mouseX >= trashX && mouseX <= trashX + 8.5f && mouseY >= trashY && mouseY <= trashY + 8.5f) {
            if (summary.isDeletable()) {
                mc.setScreen(new ConfirmScreen(
                        confirmed -> {
                            if (confirmed) {
                                screen.deleteEntry(this);
                            }
                            mc.setScreen(screen);
                        },
                        Text.translatable("selectWorld.deleteQuestion"),
                        Text.translatable("selectWorld.deleteWarning", summary.getName()).formatted(Formatting.RED),
                        Text.translatable("selectWorld.deleteButton"),
                        Text.translatable("gui.cancel")
                ));
            }
            return true;
        }
        return false;
    }

    public LevelSummary getSummary() {
        return summary;
    }

    private void loadIcons() {
        AbstractTexture settingTexture = mc.getTextureManager().getTexture(Identifier.of("blade", "textures/settings.png"));
        this.settingsIcon = Builder.texture().size(new SizeState(7, 7)).texture(0, 0, 1, 1, settingTexture).build();

        AbstractTexture trashTexture = mc.getTextureManager().getTexture(Identifier.of("blade", "textures/trashing.png"));
        this.trashIcon = Builder.texture().size(new SizeState(8.5f, 8.5f)).texture(0, 0, 1, 1, trashTexture).build();
    }

    public void render(DrawContext context, int x, int y, int width, int height, boolean isSelected) {
        Color d = new Color(20, 18, 27, 255);


        BuiltRectangle rectangle = Builder.rectangle()
                .size(new SizeState(width,height))
                .color(new QuadColorState(d))
                .radius(new QuadRadiusState(10))
                .smoothness(1.0f)
                .build();
        rectangle.render( x,y);

        BuiltBorder border = Builder.border()
                .size(new SizeState(width,height))
                .color(new QuadColorState(new Color(255, 255, 255, 10)))
                .radius(new QuadRadiusState(10))
                .thickness(0.6f)
                .build();
        border.render(x,y);


        this.settingsIcon.render(context.getMatrices().peek().getPositionMatrix(), x + 245, y + 10);
        this.trashIcon.render(context.getMatrices().peek().getPositionMatrix(), x + 255, y + 9.5f);

        AbstractTexture worldIconTexture = mc.getTextureManager().getTexture(this.iconId);
        Builder.texture()
                .size(new SizeState(32, 32))
                .texture(0.0f, 0.0f, 1.0f, 1.0f, worldIconTexture)
                .radius(new QuadRadiusState(6))
                .smoothness(1)
                .build()
                .render(x + 10, y + (height - 32) / 2f);

        Builder.text()
                .font(FontType.sf_regular.get())
                .text(summary.getName())
                .color(new Color(255, 255, 255))
                .size(10)
                .build()
                .render(x + 50, y + 10);


        String mode,cheats,subname;
        mode = summary.getGameMode().getTranslatableName().getString();
        cheats = summary.hasCheats() ? "читы" : "без читов";
        subname = mode + ", " + cheats;

        Builder.text()
                .font(FontType.sf_regular.get())
                .text(subname)
                .color(new Color(150, 150, 150))
                .size(6.5f)
                .build()
                .render(x + 50, y + 25);
    }
}