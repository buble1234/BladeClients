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
import java.awt.Color;

public class SettingComponent extends AbstractComponent {
    private Runnable runnable;

    public SettingComponent setRunnable(Runnable runnable) {
        this.runnable = runnable;
        return this;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        Builder.texture()
                .size(new SizeState(7, 7))
                .color(new QuadColorState(-1))
                .svgTexture(0f, 0f, 1f, 1f, Identifier.of("blade", "textures/gui/setting.svg"))
                .radius(new QuadRadiusState(0f))
                .build()
                .render(x, y);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (MathUtility.isHovered(mouseX, mouseY, x, y, 7, 7) && button == 0) {
            runnable.run();
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}