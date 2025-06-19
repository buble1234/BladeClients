package win.blade.core.module.storage.render;

import org.lwjgl.glfw.GLFW;
import win.blade.core.module.api.Category;
import win.blade.core.module.api.Module;
import win.blade.core.module.api.ModuleInfo;

/**
 * Автор: NoCap
 * Дата создания: 18.06.2025
 */

@ModuleInfo(
        name = "HitMarkers",
        category = Category.RENDER,
        desc = "Спавнит торусы при ударе"
)
public class TorusModule extends Module {


//
//    private static final int DURATION = 5500;
//    private static final float MAIN_RADIUS = 0.9f;
//    private static final float TUBE_RADIUS = 0.3f;
//    private static final Color TORUS_COLOR = Color.RED;
//
//    private final List<HitTorus> toruses = new ArrayList<>();
//
//    private static class HitTorus {
//        final Vec3d position;
//        final float yaw;
//        final float pitch;
//        final float roll;
//        final AnimationUtility animation;
//        float scale;
//        float alpha;
//
//        HitTorus(Vec3d position, float yaw, float pitch, float roll) {
//            this.position = position;
//            this.yaw = yaw;
//            this.pitch = pitch;
//            this.roll = roll;
//            this.scale = 0.5f;
//            this.alpha = 1.0f;
//            this.animation = new AnimationUtility(DURATION, AnimationUtility.Easing.EASE_IN_OUT_QUART, progress -> {
//                this.scale = 0.5f + (1.0f * progress);
//                this.alpha = 1.0f - progress;
//            });
//        }
//
//        void update() {
//            animation.update();
//        }
//
//        boolean isExpired() {
//            return animation.getProgress() >= 0.65f;
//        }
//    }
//
//    @Override
//    public void onEnable() {
//        System.out.println("HitMarkers включен!");
//    }
//
//    @Override
//    public void onDisable() {
//        this.toruses.clear();
//        System.out.println("HitMarkers выключен!");
//    }
//
//    @EventHandler
//    public void onAttack(PlayerActionEvents.Attack event) {
//        if (mc.player == null || mc.world == null || event.getEntity() == null) return;
//
//        Vec3d pos = event.getEntity().getBoundingBox().getCenter();
//        Vec3d playerPos = mc.player.getEyePos();
//        Vec3d relativePos = pos.subtract(playerPos);
//
//        float yaw = (float) Math.toDegrees(Math.atan2(relativePos.x, relativePos.z));
//        float pitch = (float) Math.toDegrees(Math.atan2(relativePos.y, Math.sqrt(relativePos.x * relativePos.x + relativePos.z * relativePos.z)));
//        float roll = (System.currentTimeMillis() % 4000L) / 4000F * 360F;
//
//        this.toruses.add(new HitTorus(pos, yaw, pitch, roll));
//        System.out.println("Добавлен торус в позиции: " + pos);
//    }
//
//    @EventHandler
//    public void onRender(RenderEvents.World event) {
//        if (mc.player == null || mc.world == null || this.toruses.isEmpty()) return;
//
//        MatrixStack matrices = event.getMatrixStack();
//
//        RenderSystem.enableBlend();
//        RenderSystem.defaultBlendFunc();
//        RenderSystem.disableDepthTest();
//        RenderSystem.disableCull();
//
//        for (HitTorus torus : this.toruses) {
//            torus.update();
//            renderTorus(matrices, event.getCamera(), torus);
//        }
//
//        RenderSystem.enableDepthTest();
//        RenderSystem.enableCull();
//        RenderSystem.disableBlend();
//        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
//
//        this.toruses.removeIf(HitTorus::isExpired);
//    }
//
//    private void renderTorus(MatrixStack matrices, Camera camera, HitTorus torus) {
//        float currentScale = torus.scale;
//        float alpha = torus.alpha;
//
//        Vec3d renderPos = torus.position.subtract(camera.getPos());
//
//        matrices.push();
//        matrices.translate(renderPos.x, renderPos.y + 0.5f, renderPos.z);
//        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-torus.yaw));
//        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(torus.pitch));
//        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(torus.roll));
//
//        Matrix4f matrix = matrices.peek().getPositionMatrix();
//
//        float radius = MAIN_RADIUS * currentScale;
//        float tubeRadius = TUBE_RADIUS * currentScale;
//
//        renderMirrorTorus(matrix, radius, tubeRadius, TORUS_COLOR, alpha);
//
//        matrices.pop();
//    }
//
//    private void renderMirrorTorus(Matrix4f matrix, float radius, float tubeRadius, Color color, float alpha) {
//        float torusWidth = radius * 2.0f;
//        float torusHeight = tubeRadius * 2.0f;
//
//        int packedColor = (color.getRGB() & 0xFFFFFF) | ((int) (alpha * 255) << 24);
//        new BlurBuilder()
//                .size(new SizeState(132, 312))
//                .radius(QuadRadiusState.NO_ROUND)
//                .color(new QuadColorState(new Color(0, 176, 255)))
//                .smoothness(1.0f)
//                .blurRadius(20)
//                .build()
//                .render(matrix, -radius, -tubeRadius, 0);
//
//        renderTorusGeometry(matrix, radius, tubeRadius, color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, alpha);
//    }
//
//    private void renderTorusGeometry(Matrix4f matrix, float radius, float tubeRadius, float red, float green, float blue, float alpha) {
//        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
//
//        Tessellator tessellator = Tessellator.getInstance();
//        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
//
//        int rings = 32;
//        int segments = 16;
//
//        for (int i = 0; i < rings; i++) {
//            for (int j = 0; j < segments; j++) {
//                float u1 = (float) i / rings * 2.0f * (float) Math.PI;
//                float u2 = (float) (i + 1) / rings * 2.0f * (float) Math.PI;
//                float v1 = (float) j / segments * 2.0f * (float) Math.PI;
//                float v2 = (float) (j + 1) / segments * 2.0f * (float) Math.PI;
//
//                float cosU1 = MathHelper.cos(u1);
//                float sinU1 = MathHelper.sin(u1);
//                float cosU2 = MathHelper.cos(u2);
//                float sinU2 = MathHelper.sin(u2);
//                float cosV1 = MathHelper.cos(v1);
//                float sinV1 = MathHelper.sin(v1);
//                float cosV2 = MathHelper.cos(v2);
//                float sinV2 = MathHelper.sin(v2);
//
//                float x1 = (radius + tubeRadius * cosV1) * cosU1;
//                float y1 = tubeRadius * sinV1;
//                float z1 = (radius + tubeRadius * cosV1) * sinU1;
//
//                float x2 = (radius + tubeRadius * cosV1) * cosU2;
//                float y2 = tubeRadius * sinV1;
//                float z2 = (radius + tubeRadius * cosV1) * sinU2;
//
//                float x3 = (radius + tubeRadius * cosV2) * cosU2;
//                float y3 = tubeRadius * sinV2;
//                float z3 = (radius + tubeRadius * cosV2) * sinU2;
//
//                float x4 = (radius + tubeRadius * cosV2) * cosU1;
//                float y4 = tubeRadius * sinV2;
//                float z4 = (radius + tubeRadius * cosV2) * sinU1;
//
//                buffer.vertex(matrix, x1, y1, z1).color(red, green, blue, alpha);
//                buffer.vertex(matrix, x2, y2, z2).color(red, green, blue, alpha);
//                buffer.vertex(matrix, x3, y3, z3).color(red, green, blue, alpha);
//                buffer.vertex(matrix, x4, y4, z4).color(red, green, blue, alpha);
//            }
//        }
//
//        BufferRenderer.drawWithGlobalProgram(buffer.end());
//    }
}