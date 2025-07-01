package win.blade.common.utils.math.animation;


import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import win.blade.common.utils.math.animation.Animation;

import java.awt.geom.Point2D;

public class AnimationUtil {

    public static void animateScale(DrawContext context, float centerX, float centerY, Animation animation, Runnable renderCode) {
        MatrixStack matrices = context.getMatrices();
        float scale = animation.get();

        matrices.push();
        matrices.translate(centerX, centerY, 0);
        matrices.scale(scale, scale, 1);
        matrices.translate(-centerX, -centerY, 0);

        renderCode.run();

        matrices.pop();
    }

    public static Point2D.Float unproject(float mouseX, float mouseY, float pivotX, float pivotY, float scale) {
        if (scale == 0) {
            return new Point2D.Float(pivotX, pivotY);
        }
        float transformedX = pivotX + (mouseX - pivotX) / scale;
        float transformedY = pivotY + (mouseY - pivotY) / scale;
        return new Point2D.Float(transformedX, transformedY);
    }
}