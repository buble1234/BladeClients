package win.blade.common.utils.render;

import com.mojang.blaze3d.systems.RenderSystem;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import win.blade.common.utils.minecraft.MinecraftInstance;
import win.blade.common.utils.other.Pool;
import java.util.Stack;
public class ScissorManager implements MinecraftInstance {
    static Pool<Scissor> scissorPool = new Pool<>(Scissor::new);
    static Stack<Scissor> scissorStack = new Stack<>();


    public static void push(Matrix4f matrix4f, float x, float y, float width, float height) {
        Scissor currentScissor = scissorPool.get().copy();

        Vector3f pos = matrix4f.transformPosition(x,y,0, new Vector3f());
        Vector3f size = matrix4f.getScale(new Vector3f()).mul(width, height, 0);

        currentScissor.set(pos.x, pos.y, size.x, size.y);
        scissorStack.push(currentScissor);
        setScissor(currentScissor);
    }

    public static void pop() {
        if (!scissorStack.isEmpty()) {
            scissorPool.free(scissorStack.pop());
            if (scissorStack.isEmpty()) {
                RenderSystem.disableScissor();
            } else {
                setScissor(scissorStack.peek());
            }
        }
    }

    private static void setScissor(Scissor scissor) {
        int scaleFactor = (int) mc.getWindow().getScaleFactor();
        int x = scissor.x * scaleFactor;
        int y = mc.getWindow().getHeight() - (scissor.y * scaleFactor + scissor.height * scaleFactor);
        int width = scissor.width * scaleFactor;
        int height = scissor.height * scaleFactor;

        RenderSystem.enableScissor(x, y, width, height);
    }

    private static class Scissor {
        public int x, y;
        public int width, height;

        public void set(double x, double y, double width, double height) {
            this.x = Math.max(0, (int) Math.round(x));
            this.y = Math.max(0, (int) Math.round(y));
            this.width = Math.max(0, (int) Math.round(width));
            this.height = Math.max(0, (int) Math.round(height));
        }

        Scissor copy() {
            Scissor newScissor = new Scissor();
            newScissor.set(this.x, this.y, this.width, this.height);
            return newScissor;
        }
    }
}