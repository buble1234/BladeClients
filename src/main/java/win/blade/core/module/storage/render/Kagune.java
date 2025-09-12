package win.blade.core.module.storage.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.*;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import win.blade.common.gui.impl.gui.setting.implement.ValueSetting;
import win.blade.common.utils.color.ColorUtility;
import win.blade.common.utils.math.TimerUtil;
import win.blade.common.utils.math.animation.Animation;
import win.blade.common.utils.math.animation.Easing;
import win.blade.core.event.controllers.EventHandler;
import win.blade.core.event.impl.minecraft.MotionEvents;
import win.blade.core.event.impl.render.RenderEvents;
import win.blade.core.module.api.Category;
import win.blade.core.module.api.Module;
import win.blade.core.module.api.ModuleInfo;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

@ModuleInfo(name = "Kagune", category = Category.RENDER)
public class Kagune extends Module {

    private final ValueSetting delay = new ValueSetting("Длина", "").setValue(1500f).range(500f, 3000f);

    private final List<Particle3D> particles = new ArrayList<>();
    private final Identifier triangleTexture = Identifier.of("blade", "textures/triangle.png");
    private final Identifier bloomTexture = Identifier.of("blade", "textures/particle/bloom.png");

    private double lastPlayerX = 0;
    private double lastPlayerY = 0;
    private double lastPlayerZ = 0;

    public Kagune() {
        addSettings(delay);
    }

    @Override
    public void onEnable() {
        clear();
        if (mc.player != null) {
            lastPlayerX = mc.player.getX();
            lastPlayerY = mc.player.getY();
            lastPlayerZ = mc.player.getZ();
        }
    }

    @Override
    public void onDisable() {
        clear();
    }

    private void clear() {
        if (!particles.isEmpty()) particles.clear();
    }

    @EventHandler
    public void onMotion(MotionEvents.Post event) {
        if (mc.player == null) {
            clear();
            return;
        }

        if (lastPlayerX != mc.player.getX() || lastPlayerY != mc.player.getY() || lastPlayerZ != mc.player.getZ()) {

            double distance = -(mc.player.getWidth() / 2F);
            double yawRad = Math.toRadians(mc.player.bodyYaw);
            double xOffset = -Math.sin(yawRad) * distance;
            double zOffset = Math.cos(yawRad) * distance;

            Vec3d spawnPos = new Vec3d(
                    mc.player.getX() + xOffset,
                    mc.player.getY() + (mc.player.getHeight() * 0.4F),
                    mc.player.getZ() + zOffset
            );

            Vec3d playerVelocity = mc.player.getVelocity();
            Vec3d velocity = new Vec3d(playerVelocity.x, 0, playerVelocity.z).multiply(1.5F + Math.random());

            particles.add(new Particle3D(spawnPos, velocity, particles.size()));
        }

        lastPlayerX = mc.player.getX();
        lastPlayerY = mc.player.getY();
        lastPlayerZ = mc.player.getZ();

        particles.removeIf(particle -> particle.getTimerUtil().hasReached((long)delay.getValue()));
        particles.removeIf(particle -> particle.position.distance(new MutableVec3d(mc.player.getPos())) >= 100);
    }

    @EventHandler
    public void onRender3D(RenderEvents.World event) {
        if (particles.isEmpty()) return;

        MatrixStack matrixStack = event.getMatrixStack();

        setupRenderState();

        Vec3d cameraPos = mc.gameRenderer.getCamera().getPos();
        matrixStack.push();
        matrixStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        float partialTicks = mc.getRenderTickCounter().getTickDelta(false);
        for (int i = 0; i < Math.max(1, (int)partialTicks); i++) {
            particles.forEach(Particle3D::update);
        }

        float pos = 0.15F;
        int index = 0;

        for (final Particle3D particle : particles) {
            if ((int) particle.getAnimation().get() != 128 && !particle.getTimerUtil().hasReached(250)) {
                particle.getAnimation().run(128, 0.5, Easing.EASE_OUT_CUBIC, true);
            }
            if ((int) particle.getAnimation().get() != 0 && particle.getTimerUtil().hasReached((long)delay.getValue() - 250)) {
                particle.getAnimation().run(0, 0.5, Easing.EASE_OUT_CUBIC, true);
            }

            particle.getAnimation().update();

            int color = ColorUtility.applyAlpha(
                    Color.HSBtoRGB((System.currentTimeMillis() + particle.getIndex() * 30) % 2000L / 2000.0f, 0.7f, 1.0f),
                    (float) particle.getAnimation().get() / 128f
            );

            if (index > 0) {
                Particle3D prevParticle = particles.get(clamp(0, particles.size() - 1, index - 1));
                MutableVec3d prevPosition = prevParticle.getPosition();

                float smooth = 0.1F;
                prevParticle.position.set(
                        lerp(prevPosition.x, particle.position.x, smooth),
                        lerp(prevPosition.y, particle.position.y, smooth),
                        lerp(prevPosition.z, particle.position.z, smooth)
                );
            }

            Vec3d vec = particle.getPosition().toVec3d();
            float x = (float) vec.x;
            float y = (float) vec.y;
            float z = (float) vec.z;

            matrixStack.push();
            matrixStack.translate(x, y, z);
            matrixStack.multiply(mc.getEntityRenderDispatcher().camera.getRotation());
            matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180f));

            matrixStack.translate(0, pos / 2F, 0);

            MutableVec3d prevPos = (index > 0) ? particles.get(Math.max(0, index - 1)).getPosition() : particle.position;
            float bloomSize = (float) clamp(0.25, 1.0, particle.position.distance(prevPos) * 4);
            drawTexturedQuad(matrixStack, bloomTexture, -bloomSize, -bloomSize, bloomSize * 2, bloomSize * 2, color);
            drawTexturedQuad(matrixStack, triangleTexture, -pos, -pos, pos * 2, pos * 2, color);

            matrixStack.pop();
            index++;
        }

        matrixStack.pop();
        cleanupRender();
    }

    private void drawTexturedQuad(MatrixStack matrixStack, Identifier texture, float x, float y, float width, float height, int color) {
        Matrix4f matrix = matrixStack.peek().getPositionMatrix();
        float[] c = ColorUtility.normalize(color);

        RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
        RenderSystem.setShaderTexture(0, texture);

        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        bufferBuilder.vertex(matrix, x, y, 0).texture(0, 0).color(c[0], c[1], c[2], c[3]);
        bufferBuilder.vertex(matrix, x, y + height, 0).texture(0, 1).color(c[0], c[1], c[2], c[3]);
        bufferBuilder.vertex(matrix, x + width, y + height, 0).texture(1, 1).color(c[0], c[1], c[2], c[3]);
        bufferBuilder.vertex(matrix, x + width, y, 0).texture(1, 0).color(c[0], c[1], c[2], c[3]);
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
    }

    private void setupRenderState() {
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(
                GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE,
                GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO
        );
        RenderSystem.disableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);
    }

    private void cleanupRender() {
        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.disableDepthTest();
        RenderSystem.defaultBlendFunc();
    }

    private double lerp(double start, double end, double factor) {
        return start + factor * (end - start);
    }

    private int clamp(int min, int max, int value) {
        return Math.max(min, Math.min(max, value));
    }

    private double clamp(double min, double max, double value) {
        return Math.max(min, Math.min(max, value));
    }

    public static class MutableVec3d {
        public double x, y, z;

        public MutableVec3d(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public MutableVec3d(Vec3d vec) {
            this.x = vec.x;
            this.y = vec.y;
            this.z = vec.z;
        }

        public void set(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public double distance(MutableVec3d other) {
            double dx = this.x - other.x;
            double dy = this.y - other.y;
            double dz = this.z - other.z;
            return Math.sqrt(dx * dx + dy * dy + dz * dz);
        }

        public Vec3d toVec3d() {
            return new Vec3d(x, y, z);
        }
    }

    public static class Particle3D {
        private final int index;
        private final TimerUtil timerUtil = new TimerUtil();
        private final Animation animation = new Animation();

        public MutableVec3d position;
        private MutableVec3d delta;

        public Particle3D(final Vec3d position, final int index) {
            this.position = new MutableVec3d(position);
            this.delta = new MutableVec3d(
                    (Math.random() * 0.5 - 0.25) * 0.01,
                    (Math.random() * 0.25) * 0.01,
                    (Math.random() * 0.5 - 0.25) * 0.01
            );
            this.index = index;
        }

        public Particle3D(final Vec3d position, final Vec3d velocity, final int index) {
            this.position = new MutableVec3d(position);
            this.delta = new MutableVec3d(velocity.x * 0.01, velocity.y * 0.01, velocity.z * 0.01);
            this.index = index;
        }

        public void update() {
            if (mc.world == null) return;

            final Block block1 = getBlock(this.position.x, this.position.y, this.position.z + this.delta.z);
            if (isValidBlock(block1))
                this.delta.z *= -0.8;

            final Block block2 = getBlock(this.position.x, this.position.y + this.delta.y, this.position.z);
            if (isValidBlock(block2)) {
                this.delta.x *= 0.999F;
                this.delta.z *= 0.999F;
                this.delta.y *= -0.7;
            }

            final Block block3 = getBlock(this.position.x + this.delta.x, this.position.y, this.position.z);
            if (isValidBlock(block3))
                this.delta.x *= -0.8;

            this.updateWithoutPhysics();
        }

        private Block getBlock(double x, double y, double z) {
            if (mc.world == null) return Blocks.AIR;
            BlockPos blockPos = BlockPos.ofFloored(x, y, z);
            return mc.world.getBlockState(blockPos).getBlock();
        }

        private boolean isValidBlock(Block block) {
            return !(block instanceof AirBlock)
                    && !(block instanceof PlantBlock)
                    && !(block instanceof ButtonBlock)
                    && !(block instanceof TorchBlock)
                    && !(block instanceof LeverBlock)
                    && !(block instanceof PressurePlateBlock)
                    && !(block instanceof CarpetBlock)
                    && !(block instanceof FluidBlock);
        }

        public void updateWithoutPhysics() {
            this.position.x += this.delta.x;
            this.position.y += this.delta.y;
            this.position.z += this.delta.z;
            this.delta.x /= 0.999999F;
            this.delta.y = 0;
            this.delta.z /= 0.999999F;
        }

        public int getIndex() { return index; }
        public MutableVec3d getPosition() { return position; }
        public TimerUtil getTimerUtil() { return timerUtil; }
        public Animation getAnimation() { return animation; }
    }
}