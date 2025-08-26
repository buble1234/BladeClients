package win.blade.common.ui.element.hud;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import win.blade.common.ui.element.InteractiveUIElement;
import win.blade.common.utils.math.animation.Animation;
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

        private final Animation scaleAnimation = new Animation();
        private final Animation widthAnimation = new Animation(70);
        private AbstractTexture maskTexture = null;

        public wUIElement(String id, float x, float y, float width, float height) {
            super(id, x, y, width, height);
        }

        @Override
        public void renderContent(DrawContext context) {
            List<Module> activeModules = getActiveModulesWithBounds();
            boolean isInChat = mc.currentScreen instanceof ChatScreen;
            boolean shouldShow = !activeModules.isEmpty() || isInChat;

            if (shouldShow && scaleAnimation.getToValue() != 1.0) {
                scaleAnimation.run(1.0, 0.35, Easing.EASE_OUT_BACK);
                widthAnimation.run(107, 0.35, Easing.EASE_OUT_BACK);
            } else if (!shouldShow && scaleAnimation.getToValue() != 0.0) {
                scaleAnimation.run(0.0, 0.35, Easing.EASE_IN_BACK);
                widthAnimation.run(70, 0.35, Easing.EASE_IN_BACK);
            }

            scaleAnimation.update();
            widthAnimation.update();

            if (scaleAnimation.get() <= 0.01f) return;

            if (maskTexture == null) {
                maskTexture = mc.getTextureManager().getTexture(Identifier.of("blade", "textures/hotmask.png"));
            }

            Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();

            float scale = scaleAnimation.get();
            float animatedWidth = widthAnimation.get();

            float centerX = getX() + getWidth() / 2f;
            float centerY = getY() + getHeight() / 2f;

            context.getMatrices().push();
            context.getMatrices().translate(centerX, centerY, 0);
            context.getMatrices().scale(scale, scale, 1f);
            context.getMatrices().translate(-centerX, -centerY, 0);

            matrix = context.getMatrices().peek().getPositionMatrix();

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

            float headerH = titleSize + 2f;
            float itemH = itemSize + 6;

            setWidth(animatedWidth);

            List<ModuleInfo> displayModules;
            if (isInChat && activeModules.isEmpty()) {
                displayModules = getExampleModules();
            } else {
                displayModules = activeModules.stream()
                        .map(m -> new ModuleInfo(m.name(), Keyboard.getKeyName(m.keybind())))
                        .collect(Collectors.toList());
            }

            if (displayModules.isEmpty()) {
                setHeight(23);
            } else {
                float totalH = vPadding + headerH + vPadding + headerGap + lineW + headerGap + (itemH * displayModules.size()) + (gap * (displayModules.size() - 1));
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

            if (!displayModules.isEmpty()) {
                curY += headerH + headerGap;

                BuiltRectangle line = Builder.rectangle()
                        .size(new SizeState(getWidth() - hPadding * 2, lineW))
                        .color(new QuadColorState(gray))
                        .build();
                line.render(matrix, getX() + hPadding, curY);

                curY += lineW + headerGap;

                for (ModuleInfo moduleInfo : displayModules) {
                    String name = moduleInfo.name;
                    String keyStr = moduleInfo.key;

                    BuiltText nameText = Builder.text()
                            .font(FontType.popins_regular.get())
                            .text(name)
                            .color(-1)
                            .size(itemFontSize)
                            .build();
                    nameText.render(matrix, getX() + hPadding, curY + (itemH - itemFontSize) / 2f - 1.65f);

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

                    if (displayModules.indexOf(moduleInfo) < displayModules.size() - 1) {
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

            context.getMatrices().pop();
        }

        private List<Module> getActiveModulesWithBounds() {
            return Manager.moduleManager.values().stream()
                    .filter(Module::isEnabled)
                    .filter(m -> m.keybind() > 0)
                    .filter(m -> !(m instanceof InterfaceModule))
                    .sorted(Comparator.comparing(Module::name))
                    .collect(Collectors.toList());
        }

        private List<ModuleInfo> getExampleModules() {
            return List.of(
                    new ModuleInfo("Example", "R"),
                    new ModuleInfo("Example", "G"),
                    new ModuleInfo("Example", "B")
            );
        }

        private static class ModuleInfo {
            final String name;
            final String key;

            ModuleInfo(String name, String key) {
                this.name = name;
                this.key = key;
            }
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
