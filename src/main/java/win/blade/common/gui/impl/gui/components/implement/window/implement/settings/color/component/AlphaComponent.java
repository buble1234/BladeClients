package win.blade.common.gui.impl.gui.components.implement.window.implement.settings.color.component;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.util.Identifier;
import win.blade.common.gui.impl.gui.components.AbstractComponent;
import win.blade.common.gui.impl.gui.setting.implement.ColorSetting;
import win.blade.common.utils.color.ColorUtility;
import win.blade.common.utils.math.MathUtility;
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.builders.states.QuadColorState;
import win.blade.common.utils.render.builders.states.QuadRadiusState;
import win.blade.common.utils.render.builders.states.SizeState;

import java.awt.Color;

import static net.minecraft.util.math.MathHelper.clamp;

public class AlphaComponent extends AbstractComponent {
    private final ColorSetting setting;
    private boolean alphaDragging;

    private float X, Y, W, H;

    public AlphaComponent(ColorSetting setting) {
        this.setting = setting;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
//        X = x + 6;
//        Y = y + 91.5f;
//        W = 138;
//        H = 4;

        X = x + 9f;
        Y = y + 91.5F;
        W = 88;
        H = 5;

        float clampedX = X + W * setting.getAlpha();
        float min = clamp((mouseX - X) / W, 0, 1);

//        AbstractTexture alphaTexture = MinecraftClient.getInstance().getTextureManager().getTexture(Identifier.of("blade", "textures/alpha.png"));
//            Builder.texture()
//                    .size(new SizeState(W, H))
//                    .color(new QuadColorState(Color.WHITE))
//                    .texture(0f, 0f, 1f, 1f, alphaTexture)
//                    .radius(new QuadRadiusState(0f))
//                    .build()
//                    .render(X, Y);
        Builder.rectangle()
                .size(new SizeState(W + 0.5f, 3.8f))
                .color(new QuadColorState(-1,-1,setting.getColorWithAlpha(),setting.getColorWithAlpha()))
                .radius(new QuadRadiusState(1))
                .build()
                .render(X, Y);

//        Builder.rectangle()
//                .size(new SizeState(W + 0.5f, 3.8f))
//                .color(new QuadColorState(0x80000000,0x80000000,setting.getColorWithAlpha(),setting.getColorWithAlpha()))
//                .radius(new QuadRadiusState(0.5f))
//                .build()
//                .render(X, Y);

        Builder.border()
                .size(new SizeState(6, 6))
                .outlineColor(new QuadColorState(setting.getColor()))
                .color(new QuadColorState(ColorUtility.fromHex("1C1A25")))
                .radius(new QuadRadiusState(2))
                .thickness(0.45f)
                .build()
                .render(clampedX - H / 2f, Y - 1.25f);

        if (alphaDragging) {
            setting.setAlpha(min);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        alphaDragging = button == 0 && MathUtility.isHovered(mouseX, mouseY, X, Y, W, H);
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        alphaDragging = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }
}