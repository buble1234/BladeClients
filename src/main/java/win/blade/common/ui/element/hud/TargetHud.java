package win.blade.common.ui.element.hud;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import org.joml.Matrix4f;
import win.blade.common.ui.element.InteractiveUIElement;
import win.blade.common.utils.math.animation.Easing;
import win.blade.common.utils.minecraft.MinecraftInstance;
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.builders.states.QuadColorState;
import win.blade.common.utils.render.builders.states.QuadRadiusState;
import win.blade.common.utils.render.builders.states.SizeState;
import win.blade.common.utils.render.msdf.FontType;
import win.blade.core.event.controllers.EventHandler;
import win.blade.core.event.impl.input.InputEvents;
import win.blade.core.event.impl.render.RenderEvents;
import win.blade.core.module.api.Category;
import win.blade.core.module.api.Module;
import win.blade.core.module.api.ModuleInfo;
import win.blade.core.module.api.NonRegistrable;

import java.awt.*;

@ModuleInfo(
        name = "TargetHud",
        category = Category.RENDER,
        desc = "Отображает информацию о цели"
)
public class TargetHud extends Module implements MinecraftInstance, NonRegistrable {

    private wUIElement wUIElement;

    public TargetHud() {
        this.wUIElement = new TargetHud.wUIElement("TargetHud", 10, 10, 130f, 50f);
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

        public wUIElement(String id, float x, float y, float width, float height) {
            super(id, x, y, width, height);
        }

        @Override
        public void renderContent(DrawContext context) {
            Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();

            LivingEntity target = getTarget();
            if (target == null) return;

            setWidth(130f);
            setHeight(50);

            float paddingV = 4.5f;
            float paddingH = 10f;
            float gap = 10;

            float headSize = 32;

            Identifier textureId;
            if (target instanceof AbstractClientPlayerEntity player) {
                textureId = player.getSkinTextures().texture();
            } else {
                textureId = Identifier.ofVanilla("textures/entity/steve.png");
            }

            AbstractTexture texture = mc.getTextureManager().getTexture(textureId);
            String name = target.getName().getString();
            float healthPercent = target.getHealth() / target.getMaxHealth();

            Builder.rectangle()
                    .size(new SizeState(getWidth(), getHeight()))
                    .color(new QuadColorState(
                            new Color(23, 20, 35),
                            new Color(20, 18, 27),
                            new Color(20, 18, 27),
                            new Color(19, 17, 29)
                    ))
                    .radius(new QuadRadiusState(6.5f))
                    .smoothness(1.0f)
                    .build()
                    .render(matrix, getX(), getY());

            float headX = getX() + paddingH;
            float headY = getY() + paddingV + 4;

            Builder.texture()
                    .size(new SizeState(headSize, headSize))
                    .texture(8f / 64f, 8f / 64f, 8f / 64f, 8f / 64f, texture)
                    .radius(new QuadRadiusState(2))
                    .build()
                    .render(matrix, headX, headY);

            Builder.texture()
                    .size(new SizeState(headSize, headSize))
                    .texture(40f / 64f, 8f / 64f, 8f / 64f, 8f / 64f, texture)
                    .radius(new QuadRadiusState(8))
                    .build()
                    .render(matrix, headX, headY);

            float infoX = headX + headSize + gap;
            float healthBarWidth = getX() + getWidth() - paddingH - infoX;

            float tBH = 6.5f + 4 + 6 + 3 + 4;
            float tYStart = getY() + (getHeight() - tBH) / 2f;

            Builder.text()
                    .font(FontType.popins_medium.get())
                    .text(name)
                    .color(Color.WHITE.getRGB())
                    .size(7)
                    .build()
                    .render(matrix, infoX, tYStart);

            float hpY = tYStart + 6.5f + 4;
            Builder.text()
                    .font(FontType.popins_regular.get())
                    .text(Math.round(target.getHealth()) + "hp")
                    .color(new Color(102, 60, 255))
                    .size(6)
                    .build()
                    .render(matrix, infoX, hpY);

            float barY = hpY + 6 + 3;

            Builder.rectangle()
                    .size(new SizeState(healthBarWidth, 4))
                    .color(new QuadColorState(new Color(60, 60, 60, 150)))
                    .radius(new QuadRadiusState(1))
                    .smoothness(1.0f)
                    .build()
                    .render(matrix, infoX, barY);

            float barFillW = healthBarWidth * healthPercent;
            if (barFillW > 0) {
                Builder.rectangle()
                        .size(new SizeState(barFillW, 4))
                        .color(new QuadColorState(new Color(102, 60, 255)))
                        .radius(new QuadRadiusState(1))
                        .build()
                        .render(matrix, infoX, barY);
            }

            Builder.border()
                    .size(new SizeState(getWidth(), getHeight()))
                    .color(new QuadColorState(new Color(170, 160, 200, 25)))
                    .radius(new QuadRadiusState(6.5f))
                    .thickness(1)
                    .build()
                    .render(matrix, getX(), getY());
        }

        private LivingEntity getTarget() {
            if (mc.crosshairTarget instanceof EntityHitResult hit && hit.getEntity() instanceof LivingEntity living && living != mc.player) {
                return living;
            }
            return mc.player;
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