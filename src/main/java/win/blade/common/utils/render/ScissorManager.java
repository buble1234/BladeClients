package win.blade.common.utils.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import win.blade.common.utils.other.Pool;


import java.util.Stack;

public class ScissorManager {
    private final MinecraftClient mc = MinecraftClient.getInstance();
    private final Pool<Scissor> scissorPool = new Pool<>(Scissor::new);
    private final Stack<Scissor> scissorStack = new Stack<>();

    public void push(double x, double y, double width, double height) {
        if (!scissorStack.isEmpty()) {
            Scissor parent = scissorStack.peek();
            double newX = Math.max(x, parent.x);
            double newY = Math.max(y, parent.y);
            double newEndX = Math.min(x + width, parent.x + parent.width);
            double newEndY = Math.min(y + height, parent.y + parent.height);
            x = newX;
            y = newY;
            width = Math.max(0, newEndX - newX);
            height = Math.max(0, newEndY - newY);
        }

        Scissor currentScissor = scissorPool.get();
        currentScissor.set(x, y, width, height);
        scissorStack.push(currentScissor);
        setScissor(currentScissor);
    }

    public void pop() {
        if (!scissorStack.isEmpty()) {
            scissorPool.free(scissorStack.pop());
            if (scissorStack.isEmpty()) {
                RenderSystem.disableScissor();
            } else {
                setScissor(scissorStack.peek());
            }
        }
    }

    private void setScissor(Scissor scissor) {
        double scaleFactor = mc.getWindow().getScaleFactor();
        int x = (int) (scissor.x * scaleFactor);

        int y = (int) (mc.getWindow().getHeight() - (scissor.y * scaleFactor + scissor.height * scaleFactor));
        int width = (int) (scissor.width * scaleFactor);
        int height = (int) (scissor.height * scaleFactor);

        RenderSystem.enableScissor(x, y, width, height);
    }

    private static class Scissor {
        public double x, y, width, height;

        public void set(double x, double y, double width, double height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }
}