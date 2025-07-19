package win.blade.mixin.minecraft.gui.widgets;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import win.blade.common.utils.minecraft.MinecraftInstance;
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.builders.states.QuadColorState;
import win.blade.common.utils.render.builders.states.QuadRadiusState;
import win.blade.common.utils.render.builders.states.SizeState;
import win.blade.common.utils.render.msdf.FontType;
import win.blade.common.utils.render.renderers.impl.BuiltBorder;
import win.blade.common.utils.render.renderers.impl.BuiltRectangle;
import win.blade.common.utils.render.renderers.impl.BuiltText;
import win.blade.core.Manager;

import java.awt.*;

@Mixin(SliderWidget.class)
public abstract class MixinSliderWidget extends ClickableWidget implements MinecraftInstance {
    @Shadow protected double value;

    public MixinSliderWidget(int x, int y, int width, int height, Text message) {
        super(x, y, width, height, message);
    }

    @Inject(method = "renderWidget", at = @At("HEAD"), cancellable = true)
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!Manager.isPanic()) {
            if (this.visible) {
                ci.cancel();

                Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();
                boolean isHovered = this.isHovered();

                Color baseColor = new Color(20, 18, 27);
                Color borderColor = isHovered ? new Color(80, 70, 120, 200) : new Color(255, 255, 255, 15);
                Color textColor = this.active ? new Color(140, 136, 154, 255) : new Color(100, 100, 100, 255);

                int visualHeight = 32;
                int visualY = this.getY() + (this.getHeight() - visualHeight) / 2;

                int filledWidth = (int) (this.getWidth() * this.value);
                Color filledColor = new Color(71, 47, 108, 150);

                BuiltRectangle background = Builder.rectangle()
                        .size(new SizeState(this.getWidth(), visualHeight))
                        .color(new QuadColorState(baseColor))
                        .radius(new QuadRadiusState(10))
                        .smoothness(1.0f)
                        .build();
                background.render(matrix, this.getX(), visualY);

                if (filledWidth > 0) {
                    BuiltRectangle fill = Builder.rectangle()
                            .size(new SizeState(filledWidth, visualHeight))
                            .color(new QuadColorState(filledColor))
                            .radius(new QuadRadiusState(10))
                            .smoothness(1.0f)
                            .build();
                    fill.render(matrix, this.getX(), visualY);
                }

                BuiltBorder border = Builder.border()
                        .size(new SizeState(this.getWidth(), visualHeight))
                        .color(new QuadColorState(borderColor))
                        .radius(new QuadRadiusState(10))
                        .thickness(0.8f)
                        .build();
                border.render(matrix, this.getX(), visualY);

                String textToRender = this.getMessage().getString();
                float textSize = 7.5f;
                float textWidth = FontType.sf_regular.get().getWidth(textToRender, textSize);

                float textX = this.getX() + (this.getWidth() - textWidth) / 2.0f;
                float textY = visualY + (visualHeight / 2.0f) - 5;

                BuiltText render = Builder.text()
                        .font(FontType.sf_regular.get())
                        .text(textToRender)
                        .color(textColor)
                        .size(textSize)
                        .thickness(0.05f)
                        .build();
                render.render(matrix, textX, textY);
            }
        }
    }
}