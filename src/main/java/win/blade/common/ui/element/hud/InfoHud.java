package win.blade.common.ui.element.hud;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import win.blade.common.ui.element.InteractiveUIElement;
import win.blade.common.utils.math.animation.Easing;
import win.blade.common.utils.minecraft.MinecraftInstance;
import win.blade.common.utils.player.MovementUtility;
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.builders.states.QuadColorState;
import win.blade.common.utils.render.builders.states.QuadRadiusState;
import win.blade.common.utils.render.builders.states.SizeState;
import win.blade.common.utils.render.msdf.FontType;
import win.blade.common.utils.render.renderers.impl.BuiltBorder;
import win.blade.common.utils.render.renderers.impl.BuiltRectangle;
import win.blade.core.event.controllers.EventHandler;
import win.blade.core.event.impl.input.InputEvents;
import win.blade.core.event.impl.render.RenderEvents;
import win.blade.core.module.api.Category;
import win.blade.core.module.api.Module;
import win.blade.core.module.api.ModuleInfo;
import win.blade.core.module.api.NonRegistrable;
import win.blade.common.utils.math.MathUtility;

import java.awt.*;

@ModuleInfo(
        name = "InfoHud",
        category = Category.RENDER,
        desc = "Отображает координаты и скорость"
)
public class InfoHud extends Module implements MinecraftInstance, NonRegistrable {
    private wUIElement wUIElement;
    Color purple;

    public InfoHud() {
        this.wUIElement = new InfoHud.wUIElement("InfoHud", 10, 10, 100, 22);
        this.wUIElement.setAnimation(0.4, Easing.EASE_OUT_CUBIC);
    }

    @EventHandler
    public void onRenderScreen(RenderEvents.Screen e) {
        if (e == null || e.getDrawContext() == null || mc.player == null) {
            return;
        }

        wUIElement.update(true);
        wUIElement.render(e.getDrawContext());
    }

    @EventHandler
    public void onMouse(InputEvents.Mouse event) {
        if (wUIElement != null) {
            wUIElement.onMouse(event);
        }
    }

    private class wUIElement extends InteractiveUIElement {

        private AbstractTexture arrowTexture = null;

        public wUIElement(String id, float x, float y, float width, float height) {
            super(id, x, y, width, height);
        }

        @Override
        public void renderContent(DrawContext context) {

            if (arrowTexture == null) {
                arrowTexture = mc.getTextureManager().getTexture(Identifier.of("blade", "textures/arrpwL2.png"));
            }

            Matrix4f matrix = context.getMatrices()
                    .peek().getPositionMatrix();

            purple = new Color(102, 60, 255);
            
            float padding = 10f;
            
            float gap = 2;

            String[] coord = {
                    String.valueOf((int) mc.player.getX()),
                    String.valueOf((int) mc.player.getY()),
                    String.valueOf((int) mc.player.getZ())
            };

            String bpsT = String.valueOf(MathUtility.round(MovementUtility.getHorizontalSpeed() * 20.0, 1));
            setHeight(23f);

            float xyzLabelW = FontType.popins_medium.get().getWidth("XYZ", 7);
            float tW = 0;
            float[] coordW = new float[coord.length];

            for (int i = 0; i < coord.length; i++) {
                float w = FontType.popins_regular.get().getWidth(coord[i], 7);
                coordW[i] = w;
                tW += w;
            }

            float arrow = gap + 8 + gap;
            int arrowCount = coord.length > 1 ? coord.length - 1 : 0;

            float mainW = padding + xyzLabelW + 6 + tW + (arrowCount * arrow) + padding;

            float blockW = padding + FontType.popins_medium.get().getWidth("BPS", 7) + gap + 8 + gap + FontType.popins_regular.get().getWidth(bpsT, 7) + padding;

            setWidth(mainW + 6f + blockW);

            QuadColorState bgColor = new QuadColorState(
                    new Color(23, 20, 35),
                    new Color(20, 18, 27),
                    new Color(20, 18, 27),
                    new Color(19, 17, 29)
            );

            QuadRadiusState radius = new QuadRadiusState(6.5f);

            BuiltRectangle mainBackground = Builder.rectangle()
                    .size(new SizeState(mainW, getHeight()))
                    .color(bgColor)
                    .radius(radius)
                    .smoothness(1.0f)
                    .build();
            mainBackground.render(matrix, getX(), getY());
            

            BuiltBorder mainBorder = Builder.border()
                    .size(new SizeState(mainW, getHeight()))
                    .color(new QuadColorState(new Color(170, 160, 200, 25)))
                    .radius(radius)
                    .thickness(1)
                    .build();
            mainBorder.render(matrix, getX(), getY());

            float curX = getX() + padding;
            float tY = getY() + (getHeight() - 7) / 2f - 2;

            Builder.text()
                    .font(FontType.popins_medium.get())
                    .text("XYZ")
                    .color(purple.getRGB())
                    .size(7)
                    .build()
                    .render(matrix, curX, tY);

            curX += xyzLabelW + 6;

            for (int i = 0; i < coord.length; i++) {
                Builder.text()
                        .font(FontType.popins_regular.get())
                        .text(coord[i])
                        .color(-1)
                        .size(7)
                        .build()
                        .render(matrix, curX, tY);

                curX += coordW[i];

                if (i < coord.length - 1) {
                    curX += gap;
                    Builder.texture()
                            .size(new SizeState(8, 8))
                            .texture(0.0f, 0.0f, 1.0f, 1.0f, arrowTexture)
                            .build()
                            .render(matrix, curX, tY + 1.65f);
                    curX += 8 + gap;
                }
            }

            float bpsX = getX() + mainW + 2;

            BuiltRectangle bpsBackground = Builder.rectangle()
                    .size(new SizeState(blockW, 23f))
                    .color(bgColor)
                    .radius(radius)
                    .smoothness(1.0f)
                    .build();
            bpsBackground.render(matrix, bpsX, getY());

            BuiltBorder bpsBorder = Builder.border()
                    .size(new SizeState(blockW, 23f))
                    .color(new QuadColorState(new Color(170, 160, 200, 25)))
                    .radius(radius)
                    .thickness(1)
                    .build();
            bpsBorder.render(matrix, bpsX, getY());

            curX = bpsX + padding;

            Builder.text()
                    .font(FontType.popins_medium.get())
                    .text("BPS")
                    .color(purple.getRGB())
                    .size(7)
                    .build()
                    .render(matrix, curX, tY);

            curX += FontType.popins_medium.get().getWidth("BPS", 7) + gap;

            Builder.texture()
                    .size(new SizeState(8, 8))
                    .texture(0.0f, 0.0f, 1.0f, 1.0f, arrowTexture)
                    .build()
                    .render(matrix, curX, tY + 1.65f);

            curX += 8 + gap;

            Builder.text()
                    .font(FontType.popins_regular.get())
                    .text(bpsT)
                    .color(-1)
                    .size(7)
                    .build()
                    .render(matrix, curX, tY);
        }

        @Override
        public void update() {
            super.update();
        }

        @Override
        public void onMouse(InputEvents.Mouse event) {
            super.onMouse(event);
        }
    }
}