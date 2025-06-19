package win.blade.core.module.storage.render.hud;

import net.minecraft.client.gui.DrawContext;
import org.joml.Matrix4f;
import win.blade.common.utils.minecraft.MinecraftInstance;
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.builders.states.QuadColorState;
import win.blade.common.utils.render.builders.states.QuadRadiusState;
import win.blade.common.utils.render.builders.states.SizeState;
import win.blade.common.utils.render.msdf.FontType;
import win.blade.common.utils.render.renderers.impl.BuiltRectangle;
import win.blade.common.utils.render.renderers.impl.BuiltText;
import win.blade.core.event.controllers.EventHandler;
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
public class WatermarkModule extends Module implements MinecraftInstance, NonRegistrable {

    private final int x = 10;
    private final int y = 10;
    private final int height = 22;

    private final Color backgroundColor = new Color(30, 30, 30, 200);
    private final Color textColor = Color.WHITE;

    @EventHandler
    public void onRenderScreen(RenderEvents.Screen e) {
        if (e == null || e.getDrawContext() == null || mc.player == null) {
            return;
        }

        DrawContext context = e.getDrawContext();
        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();

        String watermarkText = buildWatermarkText();

        float textWidth = FontType.biko.get().getWidth(watermarkText, 12f);
        int width = (int) (textWidth + 20);

        BuiltRectangle background = Builder.rectangle()
                .size(new SizeState(width, height))
                .color(new QuadColorState(new Color(30, 30, 30, 200)))
                .radius(new QuadRadiusState(5f, 5f, 5f, 5f))
                .smoothness(1.0f)
                .build();
        background.render(matrix, x, y);

        float textX = x + (width - textWidth) / 2.0f - 3;
        float textY = y + height / 2.0f - 7;

        BuiltText text = Builder.text()
                .font(FontType.sf_regular.get())
                .text(watermarkText)
                .color(textColor)
                .size(12f)
                .thickness(0.05f)
                .build();
        text.render(matrix, textX, textY);
    }

    private String buildWatermarkText() {
        String clientName = "Blade";
        String fps = String.valueOf(mc.getCurrentFps());

        return clientName + " | FPS: " + fps;
    }
}