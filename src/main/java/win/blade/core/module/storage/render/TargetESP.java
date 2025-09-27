package win.blade.core.module.storage.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import win.blade.common.gui.impl.gui.setting.implement.ColorSetting;
import win.blade.common.gui.impl.gui.setting.implement.SelectSetting;
import win.blade.common.gui.impl.gui.setting.implement.ValueSetting;
import win.blade.common.utils.color.ColorUtility;
import win.blade.common.utils.math.animation.Animation;
import win.blade.common.utils.math.animation.Easing;
import win.blade.common.utils.render.shader.ShaderHelper;
import win.blade.common.utils.render.shader.storage.GhostShader;
import win.blade.common.utils.render.shader.storage.ReflectionShader;
import win.blade.core.Manager;
import win.blade.core.event.controllers.EventHandler;
import win.blade.core.event.impl.render.RenderEvents;
import win.blade.core.module.api.Category;
import win.blade.core.module.api.Module;
import win.blade.core.module.api.ModuleInfo;
import win.blade.core.module.storage.combat.AuraModule;

import java.awt.*;
import java.util.Objects;

@ModuleInfo(name = "TargetESP", category = Category.RENDER, desc = "Рендерит ESP вокруг цели Ауры.")
public class TargetESP extends Module {

    private final SelectSetting mode = new SelectSetting("Режим", "").value("Призраки", "Тор");

    private final ValueSetting speed = new ValueSetting("Скорость", "").setValue(1.5f).range(1f, 3f);
    private final ValueSetting size = new ValueSetting("Размер", "").setValue(0.6f).range(0.5f, 1.1f);
    private final ColorSetting color1 = new ColorSetting("Цвет 1", "").value(new Color(102, 60, 255).getRGB());
    private final ColorSetting color2 = new ColorSetting("Цвет 2", "").value(new Color(102, 60, 255).getRGB());

    private final ValueSetting frequency = new ValueSetting("Частота шума", "").setValue(50f).range(0f, 100f);

    private long startTime = 0;
    private final Animation animation = new Animation();
    private final Animation alphaAnimation = new Animation();
    private Entity lastTarget = null;

    public TargetESP() {
        speed.setVisible(() -> Objects.equals(mode.getSelected(), "Призраки"));
        size.setVisible(() -> Objects.equals(mode.getSelected(), "Призраки"));
        color1.setVisible(() -> Objects.equals(mode.getSelected(), "Призраки"));
        color2.setVisible(() -> Objects.equals(mode.getSelected(), "Призраки"));

        frequency.setVisible(() -> Objects.equals(mode.getSelected(), "Тор"));

        addSettings(mode, speed, size, color1, color2, frequency);
    }

    @Override
    public void onEnable() {
        startTime = System.currentTimeMillis();
        alphaAnimation.set(0);
        lastTarget = null;
    }

    @EventHandler
    public void onRenderWorld(RenderEvents.World event) {
        if (mc.player == null || mc.world == null || mc.gameRenderer == null) return;

        AuraModule aura = Manager.getModuleManagement().get(AuraModule.class);
        Entity target = (aura != null && aura.isEnabled()) ? aura.getCurrentTarget() : null;

        if (target != lastTarget) {
            if (target != null) {
                alphaAnimation.run(1.0, 5, Easing.EASE_OUT_CUBIC);
            } else {
                alphaAnimation.run(0.0, 5, Easing.EASE_IN_CUBIC);
            }
        }

        alphaAnimation.update();

        Entity entityToRender = target != null ? target : lastTarget;

        if (alphaAnimation.get() <= 0.01f || entityToRender == null) {
            lastTarget = target;
            return;
        }

        ShaderHelper.initShadersIfNeeded();
        if (!ShaderHelper.isInitialized()) {
            lastTarget = target;
            return;
        }

        MatrixStack matrices = event.getMatrixStack();
        Vec3d camPos = mc.gameRenderer.getCamera().getPos();

        double targetX = MathHelper.lerp(event.getPartialTicks(), entityToRender.prevX, entityToRender.getX());
        double targetY = MathHelper.lerp(event.getPartialTicks(), entityToRender.prevY, entityToRender.getY());
        double targetZ = MathHelper.lerp(event.getPartialTicks(), entityToRender.prevZ, entityToRender.getZ());

        float alpha = alphaAnimation.get();

        if (Objects.equals(mode.getSelected(), "Призраки")) {
            setupRender();
            Matrix4f projectionMatrix = RenderSystem.getProjectionMatrix();
            renderGhosts(matrices, projectionMatrix, camPos, targetX, targetY, targetZ, entityToRender, alpha);
            cleanupRender();
        } else if (Objects.equals(mode.getSelected(), "Тор")) {
            animation.update();
            if (animation.isFinished()) {
                animation.run(animation.get() > 0.5 ? 0.0 : 1.0, 650.0 / 1000.0, Easing.EASE_IN_OUT_SINE);
            }

            float fov = (float) mc.gameRenderer.getFov(event.getCamera(), event.getPartialTicks(), true);
            Matrix4f projectionMatrix = mc.gameRenderer.getBasicProjectionMatrix(fov);
            renderTorus(matrices, projectionMatrix, camPos, targetX, targetY, targetZ, entityToRender, event.getPartialTicks(), alpha);
        }

        lastTarget = target;
    }

    private void renderGhosts(MatrixStack matrices, Matrix4f projectionMatrix, Vec3d camPos, double tx, double ty, double tz, Entity target, float alpha) {
        GhostShader shader = ShaderHelper.getGhostShader();
        shader.bind();
        shader.setUniformMatrix4f("u_ProjMat", false, projectionMatrix);

        float time = ((System.currentTimeMillis() - startTime) / 1000f) * speed.getValue();

        float[] c1 = ColorUtility.normalize(color1.getColor());
        float[] c2 = ColorUtility.normalize(color2.getColor());

        for (int i = 0; i < 3; i++) {
            matrices.push();

            float rotation = time * (i % 2 == 0 ? 60f : -60f) + (i * 120f);
            float verticalOffset = (i - 1) * 0.45f;

            matrices.translate(tx - camPos.getX(), ty - camPos.getY() + target.getHeight() / 2f, tz - camPos.getZ());
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotation));
            matrices.scale(size.getValue(), size.getValue(), size.getValue());

            shader.setUniformMatrix4f("u_ModelViewMat", false, matrices.peek().getPositionMatrix());
            shader.setUniforms(time, verticalOffset, new Vector3f(c1[0], c1[1], c1[2]), new Vector3f(c2[0], c2[1], c2[2]), alpha);

            drawTorusMesh();

            matrices.pop();
        }
        shader.unbind();
    }

    private void renderTorus(MatrixStack matrices, Matrix4f projectionMatrix, Vec3d camPos, double targetX, double targetY, double targetZ, Entity target, float partialTicks, float alpha) {
        ReflectionShader.startTorusRender(true);

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);

        Color baseColor = Color.WHITE;
        Color alphaColor = new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), (int)(255 * alpha));
        RenderSystem.setShaderColor(alphaColor.getRed() / 255f, alphaColor.getGreen() / 255f, alphaColor.getBlue() / 255f, alpha);

        matrices.push();

        float animatedYOffset = (float) (animation.get() * target.getHeight());

        matrices.translate(
                targetX - camPos.getX(),
                targetY + animatedYOffset - camPos.getY(),
                targetZ - camPos.getZ()
        );
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90));

        ReflectionShader.renderTorus(matrices.peek().getPositionMatrix(), projectionMatrix, frequency.getValue() / 100f, 0.6f, 0.1f);

        matrices.pop();

        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        ReflectionShader.endTorusRender(true);
    }

    private void drawTorusMesh() {
        VertexFormat vertexFormat = VertexFormat.builder()
                .add("Position", VertexFormatElement.POSITION)
                .add("Normal", VertexFormatElement.NORMAL)
                .build();

        int slices = 40;
        int loops = 12;
        float outerRad = 1.0f;
        float innerRad = 0.1f;

        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, vertexFormat);

        for (int i = 0; i < slices; i++) {
            double theta = 2 * Math.PI * i / slices;
            double nextTheta = 2 * Math.PI * (i + 1) / slices;

            for (int j = 0; j < loops; j++) {
                double phi = 2 * Math.PI * j / loops;
                double nextPhi = 2 * Math.PI * (j + 1) / loops;

                float x1 = (float) ((outerRad + innerRad * Math.cos(theta)) * Math.cos(phi));
                float y1 = (float) (innerRad * Math.sin(theta));
                float z1 = (float) ((outerRad + innerRad * Math.cos(theta)) * Math.sin(phi));

                float x2 = (float) ((outerRad + innerRad * Math.cos(nextTheta)) * Math.cos(phi));
                float y2 = (float) (innerRad * Math.sin(nextTheta));
                float z2 = (float) ((outerRad + innerRad * Math.cos(nextTheta)) * Math.sin(phi));

                float x3 = (float) ((outerRad + innerRad * Math.cos(nextTheta)) * Math.cos(nextPhi));
                float y3 = (float) (innerRad * Math.sin(nextTheta));
                float z3 = (float) ((outerRad + innerRad * Math.cos(nextTheta)) * Math.sin(nextPhi));

                float x4 = (float) ((outerRad + innerRad * Math.cos(theta)) * Math.cos(nextPhi));
                float y4 = (float) (innerRad * Math.sin(theta));
                float z4 = (float) ((outerRad + innerRad * Math.cos(theta)) * Math.sin(nextPhi));

                Vector3f n1 = new Vector3f((float) (Math.cos(theta) * Math.cos(phi)), (float) Math.sin(theta), (float) (Math.cos(theta) * Math.sin(phi))).normalize();
                Vector3f n2 = new Vector3f((float) (Math.cos(nextTheta) * Math.cos(phi)), (float) Math.sin(nextTheta), (float) (Math.cos(nextTheta) * Math.sin(phi))).normalize();
                Vector3f n3 = new Vector3f((float) (Math.cos(nextTheta) * Math.cos(nextPhi)), (float) Math.sin(nextTheta), (float) (Math.cos(nextTheta) * Math.sin(nextPhi))).normalize();
                Vector3f n4 = new Vector3f((float) (Math.cos(theta) * Math.cos(nextPhi)), (float) Math.sin(theta), (float) (Math.cos(theta) * Math.sin(nextPhi))).normalize();

                bufferBuilder.vertex(x1, y1, z1).normal(n1.x, n1.y, n1.z);
                bufferBuilder.vertex(x2, y2, z2).normal(n2.x, n2.y, n2.z);
                bufferBuilder.vertex(x3, y3, z3).normal(n3.x, n3.y, n3.z);
                bufferBuilder.vertex(x4, y4, z4).normal(n4.x, n4.y, n4.z);
            }
        }
        BufferRenderer.draw(bufferBuilder.end());
    }

    private void setupRender() {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
    }

    private void cleanupRender() {
        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.defaultBlendFunc();
    }
}