package win.blade.common.gui.impl.gui.components.implement.other;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import win.blade.common.gui.impl.gui.MenuScreen;
import win.blade.common.gui.impl.gui.components.AbstractComponent;
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.builders.states.QuadColorState;
import win.blade.common.utils.render.builders.states.QuadRadiusState;
import win.blade.common.utils.render.builders.states.SizeState;
import win.blade.common.utils.render.msdf.FontType;
import win.blade.common.utils.render.msdf.MsdfFont;


import java.awt.Color;

public class BackgroundComponent extends AbstractComponent {
    private MenuScreen menuScreen;

    private final MsdfFont fontBold = FontType.sf_regular.get();

    public BackgroundComponent setMenuScreen(MenuScreen menuScreen) {
        this.menuScreen = menuScreen;
        return this;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        Matrix4f positionMatrix = context
                .getMatrices()
                .peek()
                .getPositionMatrix();

        AbstractTexture backgroundTexture = MinecraftClient.getInstance().getTextureManager().getTexture(Identifier.of("blade", "textures/background.png"));

        Builder.texture()
                .size(new SizeState(width, height))
                .color(new QuadColorState(Color.WHITE))
                .texture(0f, 0f, 1f, 1f, backgroundTexture)
                .radius(new QuadRadiusState(0f))
                .build()
                .render(x, y);

        AbstractTexture logoTexture = MinecraftClient.getInstance().getTextureManager().getTexture(Identifier.of("blade", "textures/small_logo.png"));

        Builder.texture()
                .size(new SizeState(58, 11))
                .color(new QuadColorState(Color.WHITE))
                .texture(0f, 0f, 1f, 1f, logoTexture)
                .radius(new QuadRadiusState(0f))
                .build()
                .render(x + 13, y + 20);

        Builder.text()
                .font(fontBold)
                .text(menuScreen.category.getName())
                .size(8)
                .color(new Color(0xFFD4D6E1))
                .build()
                .render( x + 95, y + 13);
    }
}