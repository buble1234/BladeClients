package win.blade.common.gui.impl.gui.components.implement.window.implement.settings.color.component;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.util.Identifier;
import win.blade.common.gui.impl.gui.components.AbstractComponent;
import win.blade.common.gui.impl.gui.setting.implement.ColorSetting;
import win.blade.common.utils.math.MathUtility;
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.builders.states.QuadColorState;
import win.blade.common.utils.render.builders.states.QuadRadiusState;
import win.blade.common.utils.render.builders.states.SizeState;

import java.awt.Color;

import static net.minecraft.util.math.MathHelper.clamp;

public class SaturationComponent extends AbstractComponent {
    private final ColorSetting setting;
    private boolean saturationDragging;

    private float X, Y, W, H;

    public SaturationComponent(ColorSetting setting) {
        this.setting = setting;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        X = x + 6;
        Y = y + 73.5F;
        W = 138;
        H = 4;

        float clampedX = X + W * setting.getHue();
        float min = clamp((mouseX - X) / W, 0, 1);

        AbstractTexture hueTexture = MinecraftClient.getInstance().getTextureManager().getTexture(Identifier.of("blade", "textures/hue.png"));
        if (hueTexture != null) {
            Builder.texture()
                    .size(new SizeState(W, H - 1))
                    .color(new QuadColorState(Color.WHITE))
                    .texture(0f, 0f, 1f, 1f, hueTexture)
                    .radius(new QuadRadiusState(0f))
                    .build()
                    .render(X, Y + 0.5f);
        }

        Builder.rectangle()
                .size(new SizeState(H, H))
                .color(new QuadColorState(Color.WHITE))
                .radius(new QuadRadiusState(1))
                .build()
                .render(clampedX - H / 2f, Y);

        if (saturationDragging) {
            setting.setHue(min);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        saturationDragging = button == 0 && MathUtility.isHovered(mouseX, mouseY, X, Y, W, H);
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        saturationDragging = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }
}