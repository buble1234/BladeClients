package win.blade.common.gui.impl.gui.components.implement.other;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.AbstractTexture;
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
                .size(new SizeState(398.5, 248.5))
                .texture(0f, 0f, 1f, 1f, back)
                .radius(new QuadRadiusState(6))
                .build()
                .render(x, y);








        Builder.text()
                .font(FontType.icon2.get())
                .text("a")
                .size(10)
                .color(new Color(102,60,255))
                .build()
                .render( x + 15.5f, y + 19f);


        Builder.text()
                .font(FontType.involve_regular.get())
                .text("Blade Clien")
                .size(6.5f)
                .color(ColorUtility.fromHex("EEEEEE"))
                .build()
                .render( x + 28, y + 20);

        Builder.text()
                .font(FontType.sf_regular.get())
                .text("t")
                .size(6.5f)
                .color(ColorUtility.fromHex("EEEEEE"))
                .build()
                .render( x + 65.5f, y + 21.1f);


        Builder.text()
                .font(fontBold)
                .text("Move")
                .size(5.5f)
                .color(ColorUtility.fromHex("EEEEEE"))
                .build()
                .render( x + 95, y + 9.5f);



//
//        context.getMatrices().push();
//
//        float iconCenterX = x + 116f + 8;
//        float iconCenterY = y + 9.7f + 8;
//
//        context.getMatrices().translate(iconCenterX, iconCenterY, 0);
//        context.getMatrices().multiply(RotationAxis.POSITIVE_Z.rotationDegrees(90));
//
//        AbstractTexture arrowdown = MinecraftClient.getInstance().getTextureManager().getTexture(Identifier.of("blade", "textures/arrpwl2.png"));
//
//        Builder.texture()
//                .size(new SizeState(8,8))
//                .color(new QuadColorState(Color.WHITE))
//                .texture(0f, 0f, 1f, 1f, arrowdown)
//                .radius(new QuadRadiusState(0f))
//                .build()
//                .render( context.getMatrices().peek().getPositionMatrix(),   -8, -8);
//
//        context.getMatrices().pop();

    }
}