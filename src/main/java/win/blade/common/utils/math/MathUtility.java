package win.blade.common.utils.math;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.ColorHelper;

public class MathUtility {

import net.minecraft.client.render.Camera;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
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
    public static void scale(MatrixStack stack,
                             float x,
                             float y,
                             float scale,
                             Runnable data) {

        stack.push();
        stack.translate(x, y, 0);
        stack.scale(scale, scale, 1);
        stack.translate(-x, -y, 0);
        data.run();
        stack.pop();

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

    public static double getScale(Vec3d interpolated, double size){
        Vec3d cam = mc.gameRenderer.getCamera().getPos();
        double distance = cam.distanceTo(interpolated);

        double fov = mc.gameRenderer.getFov(mc.gameRenderer.getCamera(), 0, true);

        return Math.max(1f, 1000 / distance) * (size / 30.0f) / (fov == 70 ? 1 : fov / 70.0f);
    }

}
