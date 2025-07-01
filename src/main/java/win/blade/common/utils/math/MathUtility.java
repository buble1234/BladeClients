package win.blade.common.utils.math;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.ColorHelper;

public class MathUtility {

    public static boolean isHovered(double mouseX, double mouseY, double x, double y, double width, double height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }
    public static void scale(MatrixStack stack,
                             float x,
                             float y,
                             float scale,
                             Runnable data) {

        stack.push();
        stack.translate(x, y, 0);
        stack.scale(scale, scale, 1);
        stack.translate(-x, -y, 0);
        data.run();
        stack.pop();
    }

}
