package win.blade.core.module.storage.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.Blocks;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ChargedProjectilesComponent;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import org.joml.Matrix4f;
import win.blade.common.utils.color.ColorUtility;
import win.blade.common.utils.math.MathUtility;
import win.blade.core.event.controllers.EventHandler;
import win.blade.core.event.impl.render.RenderEvents;
import win.blade.core.module.api.Category;
import win.blade.core.module.api.Module;
import win.blade.core.module.api.ModuleInfo;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@ModuleInfo(name = "ProjectileHelper", category = Category.RENDER, desc = "Предсказывает траекторию всех типов снарядов.")
public class ProjectileHelper extends Module {

    private static final Identifier CROSS_TEXTURE = Identifier.of("blade", "textures/cross.png");

    private final List<ProjectileTrajectory> trajectories = new ArrayList<>();

    @EventHandler
    public void onRender3D(RenderEvents.World event) {
        if (mc.world == null || mc.player == null) return;

        trajectories.clear();
        calculateAllTrajectories();

        if (trajectories.isEmpty()) return;

        MathUtility.lastMatrices(event.getMatrixStack(), RenderSystem.getProjectionMatrix());

        renderAllImpactPoints(event.getMatrixStack());
    }

    private void calculateAllTrajectories() {
        ItemStack mainHand = mc.player.getMainHandStack();
        ItemStack offHand = mc.player.getOffHandStack();

        if (isProjectileWeapon(mainHand)) {
            calculateTrajectoryForWeapon(mainHand);
        }

        if (isProjectileWeapon(offHand)) {
            calculateTrajectoryForWeapon(offHand);
        }

        if (isThrowableItem(mainHand)) {
            calculateThrowableTrajectory(mainHand);
        } else if (isThrowableItem(offHand)) {
            calculateThrowableTrajectory(offHand);
        }
    }

    private void calculateTrajectoryForWeapon(ItemStack weapon) {
        Item item = weapon.getItem();

        if (item instanceof BowItem) {
            calculateBowTrajectory(weapon);
        } else if (item instanceof CrossbowItem) {
            calculateCrossbowTrajectory(weapon);
        } else if (item instanceof TridentItem && mc.player.isUsingItem()) {
            calculateTridentTrajectory(weapon);
        }
    }

    private void calculateBowTrajectory(ItemStack bow) {
        if (!mc.player.isUsingItem() || mc.player.getActiveItem() != bow) return;

        int useTicks = mc.player.getItemUseTime();
        float pullProgress = BowItem.getPullProgress(useTicks);

        if (pullProgress < 0.1f) return;

        ItemStack arrow = findArrowInInventory();
        if (arrow.isEmpty()) arrow = new ItemStack(Items.ARROW);

        float velocity = pullProgress * 3.0f;
        ProjectileTrajectory trajectory = calculateProjectileTrajectory(arrow, velocity, 0.05f, 0.99f, false);
        if (trajectory != null) {
            trajectories.add(trajectory);
        }
    }

    private void calculateCrossbowTrajectory(ItemStack crossbow) {
        ChargedProjectilesComponent chargedProjectiles = crossbow.get(DataComponentTypes.CHARGED_PROJECTILES);
        if (chargedProjectiles == null || chargedProjectiles.isEmpty()) return;

        List<ItemStack> projectiles = chargedProjectiles.getProjectiles();
        boolean hasMultishot = hasMultishotEnchantment(crossbow);

        if (hasMultishot) {
            ItemStack mainProjectile = projectiles.get(0);
            ProjectileTrajectory mainTrajectory = calculateProjectileTrajectory(mainProjectile, 3.15f, 0.05f, 0.99f, false);
            if (mainTrajectory != null) {
                trajectories.add(mainTrajectory);
            }

            for (int i = 0; i < 2; i++) {
                float yawOffset = (i == 0) ? -10f : 10f;
                ProjectileTrajectory sideTrajectory = calculateProjectileTrajectory(mainProjectile, 3.15f, 0.05f, 0.99f, false, yawOffset, 0f);
                if (sideTrajectory != null) {
                    sideTrajectory.isMultishot = true;
                    trajectories.add(sideTrajectory);
                }
            }
        } else {
            ItemStack projectile = projectiles.get(0);
            ProjectileTrajectory trajectory = calculateProjectileTrajectory(projectile, 3.15f, 0.05f, 0.99f, false);
            if (trajectory != null) {
                trajectories.add(trajectory);
            }
        }
    }

    private void calculateTridentTrajectory(ItemStack trident) {
        int useTicks = mc.player.getItemUseTime();
        float chargeProgress = Math.min(useTicks / 10.0f, 1.0f);

        if (chargeProgress < 0.1f) return;

        float velocity = chargeProgress * 2.5f + 0.5f;
        ProjectileTrajectory trajectory = calculateProjectileTrajectory(trident, velocity, 0.05f, 0.99f, false);
        if (trajectory != null) {
            trajectories.add(trajectory);
        }
    }

    private void calculateThrowableTrajectory(ItemStack stack) {
        ProjectileTrajectory trajectory = calculateProjectileTrajectory(stack, getThrowableVelocity(stack.getItem()), getThrowableGravity(stack.getItem()), 0.99f, true);
        if (trajectory != null) {
            trajectories.add(trajectory);
        }
    }

    private ProjectileTrajectory calculateProjectileTrajectory(ItemStack projectile, float velocity, double gravity, double airResistance, boolean isThrowable) {
        return calculateProjectileTrajectory(projectile, velocity, gravity, airResistance, isThrowable, 0f, 0f);
    }

    private ProjectileTrajectory calculateProjectileTrajectory(ItemStack projectile, float velocity, double gravity, double airResistance, boolean isThrowable, float yawOffset, float pitchOffset) {
        Vec3d startPos = mc.player.getEyePos();

        if (isThrowable) {
            startPos = startPos.subtract(0, 0.1, 0);
        }

        Vec3d direction = getShootingDirection(yawOffset, pitchOffset);

        Vec3d motion = direction.multiply(velocity);

        Vec3d playerVelocity = mc.player.getVelocity();
        motion = motion.add(playerVelocity.x, playerVelocity.y, playerVelocity.z);

        List<TrajectoryPoint> points = new ArrayList<>();
        Vec3d pos = startPos;
        points.add(new TrajectoryPoint(pos, 0));

        for (int tick = 1; tick < 300; tick++) {
            Vec3d lastPos = pos;

            pos = pos.add(motion);

            motion = updateProjectileMotionAccurate(motion, pos, gravity, airResistance, isThrowable, projectile.getItem());

            points.add(new TrajectoryPoint(pos, tick));

            HitResult hitResult = checkCollisionAccurate(lastPos, pos);
            if (hitResult != null) {
                ProjectileTrajectory trajectory = new ProjectileTrajectory();
                trajectory.points = points;
                trajectory.impactPoint = hitResult.getPos();
                trajectory.flightTime = tick;
                trajectory.projectileItem = projectile.copy();
                trajectory.isMultishot = false;
                trajectory.hitEntity = hitResult instanceof EntityHitResult;
                return trajectory;
            }

            if (pos.y <= mc.world.getBottomY() || pos.y > 320) {
                ProjectileTrajectory trajectory = new ProjectileTrajectory();
                trajectory.points = points;
                trajectory.impactPoint = pos;
                trajectory.flightTime = tick;
                trajectory.projectileItem = projectile.copy();
                trajectory.isMultishot = false;
                return trajectory;
            }
        }

        return null;
    }

    private Vec3d updateProjectileMotionAccurate(Vec3d motion, Vec3d pos, double gravity, double airResistance, boolean isThrowable, Item item) {
        BlockPos blockPos = BlockPos.ofFloored(pos);
        boolean inWater = mc.world.getBlockState(blockPos).isOf(Blocks.WATER);
        boolean inLava = mc.world.getBlockState(blockPos).isOf(Blocks.LAVA);

        if (inWater) {
            if (isThrowable) {
                if (item == Items.EXPERIENCE_BOTTLE) {
                    motion = motion.multiply(0.75);
                } else {
                    motion = motion.multiply(0.8);
                }
            } else {
                motion = motion.multiply(0.6);
            }
        } else if (inLava) {
            motion = motion.multiply(0.5);
        } else {
            if (isThrowable) {
                motion = motion.multiply(0.99);
            } else {
                motion = motion.multiply(airResistance);
            }
        }

        double actualGravity = gravity;
        if (isThrowable) {
            actualGravity = getAccurateGravity(item);
        }

        motion = motion.subtract(0, actualGravity, 0);

        return motion;
    }

    private double getAccurateGravity(Item item) {
        if (item == Items.ENDER_PEARL) return 0.03;
        if (item == Items.SNOWBALL) return 0.03;
        if (item == Items.EGG) return 0.03;
        if (item == Items.SPLASH_POTION) return 0.05;
        if (item == Items.LINGERING_POTION) return 0.05;
        if (item == Items.EXPERIENCE_BOTTLE) return 0.07;
        return 0.03;
    }

    private HitResult checkCollisionAccurate(Vec3d start, Vec3d end) {
        BlockHitResult blockHit = mc.world.raycast(new RaycastContext(
                start, end,
                RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE,
                mc.player
        ));

        if (blockHit.getType() == HitResult.Type.BLOCK) {
            return blockHit;
        }

        double projectileRadius = 0.25;

        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player) continue;
            if (entity instanceof PlayerEntity && !shouldHitPlayer((PlayerEntity) entity)) continue;

            var expandedBox = entity.getBoundingBox().expand(projectileRadius);
            var raycastResult = expandedBox.raycast(start, end);

            if (raycastResult.isPresent()) {
                EntityHitResult entityHit = new EntityHitResult(entity, raycastResult.get());
                return entityHit;
            }
        }

        return null;
    }

    private Vec3d getShootingDirection(float yawOffset, float pitchOffset) {
        float pitch = (float) Math.toRadians(mc.player.getPitch() + pitchOffset);
        float yaw = (float) Math.toRadians(mc.player.getYaw() + yawOffset);

        float x = -MathHelper.sin(yaw) * MathHelper.cos(pitch);
        float y = -MathHelper.sin(pitch);
        float z = MathHelper.cos(yaw) * MathHelper.cos(pitch);

        return new Vec3d(x, y, z);
    }

    private float getThrowableVelocity(Item item) {
        if (item == Items.ENDER_PEARL) return 1.5f;
        if (item == Items.SNOWBALL) return 1.5f;
        if (item == Items.EGG) return 1.5f;
        if (item == Items.SPLASH_POTION) return 0.8f;
        if (item == Items.LINGERING_POTION) return 0.8f;
        if (item == Items.EXPERIENCE_BOTTLE) return 0.7f;
        return 1.5f;
    }

    private double getThrowableGravity(Item item) {
        if (item == Items.ENDER_PEARL) return 0.03;
        if (item == Items.SNOWBALL) return 0.03;
        if (item == Items.EGG) return 0.03;
        if (item == Items.SPLASH_POTION) return 0.05;
        if (item == Items.LINGERING_POTION) return 0.05;
        if (item == Items.EXPERIENCE_BOTTLE) return 0.07;
        return 0.03;
    }

    private ItemStack findArrowInInventory() {
        ItemStack offHand = mc.player.getOffHandStack();
        if (offHand.getItem() instanceof ArrowItem) {
            return offHand;
        }

        for (int i = 0; i < mc.player.getInventory().size(); i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() instanceof ArrowItem) {
                return stack;
            }
        }

        return ItemStack.EMPTY;
    }

    private boolean hasMultishotEnchantment(ItemStack crossbow) {
        if (mc.world == null) return false;

        try {
            var enchantments = EnchantmentHelper.getEnchantments(crossbow);

            for (var entry : enchantments.getEnchantmentEntries()) {
                var enchantment = entry.getKey();
                if (enchantment.getIdAsString().equals("minecraft:multishot")) {
                    return entry.getIntValue() > 0;
                }
            }
        } catch (Exception e) {
        }

        return false;
    }

    private HitResult checkCollision(Vec3d start, Vec3d end) {
        BlockHitResult blockHit = mc.world.raycast(new RaycastContext(start, end, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player));

        if (blockHit.getType() == HitResult.Type.BLOCK) {
            return blockHit;
        }

        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player) continue;
            if (entity instanceof PlayerEntity && !shouldHitPlayer((PlayerEntity) entity)) continue;

            if (entity.getBoundingBox().raycast(start, end).isPresent()) {
                return new EntityHitResult(entity);
            }
        }

        return null;
    }

    private boolean shouldHitPlayer(PlayerEntity player) {
        return true;
    }

    private void renderAllImpactPoints(MatrixStack matrices) {
        Camera camera = mc.gameRenderer.getCamera();
        Vec3d cameraPos = camera.getPos();

        for (ProjectileTrajectory trajectory : trajectories) {
            if (trajectory.impactPoint == null) continue;

            matrices.push();
            matrices.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
            matrices.translate(trajectory.impactPoint.x, trajectory.impactPoint.y, trajectory.impactPoint.z);

            setupRenderState();

            int color = trajectory.hitEntity ? new Color(0, 255, 0).getRGB() : new Color(255, 0, 0).getRGB();
            float[] c = ColorUtility.normalize(color);
            float size = 0.3f;

            RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
            RenderSystem.setShaderTexture(0, CROSS_TEXTURE);

            BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
            Matrix4f matrix = matrices.peek().getPositionMatrix();

            bufferBuilder.vertex(matrix, -size, -size, 0).texture(0, 0).color(c[0], c[1], c[2], c[3]);
            bufferBuilder.vertex(matrix, -size, size, 0).texture(0, 1).color(c[0], c[1], c[2], c[3]);
            bufferBuilder.vertex(matrix, size, size, 0).texture(1, 1).color(c[0], c[1], c[2], c[3]);
            bufferBuilder.vertex(matrix, size, -size, 0).texture(1, 0).color(c[0], c[1], c[2], c[3]);

            BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
            cleanupRenderState();
            matrices.pop();
        }
    }

    private boolean isProjectileWeapon(ItemStack stack) {
        if (stack.isEmpty()) return false;
        Item item = stack.getItem();
        return item instanceof BowItem || item instanceof CrossbowItem || item instanceof TridentItem;
    }

    private boolean isThrowableItem(ItemStack stack) {
        if (stack.isEmpty()) return false;

        Item item = stack.getItem();
        return item == Items.ENDER_PEARL ||
                item == Items.SNOWBALL ||
                item == Items.EGG ||
                item == Items.SPLASH_POTION ||
                item == Items.LINGERING_POTION ||
                item == Items.EXPERIENCE_BOTTLE;
    }

    private void setupRenderState() {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.disableCull();
        RenderSystem.depthMask(false);
    }

    private void cleanupRenderState() {
        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.defaultBlendFunc();
    }

    private static class ProjectileTrajectory {
        public List<TrajectoryPoint> points = new ArrayList<>();
        public Vec3d impactPoint;
        public float flightTime;
        public ItemStack projectileItem = ItemStack.EMPTY;
        public boolean isMultishot = false;
        public boolean hitEntity = false;
    }

    private static class TrajectoryPoint {
        public final Vec3d position;
        public final int tick;

        public TrajectoryPoint(Vec3d position, int tick) {
            this.position = position;
            this.tick = tick;
        }
    }
}