package win.blade.core.module.storage.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import org.joml.Matrix4f;
import win.blade.common.gui.impl.gui.setting.implement.BooleanSetting;
import win.blade.common.gui.impl.gui.setting.implement.SelectSetting;
import win.blade.common.gui.impl.gui.setting.implement.ValueSetting;
import win.blade.common.utils.color.ColorUtility;
import win.blade.common.utils.math.TimerUtil;
import win.blade.common.utils.math.animation.Animation;
import win.blade.common.utils.math.animation.Easing;
import win.blade.common.utils.player.MovementUtility;
import win.blade.common.utils.player.PlayerUtility;
import win.blade.core.event.controllers.EventHandler;
import win.blade.core.event.impl.minecraft.UpdateEvents;
import win.blade.core.event.impl.render.RenderEvents;
import win.blade.core.module.api.Category;
import win.blade.core.module.api.Module;
import win.blade.core.module.api.ModuleInfo;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@ModuleInfo(name = "FireFly", category = Category.RENDER)
public class FireFly extends Module {

    private final ValueSetting count = new ValueSetting("Кол-во", "").setValue(5f).range(1f, 25f);
    private final ValueSetting size = new ValueSetting("Размер", "").setValue(0.5F).range(0.0F, 1F);
    private final ValueSetting range = new ValueSetting("Дистанция", "").setValue(16f).range(4f, 32f);
    private final ValueSetting duration = new ValueSetting("Время жизни", "").setValue(3500f).range(500f, 5000f);
    private final ValueSetting strength = new ValueSetting("Сила движения", "").setValue(1.0F).range(0.1F, 2.0F);
    private final ValueSetting opacity = new ValueSetting("Прозрачность", "").setValue(1.0F).range(0.1F, 1.0F);
    private final BooleanSetting glowing = new BooleanSetting("Свечение", "").setValue(true);
    private final BooleanSetting onlyMove = new BooleanSetting("Только в движении", "").setValue(false);
    private final BooleanSetting ground = new BooleanSetting("Спавнить на земле", "").setValue(false);
    private final BooleanSetting physic = new BooleanSetting("Физика", "").setValue(false);
    private final SelectSetting colorMode = new SelectSetting("Режим цвета", "").value("Клиентский", "Радужный");
    private final SelectSetting particleMode = new SelectSetting("Тип частиц", "")
            .value("Bloom", "Random", "Amongus", "Circle", "Crown", "Dollar", "Heart",
                    "Polygon", "Quad", "Skull", "Star", "Cross", "Triangle");

    private final List<Particle> particles = new ArrayList<>();

    public FireFly() {
        addSettings(
                count, size, range, duration, strength, opacity,
                glowing, onlyMove, ground, physic,
                colorMode, particleMode
        );
    }


    @Override
    public void onEnable() {
        clear();
    }

    @Override
    public void onDisable() {
        clear();
    }

    private void clear() {
        if (!particles.isEmpty()) {
            particles.clear();
        }
    }

    @EventHandler
    public void onUpdate(UpdateEvents.Update event) {
        if (mc.player == null || mc.world == null) return;
        if (onlyMove.getValue() && !MovementUtility.isMoving()) return;

        int rangeValue = (int) this.range.getValue();
        for (int i = 0; i < (int) count.getValue(); i++) {
            Vec3d playerPos = mc.player.getPos();
            double randX = playerPos.x + randomValue(-rangeValue, rangeValue);
            double randZ = playerPos.z + randomValue(-rangeValue, rangeValue);

            BlockPos topPos = mc.world.getTopPosition(Heightmap.Type.MOTION_BLOCKING, BlockPos.ofFloored(randX, playerPos.y, randZ));

            double spawnY = ground.getValue() ? topPos.getY() : mc.player.getY() + randomValue(mc.player.getHeight(), rangeValue);
            Vec3d position = new Vec3d(topPos.getX() + randomValue(0, 1), spawnY, topPos.getZ() + randomValue(0, 1));
            Vec3d velocity = new Vec3d(0, randomValue(0.0, strength.getValue()) * (ground.getValue() ? 1 : -1), 0);

            spawnParticle(position, velocity);
        }
    }

    @EventHandler
    public void onRender3D(RenderEvents.World event) {
        if (mc.player == null || mc.world == null || particles.isEmpty()) return;

        MatrixStack matrixStack = event.getMatrixStack();

        setupRender();

        Vec3d cameraPos = mc.gameRenderer.getCamera().getPos();
        matrixStack.push();
        matrixStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        renderParticles(matrixStack);

        matrixStack.pop();
        cleanupRender();
    }

    private void renderParticles(MatrixStack matrixStack) {

        double lifetime = this.duration.getValue();
        double fadeInDuration = 500;

        removeExpiredParticles(lifetime + fadeInDuration);
        if (particles.isEmpty()) return;

        for (Particle particle : new ArrayList<>(particles)) {
            particle.update(physic.getValue());

            Animation animation = particle.animation();
            animation.update();
            float animationAlpha = (float) animation.get();

            double animTime = (fadeInDuration / 1000.0);

            if (animationAlpha != opacity.getValue() && !particle.time().hasReached((long) fadeInDuration)) {
                animation.run(opacity.getValue(), animTime, Easing.EASE_OUT_CUBIC, true);
            }
            if (animationAlpha != 0.0F && particle.time().hasReached((long) lifetime)) {
                animation.run(0.0F, animTime, Easing.EASE_OUT_CUBIC, true);
            }

            float pulsate = (float) ((Math.sin((System.currentTimeMillis() - particle.spawnTime()) / 200.0) + 1.0) / 2.0);
            int colorWithAnimationAlpha = ColorUtility.applyAlpha(particle.color(), animationAlpha);
            int finalColor = multAlpha(colorWithAnimationAlpha, pulsate);

            renderParticle(matrixStack, particle, finalColor);
        }
    }

    private int multAlpha(int color, float alpha) {
        return ColorUtility.pack(
                ColorUtility.getRed(color),
                ColorUtility.getGreen(color),
                ColorUtility.getBlue(color),
                (int) (ColorUtility.getAlpha(color) * alpha)
        );
    }

    private void removeExpiredParticles(double lifespan) {
        particles.removeIf(particle -> !PlayerUtility.isInView(particle.getBox()));
        particles.removeIf(particle -> particle.time().hasReached((long) lifespan));
    }

    private void renderParticle(MatrixStack matrixStack, Particle particle, int color) {
        float size = particle.size();
        Vec3d pos = particle.position();

        matrixStack.push();
        matrixStack.translate(pos.x, pos.y, pos.z);
        matrixStack.multiply(mc.getEntityRenderDispatcher().camera.getRotation());
        matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180f));

        if (particle.type().rotatable()) {
            matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(particle.rotate()));
        }

        if (glowing.getValue()) {
            int glowColor = multAlpha(color, 0.25f);
            drawTexturedQuad(matrixStack, ParticleType.BLOOM.texture(), -size * 4, -size * 4, size * 8, size * 8, glowColor);
        }

        drawTexturedQuad(matrixStack, particle.type().texture(), -size, -size, size * 2, size * 2, color);

        if (particle.type() == ParticleType.BLOOM) {
            drawTexturedQuad(matrixStack, particle.type().texture(), -size / 2, -size / 2, size, size, color);
        }

        matrixStack.pop();
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

    private void setupRender() {
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

    private void spawnParticle(Vec3d position, Vec3d velocity) {
        float size = 0.05F + (this.size.getValue() * 0.2F);

        int particleColor = switch (this.colorMode.getSelected()) {
            case "Клиентский" -> Color.WHITE.getRGB();
            case "Радужный" -> Color.HSBtoRGB((System.currentTimeMillis() + particles.size() * 100) % 2000L / 2000.0f, 0.7f, 1.0f);
            default -> Color.WHITE.getRGB();
        };

        ParticleType type = switch (this.particleMode.getSelected()) {
            case "Amongus" -> ParticleType.AMONGUS;
            case "Circle" -> ParticleType.CIRCLE;
            case "Crown" -> ParticleType.CROWN;
            case "Dollar" -> ParticleType.DOLLAR;
            case "Heart" -> ParticleType.HEART;
            case "Polygon" -> ParticleType.POLYGON;
            case "Quad" -> ParticleType.QUAD;
            case "Skull" -> ParticleType.SKULL;
            case "Star" -> ParticleType.STAR;
            case "Cross" -> ParticleType.CROSS;
            case "Triangle" -> ParticleType.TRIANGLE;
            case "Bloom" -> ParticleType.BLOOM;
            default -> ParticleType.getRandom();
        };

        int rotation = (int) (Math.floor(randomValue(0, 360) / 15) * 15);

        particles.add(new Particle(type, position.add(0, size, 0), velocity, particleColor, size, rotation));
    }

    private double randomValue(double min, double max) {
        if (min >= max) return min;
        return min + (max - min) * ThreadLocalRandom.current().nextDouble();
    }

    public enum ParticleType {
        AMONGUS("amongus", false),
        CIRCLE("circle", false),
        CROWN("crown", false),
        DOLLAR("dollar", false),
        HEART("heart", false),
        POLYGON("polygon", true),
        QUAD("quad", true),
        SKULL("skull", false),
        STAR("star", true),
        CROSS("cross", true),
        TRIANGLE("triangle", true),
        BLOOM("bloom", false);

        private final Identifier texture;
        private final boolean rotatable;

        ParticleType(String name, boolean rotatable) {
            this.texture = Identifier.of("blade", "textures/particle/" + name + ".png");
            this.rotatable = rotatable;
        }

        public Identifier texture() {
            return texture;
        }

        public boolean rotatable() {
            return rotatable;
        }

        public static ParticleType getRandom() {
            ParticleType[] values = values();
            return values[ThreadLocalRandom.current().nextInt(values.length)];
        }
    }

    public static class Particle {
        private final long spawnTime = System.currentTimeMillis();
        private final ParticleType type;
        private Box box;
        private Vec3d position;
        private Vec3d velocity;
        private final int rotate;
        private final int color;
        private final float size;

        private final TimerUtil time = new TimerUtil();
        private final Animation animation = new Animation();

        public Particle(ParticleType type, Vec3d position, Vec3d velocity, int color, float size, int rotate) {
            this.type = type;
            this.rotate = rotate;
            this.position = position;
            this.velocity = velocity.multiply(0.01F);
            this.color = color;
            this.size = size;
            this.box = new Box(position, position).expand(size);
        }

        public void update(boolean physic) {
            if (physic && mc.world != null) {
                if (isBlockSolid(this.position.add(0, 0, this.velocity.z))) {
                    this.velocity = this.velocity.multiply(1, 1, -0.8);
                }
                if (isBlockSolid(this.position.add(0, this.velocity.y, 0))) {
                    this.velocity = this.velocity.multiply(0.999, -0.6, 0.999);
                }
                if (isBlockSolid(this.position.add(this.velocity.x, 0, 0))) {
                    this.velocity = this.velocity.multiply(-0.8, 1, 1);
                }
                this.velocity = this.velocity.multiply(0.999999).subtract(0, 0.00005, 0);
            }
            this.position = this.position.add(this.velocity);
            this.box = new Box(position, position).expand(this.size);
        }

        private boolean isBlockSolid(Vec3d pos) {
            if (mc.world == null) return false;
            BlockPos blockPos = BlockPos.ofFloored(pos);
            return !mc.world.getBlockState(blockPos).getCollisionShape(mc.world, blockPos).isEmpty();
        }

        public Box getBox() { return box; }
        public long spawnTime() { return spawnTime; }
        public ParticleType type() { return type; }
        public Vec3d position() { return position; }
        public int rotate() { return rotate; }
        public int color() { return color; }
        public float size() { return size; }
        public TimerUtil time() { return time; }
        public Animation animation() { return animation; }
    }
}