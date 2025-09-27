package win.blade.core.module.storage.render;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ChargedProjectilesComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.entity.projectile.thrown.*;
import net.minecraft.item.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import org.joml.Quaternionf;
import win.blade.common.gui.impl.gui.setting.implement.ColorSetting;
import win.blade.common.gui.impl.gui.setting.implement.ValueSetting;
import win.blade.common.utils.math.RenderUtility;
import win.blade.core.event.controllers.EventHandler;
import win.blade.core.event.impl.render.RenderEvents;
import win.blade.core.module.api.Category;
import win.blade.core.module.api.Module;
import win.blade.core.module.api.ModuleInfo;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

@ModuleInfo(name = "ProjectileHelper", category = Category.RENDER, desc = "Предсказывает траекторию всех типов снарядов.")
public class ProjectileHelper extends Module {

    private final ValueSetting thickness = new ValueSetting("Толщина", "Толщина линии цели.").setValue(1.0f).range(0.5f, 5.0f);
    private final ColorSetting color = new ColorSetting("Цвет", "Цвет цели.").value(new Color(100, 150, 255, 255).getRGB());
    private final ValueSetting circleWidth = new ValueSetting("Радиус круга", "Радиус круга в точке попадания.").setValue(0.3f).range(0.1f, 1.0f);

    public ProjectileHelper() {
        addSettings(thickness, color, circleWidth);
    }

    @EventHandler
    public void onRender3D(RenderEvents.World event) {
        if (mc.world == null || mc.player == null) return;
        drawPredictionInHand(event.getMatrixStack(), mc.player.getMainHandStack());
    }

    public void drawPredictionInHand(MatrixStack matrix, ItemStack stack) {
        List<HitResult> results = getHitResults(stack);
        if (results != null) {
            results = results.stream().filter(Objects::nonNull).toList();
            if (!results.isEmpty()) {
                renderProjectileResults(matrix, results);
            }
        }
    }

    private List<HitResult> getHitResults(ItemStack stack) {
        Item item = stack.getItem();
        float yaw = mc.player.getYaw();
        float pitch = mc.player.getPitch();

        if (item instanceof BowItem && mc.player.isUsingItem()) {
            float power = (72000 - mc.player.getItemUseTime()) / 20.0f;
            power = (power * power + power * 2.0f) / 3.0f;
            if (power > 1.0f) power = 1.0f;
            return Collections.singletonList(traceTrajectory(new ArrowEntity(mc.world, mc.player, stack, stack), yaw, pitch, power * 3.0f));
        }
        if (item instanceof CrossbowItem && CrossbowItem.isCharged(stack)) {
            ChargedProjectilesComponent component = stack.get(DataComponentTypes.CHARGED_PROJECTILES);
            if (component == null) return null;
            float velocity = component.getProjectiles().getFirst().isOf(Items.FIREWORK_ROCKET) ? 1.6f : 3.15f;
            return Collections.singletonList(traceTrajectory(new ArrowEntity(mc.world, mc.player, stack, stack), yaw, pitch, velocity));
        }
        if (item instanceof EnderPearlItem) return Collections.singletonList(traceTrajectory(new EnderPearlEntity(mc.world, mc.player, stack), yaw, pitch, 1.5f));
        if (item instanceof ExperienceBottleItem) return Collections.singletonList(traceTrajectory(new ExperienceBottleEntity(mc.world, mc.player, stack), yaw, pitch, 0.7f));
        if (item instanceof SnowballItem) return Collections.singletonList(traceTrajectory(new SnowballEntity(mc.world, mc.player, stack), yaw, pitch, 1.5f));
        if (item instanceof EggItem) return Collections.singletonList(traceTrajectory(new EggEntity(mc.world, mc.player, stack), yaw, pitch, 1.5f));
        if (item instanceof SplashPotionItem || item instanceof LingeringPotionItem) return Collections.singletonList(traceTrajectory(new PotionEntity(mc.world, mc.player, stack), yaw, pitch, 0.5f));
        if (item instanceof TridentItem && mc.player.isUsingItem()) return Collections.singletonList(traceTrajectory(new TridentEntity(mc.world, mc.player, stack), yaw, pitch, 2.5f));

        return null;
    }

    public HitResult traceTrajectory(ProjectileEntity entity, float yaw, float pitch, float velocity) {
        Vec3d pos = new Vec3d(mc.player.getX(), mc.player.getEyeY() - 0.1, mc.player.getZ());

        Vec3d motion = new Vec3d(
                -MathHelper.sin(yaw * 0.017453292F) * MathHelper.cos(pitch * 0.017453292F),
                -MathHelper.sin(pitch * 0.017453292F),
                MathHelper.cos(yaw * 0.017453292F) * MathHelper.cos(pitch * 0.017453292F)
        );

        motion = motion.normalize().multiply(velocity);

        float gravity = getGravity(entity);

        for (int i = 0; i < 300; i++) {
            Vec3d prevPos = pos;
            pos = pos.add(motion);

            HitResult blockHit = mc.world.raycast(new RaycastContext(prevPos, pos, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player));
            if (blockHit.getType() != HitResult.Type.MISS) return blockHit;

            HitResult entityHit = getEntityHit(prevPos, pos, entity);
            if (entityHit != null) return entityHit;

            motion = motion.multiply(0.99f);
            motion = motion.subtract(0, gravity, 0);
        }
        return null;
    }

    private HitResult getEntityHit(Vec3d start, Vec3d end, ProjectileEntity projectile) {
        Entity hitEntity = null;
        double minDistanceSq = Double.MAX_VALUE;

        Predicate<Entity> predicate = e -> !e.isSpectator() && e.isAlive() && e.canHit() && e != mc.player && e != projectile.getOwner();
        Box box = new Box(start, end).expand(1.0);

        for (Entity entity : mc.world.getOtherEntities(mc.player, box, predicate)) {
            Box entityBox = entity.getBoundingBox().expand(0.3);
            if (entityBox.raycast(start, end).isPresent()) {
                double distSq = start.squaredDistanceTo(entity.getPos());
                if (distSq < minDistanceSq) {
                    minDistanceSq = distSq;
                    hitEntity = entity;
                }
            }
        }
        return hitEntity != null ? new EntityHitResult(hitEntity) : null;
    }

    private float getGravity(ProjectileEntity entity) {
        if (entity instanceof PotionEntity) return 0.05F;
        if (entity instanceof TridentEntity) return 0.05F;
        return 0.03F;
    }

    public void renderProjectileResults(MatrixStack matrix, List<HitResult> results) {
        for (HitResult result : results) {
            int resultColor = result.getType() == HitResult.Type.ENTITY ? Color.RED.getRGB() : color.getColor();
            float width = circleWidth.getValue();
            float thick = thickness.getValue();

            matrix.push();
            matrix.translate(result.getPos().x, result.getPos().y, result.getPos().z);

            if (result instanceof BlockHitResult blockHitResult) {
                Direction side = blockHitResult.getSide();
                matrix.multiply(side.getRotationQuaternion());
            }

            MatrixStack.Entry entry = matrix.peek();

            for (int i = 0, size = 90; i <= size; i++) {
                RenderUtility.drawLine(entry, cosSin(i, size, width), cosSin(i + 1, size, width), resultColor, thick, false);
            }
            RenderUtility.drawLine(entry, new Vec3d(0, -width, 0), new Vec3d(0, width, 0), resultColor, thick, false);
            RenderUtility.drawLine(entry, new Vec3d(-width, 0, 0), new Vec3d(width, 0, 0), resultColor, thick, false);
            matrix.pop();
        }
        RenderUtility.renderQueues();
    }

    private Vec3d cosSin(int index, int total, double radius) {
        double angle = Math.toRadians(((double) index / total) * 360.0);
        return new Vec3d(Math.cos(angle) * radius, Math.sin(angle) * radius, 0);
    }
}