package win.blade.common.utils.math;

import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
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

public class MathUtility implements MinecraftInstance {

    public static final Matrix4f lastProjMat = new Matrix4f();
    public static final Matrix4f lastModMat = new Matrix4f();
    public static final Matrix4f lastWorldSpaceMatrix = new Matrix4f();

    public static boolean isHovered(double mouseX, double mouseY, double x, double y, double width, double height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    public static Vec3d interpolate(Entity entity, float partialTicks) {
        double posX = lerp(entity.lastRenderX, entity.getX(), partialTicks);
        double posY = lerp(entity.lastRenderY, entity.getY(), partialTicks);
        double posZ = lerp(entity.lastRenderZ, entity.getZ(), partialTicks);
        return new Vec3d(posX, posY, posZ);
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
}