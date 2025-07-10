package win.blade.core.module.storage.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import win.blade.common.gui.impl.menu.settings.impl.SliderSetting;
import win.blade.common.gui.impl.menu.settings.impl.BooleanSetting;
import win.blade.common.utils.render.builders.impl.BorderBuilder;
import win.blade.common.utils.render.builders.states.QuadColorState;
import win.blade.common.utils.render.builders.states.QuadRadiusState;
import win.blade.common.utils.render.builders.states.SizeState;
import win.blade.common.utils.render.renderers.impl.BuiltBorder;
import win.blade.core.event.controllers.EventHandler;
import win.blade.core.event.impl.minecraft.UpdateEvents;
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

    private final SliderSetting lifetime = new SliderSetting(this, "Время жизни круга в мс.", 2000, 500, 5000, 100);

    private final BooleanSetting deepestLight = new BooleanSetting(this, "Свечение", true);

    private final List<Circle> circles = new ArrayList<>();

    private record Circle(Vec3d position, long creationTime) {}

    @EventHandler
    public void onJump(PlayerActionEvents.Jump event) {
        if (mc.player == null || mc.world == null) return;
        circles.add(new Circle(mc.player.getPos(), System.currentTimeMillis()));
    }

    @EventHandler
    public void onUpdate(UpdateEvents.PlayerUpdate event) {
        if (mc.player == null || mc.world == null) return;
        if (circles.isEmpty()) return;
        float maxLifetime = lifetime.getValue() * 100;
        circles.removeIf(circle -> System.currentTimeMillis() - circle.creationTime > maxLifetime);
    }

    @EventHandler
    public void onRender3D(RenderEvents.World event) {
        if (circles.isEmpty() || mc.player == null || mc.world == null) return;

        MatrixStack matrices = event.getMatrixStack();
        Camera camera = mc.gameRenderer.getCamera();
        Vec3d cameraPos = camera.getPos();

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(770, 771);
        RenderSystem.depthMask(false);

        matrices.push();
        matrices.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        for (Circle circle : circles) {
            renderCircle(matrices, circle);
        }

        matrices.pop();
        RenderSystem.depthMask(true);
    }

    private void renderCircle(MatrixStack matrices, Circle circle) {
        float circleRadiusValue = 1.6f;
        float alphaPC = 1.0f;

        matrices.push();
        matrices.translate(circle.position.x, circle.position.y, circle.position.z);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90.0F));

        matrices.pop();

        if (deepestLight.getValue()) {
            matrices.push();
            matrices.translate(circle.position.x, circle.position.y, circle.position.z);
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90.0F));

            int numLayers = 15;
            float extMaxY = 0.5f;
            float immersiveIntense = 0.3f;

            for (int i = 1; i <= numLayers; i++) {
                float iPC = (float) i / numLayers;
                float extY = extMaxY * iPC;
                float aPC = alphaPC * immersiveIntense * (1 - iPC);
                if (aPC * 255 < 1) continue;
                int layerAlpha = (int) (255 * aPC);
                float radiusPost = circleRadiusValue;
                float layerSide = radiusPost * 2.0f;
                float layerX = -radiusPost;
                float layerY = -radiusPost;
                float layerZ = -extY;
                int layerColor = new Color(255, 0, 0, layerAlpha).getRGB();

                BuiltBorder layer = new BorderBuilder()
                        .size(new SizeState(layerSide, layerSide))
                        .radius(new QuadRadiusState(0.6))
                        .color(new QuadColorState(layerColor))
                        .smoothness(1.0f, 1.0f)
                        .thickness(0.1f)
                        .build();

                layer.render(matrices.peek().getPositionMatrix(), layerX, layerY, layerZ);
            }

            matrices.pop();
        }
    }

    @Override
    protected void onDisable() {
        circles.clear();
    }
}