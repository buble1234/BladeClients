package win.blade.common.gui.impl.gui.components.implement.other;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import win.blade.common.gui.impl.gui.MenuScreen;
import win.blade.common.gui.impl.gui.components.AbstractComponent;
import win.blade.common.gui.impl.gui.components.implement.window.AbstractWindow;
import win.blade.common.gui.impl.gui.components.implement.window.implement.module.InfoWindow;
import win.blade.common.utils.math.MathUtility;
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.builders.states.QuadColorState;
import win.blade.common.utils.render.builders.states.QuadRadiusState;
import win.blade.common.utils.render.builders.states.SizeState;
import win.blade.common.utils.render.msdf.FontType;
import win.blade.common.utils.render.msdf.MsdfFont;


import java.awt.Color;

public class UserComponent extends AbstractComponent {
    private MenuScreen menuScreen;

    private final MsdfFont fontRegular = FontType.sf_regular.get();

    public UserComponent setMenuScreen(MenuScreen menuScreen) {
        this.menuScreen = menuScreen;
        return this;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        Matrix4f positionMatrix = context
                .getMatrices()
                .peek()
                .getPositionMatrix();

        Builder.rectangle()
                .size(new SizeState(15, 15))
                .color(new QuadColorState(Color.WHITE))
                .radius(new QuadRadiusState(7))
                .build()
                .render(x + 6, y - 25);

        Builder.rectangle()
                .size(new SizeState(6, 6))
                .color(new QuadColorState(new Color(0xFF26c68c)))
                .radius(new QuadRadiusState(3))
                .build()
                .render(x + 15.5f, y - 15.5f);

        Builder.text()
                .font(fontRegular)
                .text("username")
                .size(6)
                .color(new Color(0xFFD4D6E1))
                .build()
                .render( x + 25, y - 21);

        Builder.text()
                .font(fontRegular)
                .text("09.05.2024")
                .size(5)
                .color(new Color(0xFF8187FF))
                .build()
                .render( x + 25, y - 14.5f);

        AbstractTexture settingsTexture = MinecraftClient.getInstance().getTextureManager().getTexture(Identifier.of("blade", "textures/settings.png"));

        Builder.texture()
                .size(new SizeState(6.5f, 6.5f))
                .color(new QuadColorState(new Color(0xFFafb0bc)))
                .texture(0f, 0f, 1f, 1f, settingsTexture)
                .radius(new QuadRadiusState(0f))
                .build()
                .render(x + 72, y - 20);

        AbstractTexture paletteTexture = MinecraftClient.getInstance().getTextureManager().getTexture(Identifier.of("blade", "textures/palette.png"));

        Builder.texture()
                .size(new SizeState(7, 7))
                .color(new QuadColorState(new Color(0xFFafb0bc)))
                .texture(0f, 0f, 1f, 1f, paletteTexture)
                .radius(new QuadRadiusState(0f))
                .build()
                .render(x + 61, y - 20);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (MathUtility.isHovered(mouseX, mouseY, x + 72, y - 20, 6.5F, 6.5F) && button == 0) {
            AbstractWindow infoWindow = new InfoWindow()
                    .position(menuScreen.x - 150, menuScreen.y)
                    .size(140, 184);

            windowManager.add(infoWindow);
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }
}