package win.blade.mixin.minecraft.gui.widgets;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.text.Text;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import win.blade.common.utils.minecraft.MinecraftInstance;
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.builders.states.QuadColorState;
import win.blade.common.utils.render.builders.states.QuadRadiusState;
import win.blade.common.utils.render.builders.states.SizeState;
import win.blade.common.utils.render.msdf.FontType;
import win.blade.common.utils.render.renderers.impl.BuiltRectangle;
import win.blade.common.utils.render.renderers.impl.BuiltText;

import java.awt.*;

@Mixin(PressableWidget.class)
public abstract class MixinPressableWidget extends ClickableWidget implements MinecraftInstance {

//    private final Color normalColor = new Color(30, 30, 30);
//    private final Color hoverColor = new Color(50, 50, 50);
//
    public MixinPressableWidget(int x, int y, int width, int height, Text message) {
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
//            boolean isHovered = mouseX >= this.getX() && mouseX <= this.getX() + this.getWidth() && mouseY >= this.getY() && mouseY <= this.getY() + this.getHeight();
//            Color backgroundColor = isHovered ? hoverColor : normalColor;
//
//            BuiltRectangle background = Builder.rectangle()
//                    .size(new SizeState(this.getWidth(), this.getHeight()))
//                    .color(new QuadColorState(backgroundColor, backgroundColor, backgroundColor, backgroundColor))
//                    .radius(new QuadRadiusState(5))
//                    .smoothness(1.0f)
//                    .build();
//            background.render(matrix, this.getX(), this.getY());
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