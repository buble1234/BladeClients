package win.blade.common.gui.impl.gui.components.implement.other;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;
import win.blade.common.gui.impl.gui.MenuScreen;
import win.blade.common.gui.impl.gui.components.AbstractComponent;
import win.blade.common.utils.color.ColorUtility;
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.builders.states.QuadColorState;
import win.blade.common.utils.render.builders.states.QuadRadiusState;
import win.blade.common.utils.render.builders.states.SizeState;
import win.blade.common.utils.render.msdf.FontType;
import win.blade.common.utils.render.msdf.MsdfFont;


import java.awt.Color;
import java.util.Locale;

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

//        Builder.texture()
//                .size(new SizeState(350, 350))
//                .color(new QuadColorState(Color.BLACK))
//                .svgTexture(Identifier.of("blade", "textures/svg/gui/ellipse.svg"))
//                .build();
//                .render(x - 175, y - 170);

        Builder.texture()
                .size(new SizeState(398.5, 248.5))
//                .svgTexture(Identifier.of("blade", "textures/svg/gui/background.svg"))
                .texture(0f, 0f, 1f, 1f, back)
                .radius(new QuadRadiusState(6))
                .build()
                .render(x, y);


        Builder.texture()
                .size(new SizeState(67.5f, 60))
                .svgTexture(Identifier.of("blade", "textures/svg/bladetitle.svg"))
                .build()
                .render(x, y - 5);

        String categoryName = menuScreen.category.toString().toLowerCase();
        String lowerName = categoryName.substring(1);
        categoryName = Character.toUpperCase(categoryName.charAt(0)) + lowerName;

        Builder.text()
                .font(fontBold)
                .text(categoryName)
                .size(5.5f)
                .color(ColorUtility.fromHex("EEEEEE"))
                .build()
                .render( x + 95, y + 9.5f);
//


//

        var matrixStack = new MatrixStack();
        matrixStack.push();

        float iconCenterX = x + 97 + fontBold.getWidth(categoryName, 5.5f);
        float iconCenterY = y + 18f;

        matrixStack.translate(iconCenterX, iconCenterY, 0);
        matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(90));

        AbstractTexture arrowdown = MinecraftClient.getInstance().getTextureManager().getTexture(Identifier.of("blade", "textures/arrpwl2.png"));

        Builder.texture()
                .size(new SizeState(8,8))
                .color(new QuadColorState(Color.WHITE))
                .texture(0f, 0f, 1f, 1f, arrowdown)
                .radius(new QuadRadiusState(0f))
                .build()
                .render(matrixStack.peek().getPositionMatrix(),   -8, -8);


        matrixStack.pop();

    }
}