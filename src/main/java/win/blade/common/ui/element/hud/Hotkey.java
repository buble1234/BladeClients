package win.blade.common.ui.element.hud;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import win.blade.common.ui.element.InteractiveUIElement;
import win.blade.common.utils.math.animation.Easing;
import win.blade.common.utils.minecraft.MinecraftInstance;
import win.blade.common.utils.render.Stencil;
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.builders.states.QuadColorState;
import win.blade.common.utils.render.builders.states.QuadRadiusState;
import win.blade.common.utils.render.builders.states.SizeState;
import win.blade.common.utils.render.msdf.FontType;
import win.blade.common.utils.render.renderers.impl.BuiltBorder;
import win.blade.common.utils.render.renderers.impl.BuiltRectangle;
import win.blade.common.utils.render.renderers.impl.BuiltText;
import win.blade.core.Manager;
import win.blade.core.event.controllers.EventHandler;
import win.blade.core.event.impl.input.InputEvents;
import win.blade.core.event.impl.render.RenderEvents;
import win.blade.core.module.api.Category;
import win.blade.core.module.api.Module;
import win.blade.core.module.api.ModuleInfo;
import win.blade.core.module.api.NonRegistrable;
import win.blade.common.utils.keyboard.Keyboard;
import win.blade.core.module.storage.render.InterfaceModule;

import java.awt.*;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@ModuleInfo(
        name = "Hotkey",
        category = Category.RENDER,
        desc = "Отображает включенные клавишы"
)
public class Hotkey extends Module implements MinecraftInstance, NonRegistrable {

    private wUIElement wUIElement;

    public Hotkey() {
        this.wUIElement = new Hotkey.wUIElement("Hotkey", 10, 10, 107, 22);
        this.wUIElement.setAnimation(0.4, Easing.EASE_OUT_CUBIC);
    }

    @EventHandler
    public void onRenderScreen(RenderEvents.Screen.POST e) {
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

        private AbstractTexture maskTexture = null;

        public wUIElement(String id, float x, float y, float width, float height) {
            super(id, x, y, width, height);
        }

        @Override
        public void renderContent(DrawContext context) {

            if (maskTexture == null) {
                maskTexture = mc.getTextureManager().getTexture(Identifier.of("blade", "textures/hotmask.png"));
            }

            Matrix4f matrix = context.getMatrices()
                    .peek().getPositionMatrix();

            Color gray = new Color(255, 255, 255, 64);
            Color buttonBg = new Color(28, 26, 37, 255);

            float titleSize = 6;
            float itemSize = 7;
            float itemFontSize = 5.5f;

            float vPadding = 7.5f;
            float hPadding = 10f;
            float headerGap = 6f;

            float gap = 2f;
            float lineW = 1f;

            List<Module> activeModules = Manager.moduleManager.values().stream()
                    .filter(Module::isEnabled)
                    .filter(m -> m.keybind() != 0)
                    .filter(m -> !(m instanceof InterfaceModule))
                    .sorted(Comparator.comparing(Module::name))
                    .collect(Collectors.toList());

            float headerH = titleSize + 2f;
            float itemH = itemSize + 6;

            setWidth(107);

            if (activeModules.isEmpty()) {
                setHeight(23);
            } else {
                float totalH = vPadding + headerH + vPadding + headerGap + lineW + headerGap + (itemH * activeModules.size()) + (gap * (activeModules.size() - 1));
                setHeight(totalH);
            }

            BuiltRectangle background = Builder.rectangle()
                    .size(new SizeState(getWidth(), getHeight()))
                    .color(new QuadColorState(
                            new Color(23, 20, 35),
                            new Color(20, 18, 27),
                            new Color(20, 18, 27),
                            new Color(19, 17, 29)
                    ))
                    .radius(new QuadRadiusState(6.5f))
                    .smoothness(1.0f)
                    .build();

            Stencil.push();
            background.render(matrix, getX(), getY());
            Stencil.read(1);

            background.render(matrix, getX(), getY());

            float maskW = 39;

            Builder.texture()
                    .size(new SizeState(maskW, 39))
                    .texture(0.0f, 0.0f, 1.0f, 1.0f, maskTexture)
                    .radius(new QuadRadiusState(0, 0, 0, 5))
                    .build()
                    .render(matrix, getX() + getWidth() - maskW, getY());

            float curY = getY() + vPadding;

            BuiltText hot = Builder.text()
                    .font(FontType.popins_regular.get())
                    .text("Hot Keys")
                    .color(-1)
                    .size(titleSize)
                    .build();

            hot.render(matrix, getX() + hPadding, curY + (headerH - titleSize) / 2f - 1f);

            if (!activeModules.isEmpty()) {
                curY += headerH + headerGap;

                BuiltRectangle line = Builder.rectangle()
                        .size(new SizeState(getWidth() - hPadding * 2, lineW))
                        .color(new QuadColorState(gray))
                        .build();
                line.render(matrix, getX() + hPadding, curY);

                curY += lineW + headerGap;

                for (Module m : activeModules) {
                    String name = m.name();
                    String keyStr = Keyboard.getKeyName(m.keybind());

                    BuiltText nameText = Builder.text()
                            .font(FontType.popins_regular.get())
                            .text(name)
                            .color(-1)
                            .size(itemFontSize)
                            .build();
                    nameText.render(matrix, getX() + hPadding, curY + (itemH - itemFontSize) / 2f  - 1.65f);

                    float keyW = FontType.popins_regular.get().getWidth(keyStr, itemFontSize);
                    float bW = (6f + 21 + 6f);

                    BuiltRectangle backR = Builder.rectangle()
                            .size(new SizeState(bW, itemH))
                            .color(new QuadColorState(buttonBg))
                            .radius(new QuadRadiusState(3))
                            .smoothness(1.0f)
                            .build();
                    backR.render(matrix, getX() + getWidth() - hPadding - bW, curY);

                    BuiltText keyText = Builder.text()
                            .font(FontType.popins_regular.get())
                            .text(keyStr)
                            .color(-1)
                            .size(itemFontSize)
                            .build();
                    keyText.render(matrix, getX() + getWidth() - hPadding - bW + (bW - keyW) / 2f, curY + (itemH - itemFontSize) / 2f - 1.65f);

                    if (activeModules.indexOf(m) < activeModules.size() - 1) {
                        curY += itemH + gap;
                    } else {
                        curY += itemH;
                    }
                }
            }

            Stencil.pop();

            BuiltBorder border = Builder.border()
                    .size(new SizeState(getWidth(), getHeight()))
                    .color(new QuadColorState(new Color(170, 160, 200, 25)))
                    .radius(new QuadRadiusState(6.5f))
                    .thickness(1)
                    .build();
            border.render(matrix, getX(), getY());
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