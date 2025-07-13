package win.blade.core.module.storage.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import win.blade.common.gui.impl.menu.settings.impl.SliderSetting;
import win.blade.common.utils.math.animation.Animation;
import win.blade.common.utils.math.animation.Easing;
import win.blade.common.utils.render.builders.impl.BorderBuilder;
import win.blade.common.utils.render.builders.states.QuadColorState;
import win.blade.common.utils.render.builders.states.QuadRadiusState;
import win.blade.common.utils.render.builders.states.SizeState;
import win.blade.common.utils.render.renderers.impl.BuiltBorder;
import win.blade.core.event.controllers.EventHandler;
import win.blade.core.event.impl.player.PlayerActionEvents;
import win.blade.core.event.impl.render.RenderEvents;
import win.blade.core.module.api.Category;
import win.blade.core.module.api.Module;
import win.blade.core.module.api.ModuleInfo;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * Автор: NoCap
 * Дата создания: 04.07.2025
 */
@ModuleInfo(
        name = "JumpCircles",
        category = Category.RENDER
)
public class JumpCircles extends Module {

    private final SliderSetting lifetime = new SliderSetting(this, "Время жизни круга в мс.", 2000, 1000, 5000, 100);

    private final List<Circle> circles = new ArrayList<>();

    private class Circle {
        private final Vec3d position;
        private final long creationTime;
        private final Animation alphaAnimation;
        private boolean fadingOut = false;

        Circle(Vec3d position) {
            this.position = position;
            this.creationTime = System.currentTimeMillis();
            this.alphaAnimation = new Animation();
            this.alphaAnimation.run(255, 0.5f, Easing.EASE_OUT_SINE);
        }

        void update() {
            alphaAnimation.update();

            long life = System.currentTimeMillis() - creationTime;
            float maxLifetime = lifetime.getValue();
            float fadeOutTimeMillis = 0.5f * 1000.0f;

            if (!fadingOut && life > maxLifetime - fadeOutTimeMillis) {
                fadingOut = true;
                alphaAnimation.run(0, 0.8f, Easing.EASE_IN_SINE);
            }
        }

        boolean isFinished() {
            return fadingOut && !alphaAnimation.isAnimating();
        }

        int getAlpha() {
            return (int) Math.max(0, Math.min(255, alphaAnimation.get()));
        }
    }

    @EventHandler
    public void onJump(PlayerActionEvents.Jump event) {
        if (mc.player == null || mc.world == null) return;
        circles.add(new Circle(mc.player.getPos()));
    }

    @EventHandler
    public void onRender3D(RenderEvents.World event) {
        if (circles.isEmpty() || mc.player == null) return;

        circles.forEach(Circle::update);
        circles.removeIf(Circle::isFinished);

        MatrixStack matrices = event.getMatrixStack();
        Camera camera = mc.gameRenderer.getCamera();
        Vec3d cameraPos = camera.getPos();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.depthMask(false);

        matrices.push();
        matrices.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        for (Circle circle : circles) {
            renderCircle(matrices, circle);
        }

        matrices.pop();
        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    private void renderCircle(MatrixStack matrices, Circle circle) {
        int alpha = circle.getAlpha();
        if (alpha <= 0) return;

        matrices.push();
        matrices.translate(circle.position.x, circle.position.y, circle.position.z);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90.0F));

        Color baseColor = new Color(237, 172, 255);
        int animatedColor = new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), alpha).getRGB();

        BuiltBorder border = new BorderBuilder()
                .size(new SizeState(1.2 * 2.0f, 1.2 * 2.0f))
                .radius(new QuadRadiusState(0.2))
                .color(new QuadColorState(animatedColor))
                .smoothness(1f, 0.5f)
                .thickness(0.5f)
                .build();

        border.render(matrices.peek().getPositionMatrix(), -1.6f, -1.6f, 0.0f);
        matrices.pop();
    }

    @Override
    protected void onDisable() {
        circles.clear();
    }
}