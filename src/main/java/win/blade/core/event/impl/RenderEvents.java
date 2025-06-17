package win.blade.core.event.impl;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import win.blade.core.event.controllers.Event;

public abstract class RenderEvents extends Event {

    private final MatrixStack matrixStack;
    private final float partialTicks;

    private RenderEvents(MatrixStack matrixStack, float partialTicks) {
        this.matrixStack = matrixStack;
        this.partialTicks = partialTicks;
    }

    public MatrixStack getMatrixStack() {
        return matrixStack;
    }

    public float getPartialTicks() {
        return partialTicks;
    }

    public static class Screen extends RenderEvents {
        private final DrawContext drawContext;

        public Screen(MatrixStack matrixStack, float partialTicks, DrawContext drawContext) {
            super(matrixStack, partialTicks);
            this.drawContext = drawContext;
        }

        public DrawContext getDrawContext() {
            return drawContext;
        }
    }

    public static class World extends RenderEvents {
        private final Camera camera;

        public World(MatrixStack matrixStack, Camera camera, float partialTicks) {
            super(matrixStack, partialTicks);
            this.camera = camera;
        }

        public Camera getCamera() {
            return camera;
        }
    }
}
