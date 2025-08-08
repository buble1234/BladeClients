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

    private final MsdfFont fontRegular = FontType.popins_regular.get();

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

        float x = this.x + 3.5f;

        Builder.rectangle()
                .size(new SizeState(15, 15))
                .color(new QuadColorState(new Color(255,255,255,25)))
                .radius(new QuadRadiusState(5))
                .build()
                .render(x + 6, y - 24);

        Builder.rectangle()
                .size(new SizeState(5.5f, 5.5f))
                .color(new QuadColorState(new Color(92,219,80)))
                .radius(new QuadRadiusState(2))
                .build()
                .render(x + 15.5f, y - 14.5f);

        Builder.text()
                .font(fontRegular)
                .text("cutthroat")
                .size(5)
                .color(new Color(0xFFD4D6E1))
                .build()
                .render( x + 24, y - 22.5f);

        Builder.text()
                .font(fontRegular)
                .text("Developer")
                .size(3.5f)
                .color(new Color(140,136,154))
                .build()
                .render( x + 24.5f, y - 16f);
//
//        AbstractTexture settingsTexture = MinecraftClient.getInstance().getTextureManager().getTexture(Identifier.of("blade", "textures/settings.png"));
//
//        Builder.texture()
//                .size(new SizeState(6.5f, 6.5f))
//                .color(new QuadColorState(new Color(0xFFafb0bc)))
//                .texture(0f, 0f, 1f, 1f, settingsTexture)
//                .radius(new QuadRadiusState(0f))
//                .build()
//                .render(x + 72, y - 20);

        Builder.texture()
                .size(new SizeState(6, 6))
                .color(new QuadColorState(-1))
                .svgTexture(0f, 0f, 1f, 1f, Identifier.of("blade", "textures/svg/gui/setting.svg"))
                .radius(new QuadRadiusState(0f))
                .build()
                .render(this.x + 71, y - 19);

    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (MathUtility.isHovered(mouseX, mouseY, x + 71, y - 19, 6F, 6F) && button == 0) {
            AbstractWindow infoWindow = new InfoWindow()
                    .position(menuScreen.x - 150, menuScreen.y)
                    .size(140, 184);

            windowManager.add(infoWindow);
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }
}