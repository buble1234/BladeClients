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
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.builders.states.QuadColorState;
import win.blade.common.utils.render.builders.states.QuadRadiusState;
import win.blade.common.utils.render.builders.states.SizeState;
import win.blade.common.utils.render.msdf.FontType;
import win.blade.common.utils.render.renderers.impl.BuiltRectangle;
import win.blade.common.utils.render.renderers.impl.BuiltText;

import java.awt.*;

@Mixin(SliderWidget.class)
public abstract class MixinSliderWidget extends ClickableWidget {
//    @Shadow protected double value;
//
//    private final Color sliderBackground = new Color(30, 30, 30);
//    private final Color sliderHandle = new Color(200, 200, 200);
//
    public MixinSliderWidget(int x, int y, int width, int height, Text message) {
        super(x, y, width, height, message);
    }
//
//    @Inject(method = "renderWidget", at = @At("HEAD"), cancellable = true)
//    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
//        if (this.visible) {
//            ci.cancel();
//
//            Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();
//
//            BuiltRectangle background = Builder.rectangle()
//                    .size(new SizeState(this.getWidth(), this.getHeight()))
//                    .color(new QuadColorState(sliderBackground, sliderBackground, sliderBackground, sliderBackground))
//                    .radius(new QuadRadiusState(5))
//                    .smoothness(1.0f)
//                    .build();
//            background.render(matrix, this.getX(), this.getY());
//
//            int handleX = this.getX() + (int) (this.value * (double) (this.width - 8));
//            BuiltRectangle handle = Builder.rectangle()
//                    .size(new SizeState(8, this.getHeight()))
//                    .color(new QuadColorState(sliderHandle, sliderHandle, sliderHandle, sliderHandle))
//                    .radius(new QuadRadiusState(0f, 0f, 0f, 0f))
//                    .smoothness(1.0f)
//                    .build();
//            handle.render(matrix, handleX, this.getY());
//
//            Text message = this.getMessage();
//            String textToRender = message.getString();
//            float textWidth = FontType.biko.get().getWidth(textToRender, 9f);
//
//            float textX = this.getX() + (this.getWidth() - textWidth) / 2.0f;
//            float textY = this.getY() + this.getHeight() / 2.0f - 4;
//
//            Color textColor = this.active ? Color.WHITE : new Color(160, 160, 160);
//
//            BuiltText text = Builder.text()
//                    .font(FontType.biko.get())
//                    .text(textToRender)
//                    .color(textColor)
//                    .size(9f)
//                    .thickness(0.05f)
//                    .build();
//            text.render(matrix, textX, textY);
//        }
//    }
}