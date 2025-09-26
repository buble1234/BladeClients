package win.blade.core.module.storage.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ChargedProjectilesComponent;
import net.minecraft.item.*;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.RaycastContext;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import win.blade.common.gui.impl.gui.setting.implement.ColorSetting;
import win.blade.common.gui.impl.gui.setting.implement.ValueSetting;
import win.blade.common.utils.color.ColorUtility;
import win.blade.core.event.controllers.EventHandler;
import win.blade.core.event.impl.render.RenderEvents;
import win.blade.core.module.api.Category;
import win.blade.core.module.api.Module;
import win.blade.core.module.api.ModuleInfo;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@ModuleInfo(name = "ProjectileHelper", category = Category.RENDER, desc = "Предсказывает траекторию всех типов снарядов.")
public class ProjectileHelper extends Module {

    private final ValueSetting thickness = new ValueSetting("Толщина", "Толщина линии цели.").setValue(1.0f).range(0.5f, 5.0f);
    private final ColorSetting color = new ColorSetting("Цвет", "Цвет цели.").value(new Color(100, 150, 255, 255).getRGB());
    private final ValueSetting circleWidth = new ValueSetting("Радиус круга", "Радиус круга в точке попадания.").setValue(0.3f).range(0.1f, 1.0f);

    private static final Identifier BLOOM_TEXTURE = Identifier.of("blade", "textures/particle/bloom.png");

    public ProjectileHelper() {
        addSettings(thickness, color, circleWidth);
    }

    @EventHandler
    public void onRender3D(RenderEvents.World event) {
        if (mc.world == null || mc.player == null || mc.gameRenderer == null) return;

        setupRenderState();
        drawPredictionInHand(event.getMatrixStack(), event.getPartialTicks());
        cleanupRenderState();
    }

    private void drawPredictionInHand(MatrixStack matrices, float tickDelta) {
        Item activeItem = mc.player.getActiveItem().getItem();
        Vec3d playerLookVec = mc.player.getRotationVec(tickDelta);

        for (ItemStack stack : mc.player.getHandItems()) {
            List<HitResult> results = switch (stack.getItem()) {
                case ExperienceBottleItem item -> List.of(calculateHitResult(playerLookVec, 0.7, 0.07f));
                case SplashPotionItem item -> List.of(calculateHitResult(playerLookVec, 0.5, 0.05f));
                case LingeringPotionItem item -> List.of(calculateHitResult(playerLookVec, 0.5, 0.05f));
                case TridentItem item when item.equals(activeItem) && mc.player.getItemUseTime() >= 10 -> List.of(calculateHitResult(playerLookVec, 2.5, 0.05f));
                case SnowballItem item -> List.of(calculateHitResult(playerLookVec, 1.5, 0.03f));
                case EggItem item -> List.of(calculateHitResult(playerLookVec, 1.5, 0.03f));
                case EnderPearlItem item -> List.of(calculateHitResult(playerLookVec, 1.5, 0.03f));
                case BowItem item when item.equals(activeItem) && mc.player.isUsingItem() -> {
                    float power = BowItem.getPullProgress(mc.player.getItemUseTime());
                    yield List.of(calculateHitResult(playerLookVec, power * 3.0, 0.05f));
                }
                case CrossbowItem item when CrossbowItem.isCharged(stack) -> {
                    ChargedProjectilesComponent component = stack.get(DataComponentTypes.CHARGED_PROJECTILES);
                    List<HitResult> list = new ArrayList<>();
                    if (component != null) {
                        float velocity = component.getProjectiles().getFirst().isOf(Items.FIREWORK_ROCKET) ? 1.6f : 3.15f;
                        list.add(calculateHitResult(playerLookVec, velocity, 0.05f));
                    }
                    yield list;
                }
                default -> null;
            };

            if (results != null) {
                results = results.stream().filter(r -> r != null && r.getType() != HitResult.Type.MISS).toList();
                if (!results.isEmpty()) {
                    renderImpacts(matrices, mc.gameRenderer.getCamera(), results);
                }
                return;
            }
        }
    }

    private HitResult calculateHitResult(Vec3d lookVec, double velocity, double gravity) {
        Vec3d pos = mc.player.getEyePos();
        Vec3d motion = lookVec.multiply(velocity);

        for (int i = 0; i < 300; i++) {
            Vec3d lastPos = pos;
            pos = pos.add(motion);
            motion = getUpdatedMotion(motion, pos, gravity);

            HitResult collisionResult = mc.world.raycast(new RaycastContext(lastPos, pos, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player));
            if (collisionResult.getType() != HitResult.Type.MISS) {
                return collisionResult;
            }

            if (pos.y < mc.world.getBottomY()) {
                return new BlockHitResult(pos, Direction.DOWN, BlockPos.ofFloored(pos), false);
            }
        }
        return new BlockHitResult(pos, Direction.DOWN, BlockPos.ofFloored(pos), false);
    }

    private Vec3d getUpdatedMotion(Vec3d motion, Vec3d pos, double gravity) {
        boolean inWater = mc.world.getFluidState(BlockPos.ofFloored(pos)).isIn(FluidTags.WATER);
        double drag = inWater ? 0.8 : 0.99;
        return motion.multiply(drag).subtract(0, gravity, 0);
    }

    private void renderImpacts(MatrixStack matrices, Camera camera, List<HitResult> results) {
        Vec3d cameraPos = camera.getPos();
        matrices.push();
        matrices.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        float quadRadius = thickness.getValue() * 0.02f;
        float[] c = ColorUtility.normalize(color.getColor());

        RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
        RenderSystem.setShaderTexture(0, BLOOM_TEXTURE);
        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);

        for (HitResult result : results) {
            drawGlowingCircle(matrices, camera.getRotation(), bufferBuilder, result, quadRadius, c);
        }

        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        matrices.pop();
    }

    private void drawGlowingCircle(MatrixStack matrices, Quaternionf cameraRotation, BufferBuilder bufferBuilder, HitResult result, float quadRadius, float[] color) {
        Vec3d center = result.getPos();
        float width = circleWidth.getValue();
        Direction direction = getDirection(result);

        Quaternionf surfaceRotation = switch (direction) {
            case WEST, EAST -> RotationAxis.POSITIVE_Z.rotationDegrees(90);
            case NORTH, SOUTH -> RotationAxis.POSITIVE_X.rotationDegrees(90);
            default -> new Quaternionf();
        };

        // Рендер круга
        int segments = 90;
        for (int i = 0; i < segments; i++) {
            double angle1 = i * (Math.PI * 2) / segments;
            double angle2 = (i + 1) * (Math.PI * 2) / segments;

            Vec3d p1_local = new Vec3d(Math.cos(angle1) * width, 0, Math.sin(angle1) * width);
            Vec3d p2_local = new Vec3d(Math.cos(angle2) * width, 0, Math.sin(angle2) * width);

            addGlowingLineToBuffer(matrices, cameraRotation, bufferBuilder,
                    transformLocalToWorld(p1_local, surfaceRotation, center),
                    transformLocalToWorld(p2_local, surfaceRotation, center),
                    quadRadius, color);
        }

        // Рендер перекрестия
        Vec3d cross1_start_local = new Vec3d(-width, 0, 0);
        Vec3d cross1_end_local = new Vec3d(width, 0, 0);
        Vec3d cross2_start_local = new Vec3d(0, 0, -width);
        Vec3d cross2_end_local = new Vec3d(0, 0, width);

        addGlowingLineToBuffer(matrices, cameraRotation, bufferBuilder,
                transformLocalToWorld(cross1_start_local, surfaceRotation, center),
                transformLocalToWorld(cross1_end_local, surfaceRotation, center),
                quadRadius, color);
        addGlowingLineToBuffer(matrices, cameraRotation, bufferBuilder,
                transformLocalToWorld(cross2_start_local, surfaceRotation, center),
                transformLocalToWorld(cross2_end_local, surfaceRotation, center),
                quadRadius, color);
    }

    private Vec3d transformLocalToWorld(Vec3d local, Quaternionf rotation, Vec3d origin) {
        Vector3f local_f = local.toVector3f();
        rotation.transform(local_f);
        return origin.add(new Vec3d(local_f));
    }

    private void addGlowingLineToBuffer(MatrixStack matrices, Quaternionf cameraRotation, BufferBuilder bufferBuilder, Vec3d start, Vec3d end, float quadRadius, float[] color) {
        int segments = (int) Math.max(1, start.distanceTo(end) * 20);
        for (int j = 0; j <= segments; j++) {
            Vec3d interpPos = start.lerp(end, (double) j / segments);

            matrices.push();
            matrices.translate(interpPos.x, interpPos.y, interpPos.z);
            matrices.multiply(cameraRotation);

            Matrix4f matrix = matrices.peek().getPositionMatrix();
            bufferBuilder.vertex(matrix, -quadRadius, -quadRadius, 0).texture(0, 0).color(color[0], color[1], color[2], color[3]);
            bufferBuilder.vertex(matrix, -quadRadius, quadRadius, 0).texture(0, 1).color(color[0], color[1], color[2], color[3]);
            bufferBuilder.vertex(matrix, quadRadius, quadRadius, 0).texture(1, 1).color(color[0], color[1], color[2], color[3]);
            bufferBuilder.vertex(matrix, quadRadius, -quadRadius, 0).texture(1, 0).color(color[0], color[1], color[2], color[3]);

            matrices.pop();
        }
    }

    private Direction getDirection(HitResult result) {
        if (result instanceof BlockHitResult blockHitResult) {
            return blockHitResult.getSide();
        }
        return Direction.getFacing(result.getPos().subtract(mc.player.getEyePos()).normalize());
    }

    private void setupRenderState() {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
        RenderSystem.disableCull();
        RenderSystem.depthMask(false);
    }

    private void cleanupRenderState() {
        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.defaultBlendFunc();
    }
}