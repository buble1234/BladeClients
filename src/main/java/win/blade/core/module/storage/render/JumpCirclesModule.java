package win.blade.core.module.storage.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import win.blade.common.gui.impl.menu.settings.impl.SliderSetting;
import win.blade.common.utils.math.TimerUtil;
import win.blade.common.utils.math.animation.Animation;
import win.blade.common.utils.math.animation.Easing;
import win.blade.core.event.controllers.EventHandler;
import win.blade.core.event.impl.player.PlayerActionEvents;
import win.blade.core.event.impl.render.RenderEvents;
import win.blade.core.module.api.Category;
import win.blade.core.module.api.Module;
import win.blade.core.module.api.ModuleInfo;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

@ModuleInfo(
        name = "JumpCircles",
        category = Category.RENDER
)
public class JumpCirclesModule extends Module {


    private final SliderSetting lifetime = new SliderSetting(this, "Время жизни", 1500, 500, 3000, 100);
    private final SliderSetting radius = new SliderSetting(this, "Радиус", 1F, 0.5F, 3F, 0.1F);
    private final SliderSetting opacity = new SliderSetting(this, "Прозрачность", 1F, 0.05F, 1F, 0.05F);

    private final float glow = 0.2f;
    private final float line = 0.01f;

    private final static List<Circle> circles = new ArrayList<>();
    private static JumpCirclesModule instance;

    public JumpCirclesModule() {
        instance = this;
    }

    public static JumpCirclesModule getInstance() {
        return instance;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        clear();
    }

    @EventHandler
    public void onJump(PlayerActionEvents.Jump event) {
        if (mc.player == null) return;
        addCircle(mc.player);
    }

    @EventHandler
    public void onRender3D(RenderEvents.World event) {
        if (circles.isEmpty()) return;

        circles.removeIf(this::finished);

        if (circles.isEmpty()) return;

        MatrixStack matrixStack = event.getMatrixStack();
        setupRender();

        Vec3d cameraPos = mc.gameRenderer.getCamera().getPos();
        matrixStack.push();
        matrixStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        circles.forEach(circle -> drawCircle(matrixStack, circle));

        matrixStack.pop();
        cleanupRender();
    }

    private void drawCircle(final MatrixStack matrix, final Circle circle) {
        circle.getAnimation().update();

        final float percent = circle.getTime().elapsedTime() / lifetime.getValue();
        final boolean finished = percent >= 0.5F;
        circle.getAnimation().run(
                finished ? 0 : 1,
                finished ? (this.lifetime.getValue() / 1000D) : (this.lifetime.getValue() / 1000D) / 2D,
                finished ? Easing.EASE_IN_OUT_SINE : Easing.EASE_OUT_SINE,
                true
        );

        float animProgress = (float) circle.getAnimation().get();
        if (animProgress <= 0) {
            return;
        }

        final long elapsedTime = circle.getTime().elapsedTime();
        final double animationTime = 3000;
        final double rotate = (elapsedTime % animationTime) / animationTime;

        matrix.push();
        matrix.translate(circle.getPos().x, circle.getPos().y, circle.getPos().z);
        matrix.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float) (rotate * 360F)));
        matrix.translate(-circle.getPos().x, -circle.getPos().y, -circle.getPos().z);

        double xVal = circle.getPos().x;
        double yVal = circle.getPos().y;
        double zVal = circle.getPos().z;
        Matrix4f matrix4f = matrix.peek().getPositionMatrix();

        Color colorObj = Color.WHITE;
        float r = colorObj.getRed() / 255.0f;
        float g = colorObj.getGreen() / 255.0f;
        float b = colorObj.getBlue() / 255.0f;

        Tessellator tessellator = Tessellator.getInstance();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);

        float lineAlpha = opacity.getValue() * animProgress;
        float glowAlpha = lineAlpha * 0.35f;

        float centerRadius = this.radius.getValue() * animProgress;
        float currentGlowThickness = this.glow * animProgress;
        float currentLineThickness = this.line * animProgress;

        float glowOuterRadius = centerRadius + currentGlowThickness;
        float lineOuterRadius = centerRadius + currentLineThickness;
        float lineInnerRadius = centerRadius - currentLineThickness;
        float glowInnerRadius = centerRadius - currentGlowThickness;

        drawRing(tessellator, matrix4f, xVal, yVal, zVal, glowOuterRadius, lineOuterRadius, 0, glowAlpha, r, g, b);
        drawRing(tessellator, matrix4f, xVal, yVal, zVal, lineOuterRadius, centerRadius, glowAlpha, lineAlpha, r, g, b);
        drawRing(tessellator, matrix4f, xVal, yVal, zVal, centerRadius, lineInnerRadius, lineAlpha, glowAlpha, r, g, b);
        drawRing(tessellator, matrix4f, xVal, yVal, zVal, lineInnerRadius, glowInnerRadius, glowAlpha, 0, r, g, b);

        matrix.pop();
    }

    private void drawRing(Tessellator tessellator, Matrix4f matrix, double x, double y, double z, float fromRadius, float toRadius, float fromAlpha, float toAlpha, float r, float g, float b) {
        if (fromAlpha == 0 && toAlpha == 0) return;
        double pi2 = Math.PI * 2.0;
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
        for (int i = 0; i <= 90; ++i) {
            double angle = i * pi2 / 90;
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);
            buffer.vertex(matrix, (float) (x + fromRadius * cos), (float) y, (float) (z + fromRadius * sin)).color(r, g, b, fromAlpha);
            buffer.vertex(matrix, (float) (x + toRadius * cos), (float) y, (float) (z + toRadius * sin)).color(r, g, b, toAlpha);
        }
        BufferRenderer.drawWithGlobalProgram(buffer.end());
    }

    private Vec3d getEntityPosition(final Entity entity, float tickDelta) {
        return entity.getLerpedPos(tickDelta);
    }

    private void addCircle(final Entity entity) {
        if (mc.world == null) return;
        Vec3d vec = getEntityPosition(entity, mc.getRenderTickCounter().getTickDelta(true)).add(0.D, .005D, 0.D);
        final BlockPos pos = BlockPos.ofFloored(vec);
        final BlockState state = mc.world.getBlockState(pos);
        if (state.getBlock() == Blocks.SNOW) {
            vec = vec.add(0.D, .125D, 0.D);
        }
        circles.add(new Circle(vec));
    }

    private void clear() {
        if (!circles.isEmpty()) circles.clear();
    }

    private boolean finished(final Circle circle) {
        return circle.getTime().hasReached((long) (lifetime.getValue() * 2F));
    }

    private void setupRender() {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
    }

    private void cleanupRender() {
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    }

    private static final class Circle {
        private final TimerUtil time = new TimerUtil();
        private final Animation animation = new Animation();
        private final Vec3d pos;

        public Circle(Vec3d pos) {
            this.pos = pos;
        }

        public TimerUtil getTime() {
            return time;
        }

        public Animation getAnimation() {
            return animation;
        }

        public Vec3d getPos() {
            return pos;
        }
    }
}