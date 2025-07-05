package win.blade.core.module.storage.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import win.blade.common.gui.impl.menu.settings.impl.ColorSetting;
import win.blade.common.gui.impl.menu.settings.impl.SliderSetting;
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
    private final SliderSetting radius = new SliderSetting(this, "Радиус круга.", 0.5f, 0.2f, 2.0f, 0.1f);

    private final List<Circle> circles = new ArrayList<>();
    private static final Identifier CIRCLE_TEXTURE = Identifier.of("blade", "textures/misc/circle.png");

    private record Circle(Vec3d position, long creationTime) {}

    @EventHandler
    public void onJump(PlayerActionEvents.Jump event) {
        if (mc.player == null && mc.world == null) return;
        circles.add(new Circle(mc.player.getPos(), System.currentTimeMillis()));
    }

    @EventHandler
    public void onUpdate(UpdateEvents.PlayerUpdate event) {
        if (mc.player == null && mc.world == null) return;
        if (circles.isEmpty()) return;
        Float maxLifetime = lifetime.getValue();
        circles.removeIf(circle -> System.currentTimeMillis() - circle.creationTime > maxLifetime);
    }

    @EventHandler
    public void onRender3D(RenderEvents.World event) {
        if (circles.isEmpty() || (mc.player == null && mc.world == null)) return;

        MatrixStack matrices = event.getMatrixStack();
        Camera camera = mc.gameRenderer.getCamera();
        Vec3d cameraPos = camera.getPos();

        setupRenderState();

        matrices.push();
        matrices.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        for (Circle circle : circles) {
            renderCircle(matrices, circle);
        }

        matrices.pop();
        resetRenderState();
    }

    private void renderCircle(MatrixStack matrices, Circle circle) {
        matrices.push();

        matrices.translate(circle.position.x, circle.position.y, circle.position.z);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90.0F));

        Matrix4f matrix = matrices.peek().getPositionMatrix();
        float circleRadius = radius.getValue();

        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);

        int color = new Color(255, 255, 255, 255).getRGB();
        buffer.vertex(matrix, -circleRadius, -circleRadius, 0).texture(0, 0).color(color);
        buffer.vertex(matrix, -circleRadius,  circleRadius, 0).texture(0, 1).color(color);
        buffer.vertex(matrix,  circleRadius,  circleRadius, 0).texture(1, 1).color(color);
        buffer.vertex(matrix,  circleRadius, -circleRadius, 0).texture(1, 0).color(color);

        BufferRenderer.drawWithGlobalProgram(buffer.end());

        matrices.pop();
    }

    private void setupRenderState() {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.depthMask(false);
        RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.setShaderTexture(0, CIRCLE_TEXTURE);
    }

    private void resetRenderState() {
        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }
}