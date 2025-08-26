package win.blade.common.gui.impl.gui.components.implement.other;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import win.blade.common.gui.impl.gui.components.AbstractComponent;
import win.blade.common.gui.impl.gui.trait.ResizableMovable;
import win.blade.common.utils.color.ColorUtility;
import win.blade.common.utils.math.MathUtility;
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.builders.states.QuadColorState;
import win.blade.common.utils.render.builders.states.QuadRadiusState;
import win.blade.common.utils.render.builders.states.SizeState;

import java.awt.Color;
import java.io.Serializable;

public class CheckComponent extends AbstractComponent {
    private boolean state;
    private Runnable runnable;

    public CheckComponent setState(boolean state) {
        this.state = state;
        return this;
    }

    public CheckComponent setRunnable(Runnable runnable) {
        this.runnable = runnable;
        return this;
    }

    @Override
    public CheckComponent position(float x, float y) {
        return (CheckComponent) super.position(x, y);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        Color stateColor = state
                ? new Color(102,60,255)
                : new Color(28,26,37);

        Builder.rectangle()
                .size(new SizeState(8, 8))
                .color(new QuadColorState(new Color(stateColor.getRGB())))
                .radius(new QuadRadiusState(2f))
                .build()
                .render(x, y);

        if (state) {

            Builder.texture()
                    .size(new SizeState(4, 4))
                    .color(new QuadColorState(Color.WHITE))
                    .svgTexture(0f, 0f, 1f, 1f, Identifier.of("blade", "textures/svg/gui/check.svg"))
                    .radius(new QuadRadiusState(0f))
                    .build()
                    .render(x + 1.9f , y + 1.9f);

        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (MathUtility.isHovered(mouseX, mouseY, x, y, 8, 8) && button == 0) {
            runnable.run();
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }



}