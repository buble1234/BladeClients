package win.blade.common.gui.impl.gui.components.implement.other;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import win.blade.common.gui.impl.gui.components.AbstractComponent;
import win.blade.common.utils.math.MathUtility;
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.builders.states.QuadColorState;
import win.blade.common.utils.render.builders.states.QuadRadiusState;
import win.blade.common.utils.render.builders.states.SizeState;
import win.blade.common.utils.render.msdf.FontType;
import win.blade.common.utils.render.msdf.MsdfFont;

import java.awt.Color;

public class LanguageComponent extends AbstractComponent {

    private final MsdfFont fontRegular = FontType.sf_regular.get();

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        Matrix4f positionMatrix = context
                .getMatrices()
                .peek()
                .getPositionMatrix();

        width = 31;
        height = 15;

        Builder.rectangle()
                .size(new SizeState(width, height))
                .color(new QuadColorState(new Color(0x1A191A28)))
                .radius(new QuadRadiusState(6))
                .build()
                .render(x, y);

        AbstractTexture locateTexture = MinecraftClient.getInstance().getTextureManager().getTexture(Identifier.of("blade", "textures/locate.png"));

        Builder.texture()
                .size(new SizeState(6, 6))
                .color(new QuadColorState(Color.WHITE))
                .texture(0f, 0f, 1f, 1f, locateTexture)
                .radius(new QuadRadiusState(0f))
                .build()
                .render(x + 5, y + 4.5f);

        Builder.text()
                .font(fontRegular)
                .text("eng")
                .size(6)
                .color(new Color(0xFF878894))
                .build()
                .render( x + 13.3f, y + 4.5f);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (MathUtility.isHovered(mouseX, mouseY, x, y, width, height) && button == 0) {
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}