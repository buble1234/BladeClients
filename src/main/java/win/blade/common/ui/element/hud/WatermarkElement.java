package win.blade.common.ui.element.hud;

import net.minecraft.client.gui.DrawContext;
import org.joml.Matrix4f;
import win.blade.common.ui.element.InteractiveUIElement;
import win.blade.common.utils.math.animation.Easing;
import win.blade.common.utils.minecraft.MinecraftInstance;
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.builders.states.QuadColorState;
import win.blade.common.utils.render.builders.states.QuadRadiusState;
import win.blade.common.utils.render.builders.states.SizeState;
import win.blade.common.utils.render.msdf.FontType;
import win.blade.common.utils.render.renderers.impl.BuiltRectangle;
import win.blade.common.utils.render.renderers.impl.BuiltText;
import win.blade.core.event.controllers.EventHandler;
import win.blade.core.event.impl.input.InputEvents;
import win.blade.core.event.impl.render.RenderEvents;
import win.blade.core.module.api.Category;
import win.blade.core.module.api.Module;
import win.blade.core.module.api.ModuleInfo;
import win.blade.core.module.api.NonRegistrable;

import java.awt.*;

@ModuleInfo(
        name = "Watermark",
        category = Category.RENDER,
        desc = "Отображает водяной знак клиента"
)
public class WatermarkElement extends Module implements MinecraftInstance, NonRegistrable {

    private WatermarkUIElement watermarkUIElement;

    public WatermarkElement() {
        this.watermarkUIElement = new WatermarkUIElement("Watermark", 10, 10, 100, 22);
        this.watermarkUIElement.setAnimation(0.4, Easing.EASE_OUT_CUBIC);
    }

    @EventHandler
    public void onRenderScreen(RenderEvents.Screen e) {
        if (e == null || e.getDrawContext() == null || mc.player == null) {
            return;
        }

        watermarkUIElement.update(true);
        watermarkUIElement.render(e.getDrawContext());
    }

    @EventHandler
    public void onMouse(InputEvents.Mouse event) {
        if (watermarkUIElement != null) {
            watermarkUIElement.onMouse(event);
        }
    }

    private class WatermarkUIElement extends InteractiveUIElement {

        private final Color backgroundColor = new Color(30, 30, 30, 200);
        private final Color textColor = Color.WHITE;

        public WatermarkUIElement(String id, float x, float y, float width, float height) {
            super(id, x, y, width, height);
        }

        @Override
        public void renderContent(DrawContext context) {
            Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();

            String watermarkText = buildWatermarkText();

            float calculatedTextWidth = FontType.biko.get().getWidth(watermarkText, 12f);
            float calculatedBackgroundWidth = calculatedTextWidth + 20;
            float calculatedBackgroundHeight = 22;

            if (getWidth() != calculatedBackgroundWidth) {
                setWidth(calculatedBackgroundWidth);
            }
            if (getHeight() != calculatedBackgroundHeight) {
                setHeight(calculatedBackgroundHeight);
            }

            BuiltRectangle background = Builder.rectangle()
                    .size(new SizeState(getWidth(), getHeight()))
                    .color(new QuadColorState(backgroundColor))
                    .radius(new QuadRadiusState(5f, 5f, 5f, 5f))
                    .smoothness(1.0f)
                    .build();
            background.render(matrix, getX(), getY());

            float textX = getX() + (getWidth() - calculatedTextWidth) / 2.0f - 3;
            float textY = getY() + getHeight() / 2.0f - FontType.sf_regular.get().getFontHeight(FontType.sf_regular.get(), 12f) / 2.0f;

            BuiltText text = Builder.text()
                    .font(FontType.sf_regular.get())
                    .text(watermarkText)
                    .color(textColor)
                    .size(12f)
                    .thickness(0.05f)
                    .build();
            text.render(matrix, textX, textY);
        }

        @Override
        public void update() {
            super.update();
        }

        @Override
        public void onMouse(InputEvents.Mouse event) {
            super.onMouse(event);
        }

        private String buildWatermarkText() {
            String clientName = "Blade";
            String fps = String.valueOf(mc.getCurrentFps());
            return clientName + " | FPS: " + fps;
        }
    }
}