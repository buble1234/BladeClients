package win.blade.common.ui.element.hud;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import win.blade.common.ui.element.InteractiveUIElement;
import win.blade.common.utils.math.MathUtility;
import win.blade.common.utils.math.TimerUtil;
import win.blade.common.utils.math.animation.Animation;
import win.blade.common.utils.math.animation.Easing;
import win.blade.common.utils.minecraft.MinecraftInstance;
import win.blade.common.utils.render.builders.Builder;
import win.blade.common.utils.render.builders.states.QuadColorState;
import win.blade.common.utils.render.builders.states.QuadRadiusState;
import win.blade.common.utils.render.builders.states.SizeState;
import win.blade.common.utils.render.msdf.FontType;
import win.blade.core.Manager;
import win.blade.core.event.controllers.EventHandler;
import win.blade.core.event.impl.input.InputEvents;
import win.blade.core.event.impl.player.PlayerActionEvents;
import win.blade.core.event.impl.render.RenderEvents;
import win.blade.core.module.api.*;
import win.blade.core.module.api.Module;
import win.blade.core.module.storage.combat.AuraModule;
import win.blade.core.module.storage.render.InterfaceModule;
import win.blade.core.module.storage.player.NameProtectModule;

import java.awt.*;
import java.util.stream.StreamSupport;

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

    @Override
    public void onEnable() {
        super.onEnable();
        if (wUIElement != null) {
            wUIElement.resetTimer();
        }
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
    public void onAttack(PlayerActionEvents.Attack event) {
        if (event.getEntity() instanceof LivingEntity entity) {
            wUIElement.setTarget(entity);
            wUIElement.resetTimer();
        }
    }

    @EventHandler
    public void onMouse(InputEvents.Mouse event) {
        if (wUIElement != null) {
            wUIElement.onMouse(event);
        }
    }

    private class wUIElement extends InteractiveUIElement {

        private final Animation scaleAnimation = new Animation();
        private final TimerUtil timer = TimerUtil.create();
        private LivingEntity target = null;
        private boolean lastOut = true;

        private float savedX, savedY;

        public wUIElement(String id, float x, float y, float width, float height) {
            super(id, x, y, width, height);
            this.savedX = x;
            this.savedY = y;
        }

        public void setTarget(LivingEntity target) {
            this.target = target;
        }

        public void resetTimer() {
            this.timer.reset();
        }

        @Override
        public void setPosition(float x, float y) {
            super.setPosition(x, y);
            this.savedX = x;
            this.savedY = y;
        }

        @Override
        public void setPositionInstantly(float x, float y) {
            super.setPositionInstantly(x, y);
            InterfaceModule interfaceModule = getInterfaceModule();
            boolean projectOnTarget = interfaceModule != null && interfaceModule.getTargetHudSettings().getValue();
            if (!projectOnTarget || target == null || target == mc.player) {
                this.savedX = x;
                this.savedY = y;
            }
        }

        private InterfaceModule getInterfaceModule() {
            return Manager.getModuleManagement().get(InterfaceModule.class);
        }

        private NameProtectModule getNameProtectModule() {
            return Manager.getModuleManagement().get(NameProtectModule.class);
        }

        private String getDisplayName(LivingEntity entity) {
            if (entity == mc.player) {
                NameProtectModule nameProtect = getNameProtectModule();
                if (nameProtect != null && nameProtect.isEnabled()) {
                    return NameProtectModule.getName();
                }
            }
            return entity.getName().getString();
        }

        @Override
        public void renderContent(DrawContext context) {
            LivingEntity killAuraTarget = getKillAuraTarget();
            if (killAuraTarget != null) {
                this.target = killAuraTarget;
                this.timer.reset();
            }

            if (mc.currentScreen instanceof ChatScreen) {
                this.target = mc.player;
                this.timer.reset();
            }

            boolean out = true;

            if (target != null) {
                boolean inWorld = isTargetInWorld(target);
                out = (!inWorld || this.timer.hasReached(1000));
            }

            if (out != lastOut) {
                if (out) {
                    scaleAnimation.run(0, 0.5, Easing.EASE_IN_BACK);
                } else {
                    scaleAnimation.run(1, 0.5, Easing.EASE_OUT_BACK);
                }
                lastOut = out;
            }

            scaleAnimation.update();

            if (scaleAnimation.get() <= 0.01f || target == null) return;

            updatePosition();

            Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();

            float scale = scaleAnimation.get();
            float centerX = getX() + getWidth() / 2f;
            float centerY = getY() + getHeight() / 2f;

            context.getMatrices().push();
            context.getMatrices().translate(centerX, centerY, 0);
            context.getMatrices().scale(scale, scale, 1f);
            context.getMatrices().translate(-centerX, -centerY, 0);

            matrix = context.getMatrices().peek().getPositionMatrix();

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
            String name = getDisplayName(target);
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

            float hpY = tYStart + 7.5f + 4;
            Builder.text()
                    .font(FontType.popins_regular.get())
                    .text(Math.round(target.getHealth()) + "hp")
                    .color(new Color(102, 60, 255))
                    .size(6)
                    .build()
                    .render(matrix, infoX, hpY);

            float barY = hpY + 6 + 3.5f;

            Builder.rectangle()
                    .size(new SizeState(healthBarWidth, 3))
                    .color(new QuadColorState(new Color(60, 60, 60, 150)))
                    .radius(new QuadRadiusState(0.5f))
                    .smoothness(1.0f)
                    .build()
                    .render(matrix, infoX, barY);

            float barFillW = healthBarWidth * healthPercent;
            if (barFillW > 0) {
                Builder.rectangle()
                        .size(new SizeState(barFillW, 3))
                        .color(new QuadColorState(new Color(102, 60, 255)))
                        .radius(new QuadRadiusState(0.5f))
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

            context.getMatrices().pop();
        }

        private void updatePosition() {
            InterfaceModule interfaceModule = getInterfaceModule();
            if (interfaceModule == null) return;

            boolean projectOnTarget = interfaceModule.getTargetHudSettings().getValue();

            if (isBeingDragged()) {
                savedX = getX();
                savedY = getY();
            }

            if (projectOnTarget && target != null && target != mc.player) {
                Vec3d iposition = MathUtility.interpolate(target, mc.getRenderTickCounter().getTickDelta(false));
                double posX = iposition.x;
                double posY = iposition.y + target.getHeight() / 2.0;
                double posZ = iposition.z;

                Vec3d screenPos = MathUtility.worldSpaceToScreenSpace(new Vec3d(posX, posY, posZ));

                if (screenPos != null && screenPos.z > 0 && screenPos.z < 1) {
                    float newX = (float) (screenPos.x - getWidth() / 2);
                    float newY = (float) (screenPos.y - getHeight() - 10);
                    super.setPositionInstantly(newX, newY);
                }

                if (!isTargetInWorld(target) || timer.hasReached(1000)) {
                    this.timer.setElapsed(2000);
                }
            } else {
                if (!isBeingDragged()) {
                    super.setPositionInstantly(savedX, savedY);
                }
            }
        }

        private boolean isTargetInWorld(LivingEntity target) {
            if (mc.world == null || target == null) return false;

            if (target == mc.player) return true;

            try {
                return StreamSupport.stream(mc.world.getEntities().spliterator(), false)
                        .anyMatch(entity -> entity.equals(target));
            } catch (Exception e) {
                return target.isAlive() && !target.isRemoved();
            }
        }

        private LivingEntity getKillAuraTarget() {
            try {
                Module aura = Manager.getModuleManagement().get(AuraModule.class);
                if (aura != null && aura.isEnabled()) {
                    Object target = aura.getClass().getMethod("getCurrentTarget").invoke(aura);
                    if (target instanceof LivingEntity) {
                        return (LivingEntity) target;
                    }
                }
            } catch (Exception e) {
            }
            return null;
        }

        @Override
        public void update() {
            super.update();
        }

        @Override
        public void onMouse(InputEvents.Mouse event) {
            InterfaceModule interfaceModule = getInterfaceModule();

            boolean allowDragging = interfaceModule == null || !interfaceModule.getTargetHudSettings().getValue() || target == null || target == mc.player;

            if (allowDragging) {
                super.onMouse(event);

                if (event.getButton() == 0 && event.getAction() == 0 && isBeingDragged()) {
                    savedX = getX();
                    savedY = getY();
                }
            }
        }
    }
}