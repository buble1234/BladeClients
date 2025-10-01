package win.blade.common.ui.element.hud;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringHelper;
import org.joml.Matrix4f;
import win.blade.common.ui.element.InteractiveUIElement;
import win.blade.common.utils.math.animation.Animation;
import win.blade.common.utils.math.animation.Easing;
import win.blade.common.utils.minecraft.MinecraftInstance;
import win.blade.common.utils.render.ScissorManager;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ModuleInfo(
        name = "Potions",
        category = Category.RENDER,
        desc = "Отображает активные эффекты"
)
public class Potions extends Module implements MinecraftInstance, NonRegistrable {

    private wUIElement wUIElement;

    public Potions() {
        this.wUIElement = new Potions.wUIElement("Potions", 10, 10, 107, 22);
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

        private final Animation scaleAnimation = new Animation();
        private final Animation widthAnimation = new Animation(70);
        private AbstractTexture maskTexture = null;

        public wUIElement(String id, float x, float y, float width, float height) {
            super(id, x, y, width, height);
        }

        @Override
        public void renderContent(DrawContext context) {
            List<StatusEffectInstance> activeEffects = getActiveEffects();
            boolean isInChat = mc.currentScreen instanceof ChatScreen;
            boolean shouldShow = !activeEffects.isEmpty() || isInChat;

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
                maskTexture = mc.getTextureManager().getTexture(Identifier.of("blade", "textures/potmask.png"));
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

            float titleSize = 6;
            float itemSize = 5.5f;

            float vPadding = 7.5f;
            float hPadding = 10f;
            float headerGap = 6f;

            float gap = 2f;
            float lineW = 1f;

            List<EffectInfo> displayEffects;
            if (isInChat && activeEffects.isEmpty()) {
                displayEffects = getExampleEffects();
            } else {
                displayEffects = activeEffects.stream()
                        .map(this::createEffectInfo)
                        .collect(Collectors.toList());
            }

            float headerH = titleSize + 2f;
            float itemH = itemSize + 6;
            float iconSize = 10f;

            setWidth(animatedWidth);

            if (displayEffects.isEmpty()) {
                setHeight(23);
            } else {
                float totalH = vPadding + headerH + vPadding + headerGap + lineW + headerGap + (itemH * displayEffects.size()) + (gap * (displayEffects.size() - 1));
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

            ScissorManager.push(matrix, getX(), getY(), getWidth(), getHeight());
            background.render(matrix, getX(), getY());
            background.render(matrix, getX(), getY());

            float maskW = 40;
            Builder.texture()
                    .size(new SizeState(maskW, 51.5f))
                    .texture(0.0f, 0.0f, 1.0f, 1.0f, maskTexture)
                    .radius(new QuadRadiusState(0, 0, 0, 5))
                    .build()
                    .render(matrix, getX() + getWidth() - maskW + 15, getY() - 15);

            float curY = getY() + vPadding;

            BuiltText hot = Builder.text()
                    .font(FontType.popins_regular.get())
                    .text("Potions")
                    .color(-1)
                    .size(titleSize)
                    .build();

            hot.render(matrix, getX() + hPadding, curY + (headerH - titleSize) / 2f - 1f);

            if (!displayEffects.isEmpty()) {
                curY += headerH + headerGap;

                BuiltRectangle line = Builder.rectangle()
                        .size(new SizeState(getWidth() - hPadding * 2, lineW))
                        .color(new QuadColorState(gray))
                        .build();
                line.render(matrix, getX() + hPadding, curY);

                curY += lineW + headerGap;

                for (int i = 0; i < displayEffects.size(); i++) {
                    EffectInfo effectInfo = displayEffects.get(i);
                    String name = effectInfo.name;
                    String durationStr = effectInfo.duration;
                    Identifier iconId = effectInfo.iconId;

                    AbstractTexture iconTexture = mc.getTextureManager().getTexture(iconId);

                    Builder.texture()
                            .size(new SizeState(iconSize, iconSize))
                            .texture(0.0f, 0.0f, 1.0f, 1.0f, iconTexture)
                            .build()
                            .render(matrix, getX() + hPadding, curY + (itemH - iconSize) / 2f);

                    BuiltText nameText = Builder.text()
                            .font(FontType.popins_regular.get())
                            .text(name)
                            .color(-1)
                            .size(itemSize)
                            .build();
                    nameText.render(matrix, getX() + hPadding + iconSize + gap, curY + (itemH - itemSize) / 2f - 1);

                    float timeW = FontType.popins_regular.get().getWidth(durationStr, itemSize);

                    BuiltText timeText = Builder.text()
                            .font(FontType.popins_regular.get())
                            .text(durationStr)
                            .color(-1)
                            .size(itemSize)
                            .build();
                    timeText.render(matrix, getX() + getWidth() - hPadding - timeW, curY + (itemH - itemSize) / 2f - 1);

                    if (i < displayEffects.size() - 1) {
                        curY += itemH + gap;
                    } else {
                        curY += itemH;
                    }
                }
            }

            ScissorManager.pop();

            BuiltBorder border = Builder.border()
                    .size(new SizeState(getWidth(), getHeight()))
                    .color(new QuadColorState(new Color(170, 160, 200, 25)))
                    .radius(new QuadRadiusState(6.5f))
                    .thickness(1)
                    .build();
            border.render(matrix, getX(), getY());

            context.getMatrices().pop();
        }

        private List<StatusEffectInstance> getActiveEffects() {
            Collection<StatusEffectInstance> effectsCollection = new ArrayList<>(mc.player.getStatusEffects());
            return effectsCollection.stream()
                    .sorted(Comparator.comparingInt(StatusEffectInstance::getDuration).reversed())
                    .collect(Collectors.toList());
        }

        private EffectInfo createEffectInfo(StatusEffectInstance effect) {
            StatusEffect type = effect.getEffectType().value();
            String effectId = Registries.STATUS_EFFECT.getId(type).getPath();
            String englishName = Stream.of(effectId.split("_"))
                    .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1))
                    .collect(Collectors.joining(" "));
            int level = effect.getAmplifier() + 1;
            String levelStr = "";
            if (level > 1) {
                if (level <= 10) {
                    levelStr = " " + Text.translatable("enchantment.level." + level).getString();
                } else {
                    levelStr = " " + level;
                }
            }
            String nameStr = englishName + levelStr;
            String durationStr;
            if (effect.isInfinite()) {
                durationStr = "Infinite";
            } else {
                durationStr = StringHelper.formatTicks(effect.getDuration(), mc.world.getTickManager().getTickRate());
            }
            Identifier iconId = Identifier.ofVanilla("textures/mob_effect/" + effectId + ".png");

            return new EffectInfo(nameStr, durationStr, iconId);
        }

        private List<EffectInfo> getExampleEffects() {
            return List.of(
                    new EffectInfo("Example II", "2:30", Identifier.ofVanilla("textures/mob_effect/speed.png")),
                    new EffectInfo("Example III", "1:45", Identifier.ofVanilla("textures/mob_effect/strength.png")),
                    new EffectInfo("Example IV", "0:45", Identifier.ofVanilla("textures/mob_effect/regeneration.png"))
            );
        }

        private static class EffectInfo {
            final String name;
            final String duration;
            final Identifier iconId;

            EffectInfo(String name, String duration, Identifier iconId) {
                this.name = name;
                this.duration = duration;
                this.iconId = iconId;
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
