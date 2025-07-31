package win.blade.core.module.storage.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import win.blade.common.gui.impl.gui.setting.implement.BooleanSetting;
import win.blade.common.gui.impl.gui.setting.implement.GroupSetting;
import win.blade.common.gui.impl.gui.setting.implement.SelectSetting;
import win.blade.common.gui.impl.gui.setting.implement.ValueSetting;
import win.blade.common.utils.color.ColorUtility;
import win.blade.common.utils.math.TimerUtil;
import win.blade.common.utils.math.animation.Animation;
import win.blade.common.utils.math.animation.Easing;
import win.blade.common.utils.player.PlayerUtility;
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
import java.util.concurrent.ThreadLocalRandom;

@ModuleInfo(name = "Particles", category = Category.RENDER)
public class Particles extends Module {

    private final BooleanSetting moveSetting = new BooleanSetting("Движении", "").setValue(true);
    private final BooleanSetting attackSetting = new BooleanSetting("Атаке", "").setValue(true);
    private final BooleanSetting critSetting = new BooleanSetting("Крите", "").setValue(false).visible(attackSetting::getValue);

    private final GroupSetting events = new GroupSetting("Спавнить при", "").settings(
            moveSetting, attackSetting, critSetting
    );

    private final ValueSetting countAttack = new ValueSetting("Кол-во при атаке", "")
            .setValue(2f).range(1f, 25f).visible(attackSetting::getValue);
    private final ValueSetting countMove = new ValueSetting("Кол-во при движении", "")
            .setValue(2f).range(1f, 25f).visible(moveSetting::getValue);

    private final ValueSetting size = new ValueSetting("Размер", "").setValue(0.2F).range(0.0F, 1F);
    private final ValueSetting strength = new ValueSetting("Сила движения", "").setValue(1.0F).range(0.1F, 2.0F);
    private final ValueSetting opacity = new ValueSetting("Прозрачность", "").setValue(1.0F).range(0.1F, 1.0F);
    private final BooleanSetting glowing = new BooleanSetting("Свечение", "").setValue(true);
    private final BooleanSetting physic = new BooleanSetting("Физика", "").setValue(false);
    private final SelectSetting colorMode = new SelectSetting("Режим цвета", "").value("Клиентский", "Радужный");
    private final SelectSetting particleMode = new SelectSetting("Тип частиц", "")
            .value("Bloom", "Random", "Amongus", "Circle", "Crown", "Dollar", "Heart",
                    "Polygon", "Quad", "Skull", "Star", "Cross", "Triangle");

    private final List<Particle> targetParticles = new ArrayList<>();
    private final List<Particle> flameParticles = new ArrayList<>();
    private final List<Point> playerPath = new ArrayList<>();

    public Particles() {
        addSettings(
                events, countAttack, countMove, size, strength, opacity,
                glowing, physic, colorMode, particleMode
        );
    }

    private BooleanSetting getBooleanSetting(GroupSetting group, String name) {
        return (BooleanSetting) group.getSubSetting(name);
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
        if (!targetParticles.isEmpty()) targetParticles.clear();
        if (!flameParticles.isEmpty()) flameParticles.clear();
        if (!playerPath.isEmpty()) playerPath.clear();
    }

    @EventHandler
    public void onAttack(PlayerActionEvents.Attack event) {
        if (mc.player == null || !getBooleanSetting(events, "Атаке").getValue()) return;
        if (getBooleanSetting(events, "Крите").getValue() && !PlayerUtility.isCritical()) return;

        Entity target = event.getEntity();
        float motion = strength.getValue();

        for (int i = 0; i < (int) countAttack.getValue(); i++) {
            spawnParticle(targetParticles,
                    new Vec3d(target.getX(), target.getY() + randomValue(0, target.getHeight()), target.getZ()),
                    new Vec3d(randomValue(-motion, motion), randomValue(-motion, motion / 4F), randomValue(-motion, motion))
            );
        }
    }

    @EventHandler
    public void onUpdate(UpdateEvents.Update event) {
        if (mc.player == null) {
            clear();
            return;
        }

        if (moveSetting.getValue()) {
            Vec3d currentPos = mc.player.getPos();
            Vec3d lastPos = playerPath.isEmpty() ? null : playerPath.get(playerPath.size() - 1).getPos();

            if (lastPos == null || currentPos.distanceTo(lastPos) > 0.015) {
                playerPath.add(new Point(currentPos));
                if (mc.options.getPerspective() != Perspective.FIRST_PERSON) {
                    for (int i = 0; i < (int) countMove.getValue(); i++) {
                        spawnParticle(flameParticles,
                                new Vec3d(mc.player.getX() + randomValue(-0.5, 0.5), mc.player.getY() + randomValue(0, mc.player.getHeight()), mc.player.getZ() + randomValue(-0.5, 0.5)),
                                mc.player.getVelocity().add(randomValue(-0.25, 0.25), randomValue(-0.15, 0.15), randomValue(-0.25, 0.25)).multiply(strength.getValue())
                        );
                    }
                }
            }
            playerPath.removeIf(point -> point.isExpired(System.currentTimeMillis(), 2000L));
        } else {
            if (!playerPath.isEmpty()) playerPath.clear();
        }

        removeExpiredParticles(targetParticles, 5000);
        removeExpiredParticles(flameParticles, 3500);
    }

    @EventHandler
    public void onRender3D(RenderEvents.World event) {
        if (targetParticles.isEmpty() && flameParticles.isEmpty()) return;

        MatrixStack matrixStack = event.getMatrixStack();

        setupRenderState();

        Vec3d cameraPos = mc.gameRenderer.getCamera().getPos();
        matrixStack.push();
        matrixStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        renderParticles(matrixStack, targetParticles, 500, 2000);
        renderParticles(matrixStack, flameParticles, 500, 3000);

        matrixStack.pop();
        cleanupRender();
    }

    private void spawnParticle(List<Particle> particles, Vec3d position, Vec3d velocity) {
        float size = 0.05F + (this.size.getValue() * 0.2F);
        int color = switch (this.colorMode.getSelected()) {
            case "Клиентский" -> Color.WHITE.getRGB();
            case "Радужный" -> Color.HSBtoRGB((System.currentTimeMillis() + particles.size() * 100) % 2000L / 2000.0f, 0.7f, 1.0f);
            default -> Color.WHITE.getRGB();
        };

        Particles.ParticleType type = switch (this.particleMode.getSelected()) {
            case "Amongus" -> Particles.ParticleType.AMONGUS;
            case "Circle" -> Particles.ParticleType.CIRCLE;
            case "Crown" -> Particles.ParticleType.CROWN;
            case "Dollar" -> Particles.ParticleType.DOLLAR;
            case "Heart" -> Particles.ParticleType.HEART;
            case "Polygon" -> Particles.ParticleType.POLYGON;
            case "Quad" -> Particles.ParticleType.QUAD;
            case "Skull" -> Particles.ParticleType.SKULL;
            case "Star" -> Particles.ParticleType.STAR;
            case "Cross" -> Particles.ParticleType.CROSS;
            case "Triangle" -> Particles.ParticleType.TRIANGLE;
            case "Bloom" -> Particles.ParticleType.BLOOM;
            default -> Particles.ParticleType.getRandom();
        };

        int rotation = (int) (Math.floor(randomValue(0, 360) / 15) * 15);
        particles.add(new Particle(type, position.add(0, size, 0), velocity, color, size, rotation));
    }

    private void renderParticles(MatrixStack matrix, List<Particle> particles, long fadeInTime, long fadeOutTime) {
        if (particles.isEmpty()) return;

        for (Particle particle : new ArrayList<>(particles)) {
            particle.update(physic.getValue());
            particle.animation().update();

            float animationAlpha = (float) particle.animation().get();

            if (animationAlpha != opacity.getValue() && !particle.time().hasReached(fadeInTime)) {
                particle.animation().run(opacity.getValue(), 0.5, Easing.EASE_OUT_CUBIC, true);
            }
            if (animationAlpha != 0 && particle.time().hasReached(fadeOutTime)) {
                particle.animation().run(0, 0.5, Easing.EASE_OUT_CUBIC, true);
            }

            int color = ColorUtility.applyAlpha(particle.color(), animationAlpha);
            renderParticle(matrix, particle, color);
        }
    }

    private void renderParticle(MatrixStack matrix, Particle particle, int color) {
        Vec3d vec = particle.position();
        float size = particle.size();

        matrix.push();
        matrix.translate(vec.x, vec.y, vec.z);
        matrix.multiply(mc.getEntityRenderDispatcher().camera.getRotation());
        matrix.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180f));

        if (particle.type().rotatable()) {
            matrix.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(particle.rotate()));
        }

        if (glowing.getValue()) {
            drawTexturedQuad(matrix, Particles.ParticleType.BLOOM.texture(), -size * 4, -size * 4, size * 8, size * 8, ColorUtility.applyOpacity(color, 0.1f));
        }

        drawTexturedQuad(matrix, particle.type().texture(), -size, -size, size * 2, size * 2, color);

        if (particle.type() == ParticleType.BLOOM) {
            drawTexturedQuad(matrix, particle.type().texture(), -size / 2, -size / 2, size, size, color);
        }

        matrix.pop();
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

    private void removeExpiredParticles(List<Particle> particles, long lifespan) {
        particles.removeIf(particle -> !PlayerUtility.isInView(particle.getBox()));
        particles.removeIf(particle -> particle.time().hasReached(lifespan));
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

    public static class Point {
        private final Vec3d pos;
        private final long creationTime;

        public Point(Vec3d pos) {
            this.pos = pos;
            this.creationTime = System.currentTimeMillis();
        }

        public Vec3d getPos() {
            return pos;
        }

        public boolean isExpired(long currentTime, long lifetimeMs) {
            return currentTime - creationTime > lifetimeMs;
        }
    }

    public static class Particle {
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
        public ParticleType type() { return type; }
        public Vec3d position() { return position; }
        public int rotate() { return rotate; }
        public int color() { return color; }
        public float size() { return size; }
        public TimerUtil time() { return time; }
        public Animation animation() { return animation; }
    }
}