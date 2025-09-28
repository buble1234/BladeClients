package win.blade.common.utils.math;

import net.minecraft.block.Block;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import win.blade.common.utils.minecraft.MinecraftInstance;

import java.util.function.Predicate;

public class MathUtility implements MinecraftInstance {

    public static final Matrix4f lastProjMat = new Matrix4f();
    public static final Matrix4f lastModMat = new Matrix4f();
    public static final Matrix4f lastWorldSpaceMatrix = new Matrix4f();

    public static boolean isHovered(double mouseX, double mouseY, double x, double y, double width, double height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    public static Vec3d cosSin(int i, int size, double width) {
         double PI2 = Math.PI * 2;
        int index = Math.min(i, size);
        float cos = (float) (Math.cos(index * PI2 / size) * width);
        float sin = (float) (-Math.sin(index * PI2 / size) * width);
        return new Vec3d(cos, 0, sin);
    }


    public static BlockHitResult raycast(Vec3d start, Vec3d end, RaycastContext.ShapeType shapeType, Entity entity) {
        return raycast(start, end, shapeType, RaycastContext.FluidHandling.NONE, entity);
    }

    public static BlockHitResult raycast(Vec3d start, Vec3d end, RaycastContext.ShapeType shapeType, RaycastContext.FluidHandling fluidHandling, Entity entity) {
        return mc.world.raycast(new RaycastContext(start, end, shapeType, fluidHandling, entity));
    }
    public static Vec3d interpolate(Entity entity, float partialTicks) {
        double posX = lerp(entity.lastRenderX, entity.getX(), partialTicks);
        double posY = lerp(entity.lastRenderY, entity.getY(), partialTicks);
        double posZ = lerp(entity.lastRenderZ, entity.getZ(), partialTicks);
        return new Vec3d(posX, posY, posZ);
    }

    public static boolean isBoxInBlock(Box box, Block block) {
        return isBox(box,pos -> mc.world.getBlockState(pos).getBlock().equals(block));
    }

    public static boolean isBox(Box box, Predicate<BlockPos> pos) {
        return BlockPos.stream(box).anyMatch(pos);
    }

    public static double lerp(double input, double target, double step) {
        return input + step * (target - input);
    }



    public float lerp(float input, float target, double step) {
        return (float) (input + step * (target - input));
    }

    public int lerp(int input, int target, double step) {
        return (int) (input + step * (target - input));
    }



    public static void defaultDrawStack(DrawContext context, ItemStack stack, float x, float y, boolean rect, boolean drawItemInSlot, float scale) {
        MatrixStack matrix = context.getMatrices();
        matrix.push();
        matrix.translate(x + 1, y + 1, 0);
        matrix.scale(scale, scale, 1);
        context.drawItem(stack, 0, 0);
        if (drawItemInSlot) context.drawStackOverlay(mc.textRenderer, stack, 0, 0);
        matrix.pop();
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    public static @NotNull Vec3d worldSpaceToScreenSpace(@NotNull Vec3d pos) {
        Camera camera = mc.getEntityRenderDispatcher().camera;
        int displayHeight = mc.getWindow().getHeight();
        int[] viewport = new int[4];
        GL11.glGetIntegerv(GL11.GL_VIEWPORT, viewport);
        Vector3f target = new Vector3f();

        double deltaX = pos.x - camera.getPos().x;
        double deltaY = pos.y - camera.getPos().y;
        double deltaZ = pos.z - camera.getPos().z;

        Vector4f transformedCoordinates = new Vector4f((float) deltaX, (float) deltaY, (float) deltaZ, 1.f).mul(lastWorldSpaceMatrix);
        Matrix4f matrixProj = new Matrix4f(lastProjMat);
        Matrix4f matrixModel = new Matrix4f(lastModMat);
        matrixProj.mul(matrixModel).project(transformedCoordinates.x(), transformedCoordinates.y(), transformedCoordinates.z(), viewport, target);

        return new Vec3d(target.x / mc.getWindow().getScaleFactor(), (displayHeight - target.y) / mc.getWindow().getScaleFactor(), target.z);
    }

    public static boolean canRaytraceToTarget(Entity target, float maxDistance, boolean ignoreBlocks) {
        if (target == null || mc.player == null) {
            return false;
        }

        Vec3d eyePos = mc.player.getEyePos();
        Vec3d targetCenter = target.getBoundingBox().getCenter();
        Vec3d direction = targetCenter.subtract(eyePos).normalize();
        Vec3d endPoint = eyePos.add(direction.multiply(maxDistance));

        double blockHitDistanceSq = maxDistance * maxDistance;
        BlockHitResult blockHitResult = null;

        if (!ignoreBlocks) {
            blockHitResult = mc.world.raycast(new RaycastContext(eyePos, endPoint, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, mc.player));
            if (blockHitResult != null && blockHitResult.getType() == HitResult.Type.BLOCK) {
                blockHitDistanceSq = eyePos.squaredDistanceTo(blockHitResult.getPos());
            }
        } else {
        }

        Box entitySearchBox = mc.player.getBoundingBox().stretch(direction.multiply(maxDistance)).expand(1.0, 1.0, 1.0);
        EntityHitResult entityHitResult = ProjectileUtil.raycast(mc.player, eyePos, endPoint, entitySearchBox, e -> !e.isSpectator() && e.canHit() && e == target, maxDistance * maxDistance);

        if (entityHitResult != null && entityHitResult.getEntity() == target) {
            double entityHitDistanceSq = eyePos.squaredDistanceTo(entityHitResult.getPos());
            return entityHitDistanceSq < blockHitDistanceSq;
        }

        return false;
    }

    public static void lastMatrices(MatrixStack matrixStack, Matrix4f projectionMatrix) {
        if (projectionMatrix != null) {
            lastProjMat.set(projectionMatrix);
        }
        lastModMat.set(matrixStack.peek().getPositionMatrix());
        if (projectionMatrix != null) {
            lastWorldSpaceMatrix.set(projectionMatrix).mul(matrixStack.peek().getPositionMatrix());
        }
    }

    public static float wrapDegrees(float degrees) {
        degrees = degrees % 360.0f;

        if (degrees >= 180.0f) {
            degrees -= 360.0f;
        } else if (degrees < -180.0f) {
            degrees += 360.0f;
        }

        return degrees;
    }

    public static double wrapDegrees(double degrees) {
        degrees = degrees % 360.0;

        if (degrees >= 180.0) {
            degrees -= 360.0;
        } else if (degrees < -180.0) {
            degrees += 360.0;
        }

        return degrees;
    }

    public static float angleDifference(float angle1, float angle2) {
        return wrapDegrees(angle2 - angle1);
    }

    public static float lerpAngle(float from, float to, float factor) {
        float difference = angleDifference(from, to);
        return from + difference * factor;
    }

    public static double sin(double radians) {
        return Math.sin(radians);
    }

    public static float sin(float radians) {
        return (float) Math.sin(radians);
    }

    public static double cos(double radians) {
        return Math.cos(radians);
    }

    public static float cos(float radians) {
        return (float) Math.cos(radians);
    }

    public static double tan(double radians) {
        return Math.tan(radians);
    }

    public static float tan(float radians) {
        return (float) Math.tan(radians);
    }

    public static double asin(double value) {
        return Math.asin(value);
    }

    public static double acos(double value) {
        return Math.acos(value);
    }

    public static double atan(double value) {
        return Math.atan(value);
    }

    public static double atan2(double y, double x) {
        return Math.atan2(y, x);
    }

    public static double toRadians(double degrees) {
        return Math.toRadians(degrees);
    }

    public static float toRadians(float degrees) {
        return (float) Math.toRadians(degrees);
    }

    public static double toDegrees(double radians) {
        return Math.toDegrees(radians);
    }

    public static float toDegrees(float radians) {
        return (float) Math.toDegrees(radians);
    }

    public static double sqrt(double value) {
        return Math.sqrt(value);
    }

    public static float sqrt(float value) {
        return (float) Math.sqrt(value);
    }

    public static double pow(double base, double exponent) {
        return Math.pow(base, exponent);
    }

    public static double abs(double value) {
        return Math.abs(value);
    }

    public static float abs(float value) {
        return Math.abs(value);
    }

    public static int abs(int value) {
        return Math.abs(value);
    }

    public static double min(double a, double b) {
        return Math.min(a, b);
    }

    public static float min(float a, float b) {
        return Math.min(a, b);
    }

    public static int min(int a, int b) {
        return Math.min(a, b);
    }

    public static double max(double a, double b) {
        return Math.max(a, b);
    }

    public static float max(float a, float b) {
        return Math.max(a, b);
    }

    public static int max(int a, int b) {
        return Math.max(a, b);
    }

    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    public static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public static long round(double value) {
        return Math.round(value);
    }

    public static int round(float value) {
        return Math.round(value);
    }

    public static double floor(double value) {
        return Math.floor(value);
    }

    public static double ceil(double value) {
        return Math.ceil(value);
    }

    public static double distance2D(double x1, double y1, double x2, double y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        return sqrt(dx * dx + dy * dy);
    }

    public static double distance3D(double x1, double y1, double z1, double x2, double y2, double z2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double dz = z2 - z1;
        return sqrt(dx * dx + dy * dy + dz * dz);
    }

    public static float normalizeAngle(float angle) {
        return (angle + 180f) % 360f - 180f;
    }
}