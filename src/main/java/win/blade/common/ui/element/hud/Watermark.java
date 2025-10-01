package win.blade.common.ui.element.hud;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import win.blade.common.ui.element.InteractiveUIElement;
import win.blade.common.utils.math.animation.Easing;
import win.blade.common.utils.minecraft.MinecraftInstance;
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.builders.states.QuadColorState;
import win.blade.common.utils.render.builders.states.QuadRadiusState;
import win.blade.common.utils.render.builders.states.SizeState;
import win.blade.common.utils.render.msdf.FontType;
import win.blade.common.utils.render.renderers.impl.BuiltBorder;
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
public class Watermark extends Module implements MinecraftInstance, NonRegistrable {
    private wUIElement wUIElement;
    Color black,purple,gray;
    String servetT,ping;


    public Watermark() {
        this.wUIElement = new Watermark.wUIElement("Watermark", 10, 10, 100, 22);
        this.wUIElement.setAnimation(0.4, Easing.EASE_OUT_CUBIC);
    }


    @EventHandler
    public void onRenderScreen(RenderEvents.Screen.POST e) {
        if (e == null || e.getDrawContext() == null || mc.player == null) {
            return;
        }

        wUIElement.update();
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
                arrowTexture = mc.getTextureManager().getTexture(Identifier.of("blade", "textures/arrpwl2.png"));
            }

            Matrix4f matrix = context.getMatrices()
                    .peek().getPositionMatrix();

            black = new Color(20,18,27);
            purple = new Color(102,60,255);
            gray = new Color(255, 255, 255, 64);

            float iSize = 10;
            float tSize = 7;

            float padding = 10f;
            float gap = 6f;

            float iconW = FontType.icon2.get().getWidth("a", iSize);

            float nameW = FontType.popins_medium.get().getWidth("cutthroat", tSize);
            float lineW = 1f;

            ping = (mc.isInSingleplayer() || mc.getNetworkHandler() == null || mc.player == null || mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid()) == null) ? "0" : String.valueOf(mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid()).getLatency());

            setHeight(23f);

            float mainW = padding + iconW + gap + lineW + gap + nameW + gap + 5f + gap + FontType.popins_regular.get().getWidth(ping + " ping", tSize) + gap + 5f + gap + FontType.popins_regular.get().getWidth(mc.getCurrentFps() + " fps", tSize) + padding;

            servetT = mc.isInSingleplayer() ? "Singleplayer" : (mc.getCurrentServerEntry() != null ? mc.getCurrentServerEntry().address : "Unknown");

            setWidth(mainW + 6f + 10f * 2 + FontType.popins_regular.get().getWidth(servetT, tSize));

            BuiltRectangle mainBackground = Builder.rectangle()
                    .size(new SizeState(mainW, getHeight()))
                    .color(new QuadColorState(
                            new Color(23, 20, 35),
                            new Color(20, 18, 27),
                            new Color(20, 18, 27),
                            new Color(19, 17, 29)
                    ))
                    .radius(new QuadRadiusState(6.5f))
                    .smoothness(1.0f)
                    .build();
            mainBackground.render(matrix, getX(), getY());

            BuiltBorder mainBorder = Builder.border()
                    .size(new SizeState(mainW, getHeight()))
                    .color(new QuadColorState(new Color(170, 160, 200, 25)))
                    .radius(new QuadRadiusState(6.5f))
                    .thickness(1)
                    .build();
            mainBorder.render(matrix, getX(), getY());



            float curX = getX() + padding;
            float tY = getY() + (getHeight() - tSize) / 2f -2;


            BuiltText icon = Builder.text()
                    .font(FontType.icon2.get())
                    .text("a")
                    .color(purple)
                    .size(iSize)
                    .build();
            icon.render(matrix, curX, getY() + (getHeight() - iSize) / 2f - 1);




            curX += iconW + gap;

            float lineH = getHeight() / 2;
            float lineY = getY() + (getHeight() - lineH) / 2f;

            BuiltRectangle line = Builder.rectangle()
                    .size(new SizeState(lineW, lineH))
                    .color(new QuadColorState(gray))
                    .build();
            line.render(matrix, curX, lineY);

            curX += lineW + gap;

            BuiltText name = Builder.text()
                    .font(FontType.popins_medium.get())
                    .text("cutthroat")
                    .color(-1)
                    .size(tSize)
                    .build();
            name.render(matrix, curX, tY);

            curX += nameW + gap;

            Builder.texture()
                    .size(new SizeState(8,8))
                    .texture(0.0f, 0.0f, 1.0f, 1.0f, arrowTexture)
                    .build()
                    .render(matrix, curX, tY +2);

            curX += 5f + gap;

            BuiltText pingT = Builder.text()
                    .font(FontType.popins_regular.get())
                    .text(ping + " ping")
                    .color(new Color(140,136,154))
                    .size(tSize)
                    .build();
            pingT.render(matrix, curX, tY);

            curX += FontType.popins_regular.get().getWidth(ping + " ping", tSize) + gap;

            Builder.texture()
                    .size(new SizeState(8,8))
                    .texture(0.0f, 0.0f, 1.0f, 1.0f, arrowTexture)
                    .build()
                    .render(matrix, curX, tY + 2);

            curX += 5f + gap;

            BuiltText fpsT = Builder.text()
                    .font(FontType.popins_regular.get())
                    .text(mc.getCurrentFps() + " fps")
                    .color(new Color(140,136,154))
                    .size(tSize)
                    .build();
            fpsT.render(matrix, curX, tY);

            float serverX = getX() + mainW + 2;

            BuiltRectangle serverBackground = Builder.rectangle()
                    .size(new SizeState(10f * 2 + FontType.popins_regular.get().getWidth(servetT, tSize), 23f))
                    .color(new QuadColorState(
                            new Color(23, 20, 35),
                            new Color(20, 18, 27),
                            new Color(20, 18, 27),
                            new Color(19, 17, 29)
                    ))
                    .radius(new QuadRadiusState(6.5f))
                    .smoothness(1.0f)
                    .build();
            serverBackground.render(matrix, serverX, getY());

            BuiltBorder serverBorder = Builder.border()
                    .size(new SizeState(10f * 2 + FontType.popins_regular.get().getWidth(servetT, tSize), 23f))
                    .color(new QuadColorState(new Color(170, 160, 200, 25)))
                    .radius(new QuadRadiusState(6.5f))
                    .thickness(1)
                    .build();
            serverBorder.render(matrix, serverX, getY());

            BuiltText serverT = Builder.text()
                    .font(FontType.popins_regular.get())
                    .text(servetT)
                    .color(new Color(140,136,154))
                    .size(tSize)
                    .build();
            serverT.render(matrix, serverX + 10f, tY);
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