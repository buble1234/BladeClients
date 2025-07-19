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

    private final MsdfFont fontBold = FontType.popins_regular.get();

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

        AbstractTexture back = MinecraftClient.getInstance().getTextureManager().getTexture(Identifier.of("blade", "textures/backgr.png"));

        Builder.texture()
                .size(new SizeState(width, height))
                .color(new QuadColorState(Color.WHITE))
                .texture(0f, 0f, 1f, 1f, back)
                .radius(new QuadRadiusState(12))
                .build()
                .render(x, y);


        Builder.border()
                .size(new SizeState(width, height))
                .color(new QuadColorState(new Color(170, 160, 200, 25)))
                .radius(new QuadRadiusState(9))
                .outlineColor(new QuadColorState(255,255,255,0))
                .thickness(1)
                .build()
                .render(x, y);

        AbstractTexture logoTexture = MinecraftClient.getInstance().getTextureManager().getTexture(Identifier.of("blade", "textures/logo.png"));

        Builder.texture()
                .size(new SizeState(111/2, 25/2))
                .color(new QuadColorState(Color.WHITE))
                .texture(0f, 0f, 1f, 1f, logoTexture)
                .radius(new QuadRadiusState(0f))
                .build()
                .render(x + 13, y + 20);

        Builder.text()
                .font(fontBold)
                .text(menuScreen.category.getName())
                .size(6)
                .color(new Color(0xFFD4D6E1))
                .build()
                .render( x + 95, y + 10);
    }
}