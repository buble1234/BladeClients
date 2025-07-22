package win.blade.core.event.impl.render;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import win.blade.core.event.controllers.Event;

public abstract class RenderEvents extends Event {

    public enum ScreenType {
        ON,
        POST,
    }

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

    public static abstract class Screen extends RenderEvents {
        private final DrawContext drawContext;
        private final ScreenType screenType;

        protected Screen(MatrixStack matrixStack, float partialTicks, DrawContext drawContext, ScreenType screenType) {
            super(matrixStack, partialTicks);
            this.drawContext = drawContext;
            this.screenType = screenType;
        }

        public DrawContext getDrawContext() {
            return drawContext;
        }

        public ScreenType getScreenType() {
            return screenType;
        }
    }

    public static class PRE extends Screen {
        public PRE(MatrixStack matrixStack, float partialTicks, DrawContext drawContext) {
            super(matrixStack, partialTicks, drawContext, ScreenType.ON);
        }
    }

    public static class POST extends Screen {
        public POST(MatrixStack matrixStack, float partialTicks, DrawContext drawContext) {
            super(matrixStack, partialTicks, drawContext, ScreenType.POST);
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
