package win.blade.core.module.storage.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import win.blade.common.gui.impl.gui.setting.implement.ColorSetting;
import win.blade.common.gui.impl.gui.setting.implement.ValueSetting;
import win.blade.common.utils.math.TimerUtil;
import win.blade.common.utils.math.animation.Easing;
import win.blade.common.utils.render.shader.ShaderHelper;
import win.blade.common.utils.render.shader.storage.JumpCircleShader;
import win.blade.core.event.controllers.EventHandler;
import win.blade.core.event.impl.player.PlayerActionEvents;
import win.blade.core.event.impl.render.RenderEvents;
import win.blade.core.module.api.Category;
import win.blade.core.module.api.Module;
import win.blade.core.module.api.ModuleInfo;

import java.awt.Color;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@ModuleInfo(name = "JumpCircles", category = Category.RENDER, desc = "Показывает круги под ногами при прыжке.")
public class JumpCirclesModule extends Module {

    private final ValueSetting lifetime = new ValueSetting("Время жизни", "Как долго живет круг.").setValue(800f).range(200f, 3000f);
    private final ValueSetting radius = new ValueSetting("Радиус", "Конечный радиус круга.").setValue(1.2f).range(0.5f, 5f);
    private final ValueSetting thickness = new ValueSetting("Толщина", "Толщина линии круга.").setValue(0.2f).range(0.02f, 1f);
    private final ColorSetting color = new ColorSetting("Цвет", "Цвет круга.").value(new Color(255, 170, 240).getRGB());

    private final List<Circle> circles = new CopyOnWriteArrayList<>();

    public JumpCirclesModule() {
        addSettings(lifetime, radius, thickness, color);
    }

    @Override
    public void onDisable() {
        circles.clear();
    }

    @EventHandler
    public void onJump(PlayerActionEvents.Jump event) {
        if (mc.player != null && mc.player.isOnGround()) {
            addCircle(mc.player);
        }
    }

    @EventHandler
    public void onRender3D(RenderEvents.World event) {
        if (circles.isEmpty() || mc.player == null || mc.gameRenderer == null) return;

        circles.removeIf(Circle::isFinished);
        if (circles.isEmpty()) return;

        ShaderHelper.initShadersIfNeeded();
        if (!ShaderHelper.isInitialized()) return;

        setupRender();

        MatrixStack matrices = event.getMatrixStack();
        Vec3d camPos = mc.gameRenderer.getCamera().getPos();

        Matrix4f projectionMatrix = RenderSystem.getProjectionMatrix();

        JumpCircleShader shader = ShaderHelper.getJumpCircleShader();
        shader.bind();
        shader.setUniformMatrix4f("u_ProjMat", false, projectionMatrix);

        for (Circle circle : circles) {
            drawCircle(matrices, circle, camPos);
        }

        shader.unbind();
        cleanupRender();
    }

    private void drawCircle(MatrixStack matrices, Circle circle, Vec3d camPos) {
        float progress = circle.getProgress();

        matrices.push();
        matrices.translate(
                circle.pos.getX() - camPos.getX(),
                circle.pos.getY() - camPos.getY(),
                circle.pos.getZ() - camPos.getZ()
        );
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90));

        float currentRadius = radius.getValue() * progress;
        matrices.scale(currentRadius, currentRadius, 1f);

        JumpCircleShader shader = ShaderHelper.getJumpCircleShader();
        shader.setUniformMatrix4f("u_ModelMat", false, matrices.peek().getPositionMatrix());

        float normalizedRadius = 0.5f - (thickness.getValue() / 2f);
        float normalizedSoftness = thickness.getValue();
        shader.setUniforms(progress, color.getColor(), normalizedRadius, normalizedSoftness);

        drawUnitQuad();

        matrices.pop();
    }

    private void drawUnitQuad() {
        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        bufferBuilder.vertex(-1.0f, -1.0f, 0.0f).texture(0.0f, 0.0f);
        bufferBuilder.vertex(1.0f, -1.0f, 0.0f).texture(1.0f, 0.0f);
        bufferBuilder.vertex(1.0f, 1.0f, 0.0f).texture(1.0f, 1.0f);
        bufferBuilder.vertex(-1.0f, 1.0f, 0.0f).texture(0.0f, 1.0f);
        BufferRenderer.draw(bufferBuilder.end());
    }

    private void addCircle(Entity entity) {
        if (mc.world == null) return;
        Vec3d vec = entity.getPos().add(0.0, 0.01, 0.0);
        final BlockPos pos = BlockPos.ofFloored(vec);
        final BlockState state = mc.world.getBlockState(pos);
        if (state.getBlock() == Blocks.SNOW) {
            vec = vec.add(0.0, 0.125, 0.0);
        }
        circles.add(new Circle(vec, lifetime.getValue()));
    }

    private void setupRender() {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
        RenderSystem.disableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);
    }

    private void cleanupRender() {
        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.defaultBlendFunc();
    }

    private static class Circle {
        private final Vec3d pos;
        private final TimerUtil timer = new TimerUtil();
        private final float lifetime;

        public Circle(Vec3d pos, float lifetime) {
            this.pos = pos;
            this.lifetime = lifetime;
        }

        public float getProgress() {
            float rawProgress = Math.min(timer.elapsedTime() / lifetime, 1.0f);
            return (float) Easing.EASE_OUT_CUBIC.ease(rawProgress);
        }

        public boolean isFinished() {
            return timer.hasReached((long) lifetime);
        }
    }
}