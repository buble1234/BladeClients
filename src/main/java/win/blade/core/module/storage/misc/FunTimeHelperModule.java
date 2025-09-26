package win.blade.core.module.storage.misc;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.RaycastContext;
import org.joml.Matrix4f;
import win.blade.common.gui.impl.gui.setting.implement.BooleanSetting;
import win.blade.common.gui.impl.gui.setting.implement.GroupSetting;
import win.blade.common.utils.color.ColorUtility;
import win.blade.core.event.controllers.EventHandler;
import win.blade.core.event.impl.render.RenderEvents;
import win.blade.core.module.api.Category;
import win.blade.core.module.api.Module;
import win.blade.core.module.api.ModuleInfo;

import java.awt.Color;

@ModuleInfo(name = "FunTimeHelper", category = Category.MISC, desc = "Помощник для сервера FunTime")
public class FunTimeHelperModule extends Module {

    private final GroupSetting settings = new GroupSetting("Настройки", "Настройки помощника.").setToggleable().settings(
            new BooleanSetting("Отображать радиус", "Отображает радиус предметов.").setValue(true)
    );

    public FunTimeHelperModule() {
        addSettings(settings);
    }

    @EventHandler
    public void onRender3D(RenderEvents.World event) {
        if (mc.player == null || mc.world == null) return;

        BooleanSetting enabledSetting = (BooleanSetting) settings.getSubSetting("Отображать радиус");
        if (enabledSetting == null || !enabledSetting.getValue()) return;

        int color = new Color(255, 255, 255, 100).getRGB();

        try {
            ItemStack mainHandItem = mc.player.getMainHandStack();
            ItemStack offHandItem = mc.player.getOffHandStack();

            if (isHolding(mainHandItem, Items.ENDER_EYE) || isHolding(offHandItem, Items.ENDER_EYE)) {
                renderRadius(event, 10.0f, color);
                return;
            }

            if (isHolding(mainHandItem, Items.SUGAR) || isHolding(offHandItem, Items.SUGAR)) {
                renderRadius(event, 10.0f, color);
                return;
            }

            if (isHolding(mainHandItem, Items.FIRE_CHARGE) || isHolding(offHandItem, Items.FIRE_CHARGE)) {
                renderRadius(event, 10.0f, color);
                return;
            }

            if (isHolding(mainHandItem, Items.PHANTOM_MEMBRANE) || isHolding(offHandItem, Items.PHANTOM_MEMBRANE)) {
                renderRadius(event, 2.0f, color);
                return;
            }

            if (isHolding(mainHandItem, Items.NETHERITE_SCRAP) || isHolding(offHandItem, Items.NETHERITE_SCRAP)) {
                renderCube(event, color);
                return;
            }

            if (isHolding(mainHandItem, Items.DRIED_KELP) || isHolding(offHandItem, Items.DRIED_KELP)) {
                renderPlane(event, color);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isHolding(ItemStack stack, Item item) {
        return !stack.isEmpty() && stack.getItem() == item;
    }

    private void renderRadius(RenderEvents.World event, float radius, int color) {
        if (mc.player == null) return;

        MatrixStack matrixStack = event.getMatrixStack();
        Vec3d cameraPos = mc.gameRenderer.getCamera().getPos();

        matrixStack.push();
        matrixStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        Vec3d playerPos = mc.player.getPos();
        Vec3d position = playerPos.add(0.0, 0.1, 0.0);

        setupRenderState();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
        Matrix4f matrix = matrixStack.peek().getPositionMatrix();
        float[] colorArray = ColorUtility.normalize(color);

        bufferBuilder.vertex(matrix, (float) position.x, (float) position.y, (float) position.z).color(colorArray[0], colorArray[1], colorArray[2], colorArray[3]);

        int segments = 72;
        for (int i = 0; i <= segments; i++) {
            float angle = (float) (i * 2 * Math.PI / segments);
            float x = (float) (position.x + (MathHelper.sin(angle) * radius));
            float z = (float) (position.z + (-MathHelper.cos(angle) * radius));
            bufferBuilder.vertex(matrix, x, (float) position.y, z).color(colorArray[0], colorArray[1], colorArray[2], colorArray[3]);
        }
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());

        bufferBuilder = tessellator.begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);
        for (int i = 0; i < segments; i++) {
            float angle = (float) (i * 2 * Math.PI / segments);
            float x = (float) (position.x + (MathHelper.sin(angle) * radius));
            float z = (float) (position.z + (-MathHelper.cos(angle) * radius));
            bufferBuilder.vertex(matrix, x, (float) position.y, z).color(colorArray[0], colorArray[1], colorArray[2], 1.0f);
        }
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());

        cleanupRenderState();
        matrixStack.pop();
    }

    private void renderCube(RenderEvents.World event, int color) {
        if (mc.player == null) return;

        MatrixStack matrixStack = event.getMatrixStack();
        Vec3d cameraPos = mc.gameRenderer.getCamera().getPos();

        matrixStack.push();
        matrixStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        Vec3d playerPos = mc.player.getPos();
        double cubeX = Math.floor(playerPos.x) + 0.5;
        double cubeY = Math.floor(playerPos.y) + 2.0;
        double cubeZ = Math.floor(playerPos.z) + 0.5;

        setupRenderState();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);

        matrixStack.translate(cubeX, cubeY, cubeZ);

        float size = 4.0f;
        float halfSize = size / 2.0f;

        float[][] vertices = {
                {-halfSize, -halfSize, -halfSize}, { halfSize, -halfSize, -halfSize},
                { halfSize,  halfSize, -halfSize}, {-halfSize,  halfSize, -halfSize},
                {-halfSize, -halfSize,  halfSize}, { halfSize, -halfSize,  halfSize},
                { halfSize,  halfSize,  halfSize}, {-halfSize,  halfSize,  halfSize}
        };

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);
        Matrix4f matrix = matrixStack.peek().getPositionMatrix();
        float[] colorArray = ColorUtility.normalize(color);

        int[] outline = {0, 1, 2, 3, 0, 4, 5, 6, 7, 4};
        for (int i : outline) {
            bufferBuilder.vertex(matrix, vertices[i][0], vertices[i][1], vertices[i][2]).color(colorArray[0], colorArray[1], colorArray[2], 1.0f);
        }
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());

        bufferBuilder = tessellator.begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);
        int[] verticalLines1 = {1, 5};
        for (int i : verticalLines1) {
            bufferBuilder.vertex(matrix, vertices[i][0], vertices[i][1], vertices[i][2]).color(colorArray[0], colorArray[1], colorArray[2], 1.0f);
        }
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());

        bufferBuilder = tessellator.begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);
        int[] verticalLines2 = {2, 6};
        for (int i : verticalLines2) {
            bufferBuilder.vertex(matrix, vertices[i][0], vertices[i][1], vertices[i][2]).color(colorArray[0], colorArray[1], colorArray[2], 1.0f);
        }
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());

        bufferBuilder = tessellator.begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);
        int[] verticalLines3 = {3, 7};
        for (int i : verticalLines3) {
            bufferBuilder.vertex(matrix, vertices[i][0], vertices[i][1], vertices[i][2]).color(colorArray[0], colorArray[1], colorArray[2], 1.0f);
        }
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());

        cleanupRenderState();
        matrixStack.pop();
    }

    private void renderPlane(RenderEvents.World event, int color) {
        if (mc.player == null || mc.world == null || mc.cameraEntity == null) return;

        MatrixStack matrixStack = event.getMatrixStack();
        Vec3d cameraPos = mc.gameRenderer.getCamera().getPos();

        matrixStack.push();
        matrixStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        setupRenderState();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);

        float pitch = mc.player.getPitch();
        float yaw = MathHelper.wrapDegrees(mc.player.getYaw());

        if (Math.abs(pitch) > 60.0f) {
            Vec3d start = mc.player.getEyePos();
            Vec3d end = start.add(mc.player.getRotationVector().multiply(5.0));
            RaycastContext context = new RaycastContext(start, end, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, mc.player);
            Vec3d targetPos = mc.world.raycast(context).getPos();

            matrixStack.push();
            matrixStack.translate(targetPos.x, targetPos.y, targetPos.z);
            matrixStack.multiply(mc.gameRenderer.getCamera().getRotation());
            matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180));

            float width = 5.0f;
            float height = 4.0f;
            float halfWidth = width / 2.0f;
            float halfHeight = height / 2.0f;

            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferBuilder = tessellator.begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);
            Matrix4f matrix = matrixStack.peek().getPositionMatrix();
            float[] colorArray = ColorUtility.normalize(color);
            float r = colorArray[0], g = colorArray[1], b = colorArray[2], a = 1.0f;

            bufferBuilder.vertex(matrix, -halfWidth, -halfHeight, 0).color(r, g, b, a);
            bufferBuilder.vertex(matrix,  halfWidth, -halfHeight, 0).color(r, g, b, a);
            bufferBuilder.vertex(matrix,  halfWidth,  halfHeight, 0).color(r, g, b, a);
            bufferBuilder.vertex(matrix, -halfWidth,  halfHeight, 0).color(r, g, b, a);
            bufferBuilder.vertex(matrix, -halfWidth, -halfHeight, 0).color(r, g, b, a);
            BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());

            matrixStack.pop();
        } else {
            HitResult raycast = mc.cameraEntity.raycast(6.0, event.getPartialTicks(), false);
            BlockPos centerPos;
            if (raycast.getType() == HitResult.Type.BLOCK) {
                centerPos = ((BlockHitResult) raycast).getBlockPos().offset(((BlockHitResult) raycast).getSide());
            } else {
                Vec3d posInFront = mc.player.getCameraPosVec(event.getPartialTicks()).add(mc.player.getRotationVector().multiply(4));
                centerPos = BlockPos.ofFloored(posInFront);
            }

            float[] colorArray = ColorUtility.normalize(color);
            float r = colorArray[0], g = colorArray[1], b = colorArray[2], a = 1.0f;

            if (yaw >= -22.5f && yaw <= 22.5f) { // North
                renderStraightWall(matrixStack, centerPos, Direction.Axis.X, r, g, b, a);
            } else if (yaw > 22.5f && yaw <= 67.5f) { // North-East
                renderZigzagWall(matrixStack, centerPos, new Vec3i(-1, 0, -1), r, g, b, a);
            } else if (yaw > 67.5f && yaw <= 112.5f) { // East
                renderStraightWall(matrixStack, centerPos, Direction.Axis.Z, r, g, b, a);
            } else if (yaw > 112.5f && yaw <= 157.5f) { // South-East
                renderZigzagWall(matrixStack, centerPos, new Vec3i(1, 0, -1), r, g, b, a);
            } else if (yaw > 157.5f || yaw <= -157.5f) { // South
                renderStraightWall(matrixStack, centerPos, Direction.Axis.X, r, g, b, a);
            } else if (yaw > -157.5f && yaw <= -112.5f) { // South-West
                renderZigzagWall(matrixStack, centerPos, new Vec3i(1, 0, 1), r, g, b, a);
            } else if (yaw > -112.5f && yaw <= -67.5f) { // West
                renderStraightWall(matrixStack, centerPos, Direction.Axis.Z, r, g, b, a);
            } else if (yaw > -67.5f && yaw < -22.5f) { // North-West
                renderZigzagWall(matrixStack, centerPos, new Vec3i(-1, 0, 1), r, g, b, a);
            }
        }

        cleanupRenderState();
        matrixStack.pop();
    }

    private void renderStraightWall(MatrixStack matrixStack, BlockPos centerPos, Direction.Axis axis, float r, float g, float b, float a) {
        for (int i = -2; i <= 2; i++) {
            BlockPos currentPos = (axis == Direction.Axis.X) ? centerPos.add(i, 0, 0) : centerPos.add(0, 0, i);
            drawBoxOutline(matrixStack, currentPos.getX(), currentPos.getY(), currentPos.getZ(),
                    currentPos.getX() + 1.0f, currentPos.getY() + 4.0f, currentPos.getZ() + 1.0f,
                    r, g, b, a);
        }
    }

    private void renderZigzagWall(MatrixStack matrixStack, BlockPos centerPos, Vec3i step, float r, float g, float b, float a) {
        for (int i = -2; i <= 2; i++) {
            BlockPos currentPos = centerPos.add(step.multiply(i));
            drawBoxOutline(matrixStack, currentPos.getX(), currentPos.getY(), currentPos.getZ(),
                    currentPos.getX() + 1.0f, currentPos.getY() + 4.0f, currentPos.getZ() + 1.0f,
                    r, g, b, a);
        }
    }

    private void drawBoxOutline(MatrixStack matrices, float x1, float y1, float z1, float x2, float y2, float z2, float r, float g, float b, float a) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        buffer.vertex(matrix, x1, y1, z1).color(r, g, b, a); buffer.vertex(matrix, x2, y1, z1).color(r, g, b, a);
        buffer.vertex(matrix, x2, y1, z1).color(r, g, b, a); buffer.vertex(matrix, x2, y1, z2).color(r, g, b, a);
        buffer.vertex(matrix, x2, y1, z2).color(r, g, b, a); buffer.vertex(matrix, x1, y1, z2).color(r, g, b, a);
        buffer.vertex(matrix, x1, y1, z2).color(r, g, b, a); buffer.vertex(matrix, x1, y1, z1).color(r, g, b, a);

        buffer.vertex(matrix, x1, y2, z1).color(r, g, b, a); buffer.vertex(matrix, x2, y2, z1).color(r, g, b, a);
        buffer.vertex(matrix, x2, y2, z1).color(r, g, b, a); buffer.vertex(matrix, x2, y2, z2).color(r, g, b, a);
        buffer.vertex(matrix, x2, y2, z2).color(r, g, b, a); buffer.vertex(matrix, x1, y2, z2).color(r, g, b, a);
        buffer.vertex(matrix, x1, y2, z2).color(r, g, b, a); buffer.vertex(matrix, x1, y2, z1).color(r, g, b, a);

        buffer.vertex(matrix, x1, y1, z1).color(r, g, b, a); buffer.vertex(matrix, x1, y2, z1).color(r, g, b, a);
        buffer.vertex(matrix, x2, y1, z1).color(r, g, b, a); buffer.vertex(matrix, x2, y2, z1).color(r, g, b, a);
        buffer.vertex(matrix, x2, y1, z2).color(r, g, b, a); buffer.vertex(matrix, x2, y2, z2).color(r, g, b, a);
        buffer.vertex(matrix, x1, y1, z2).color(r, g, b, a); buffer.vertex(matrix, x1, y2, z2).color(r, g, b, a);

        BufferRenderer.drawWithGlobalProgram(buffer.end());
    }

    private void setupRenderState() {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.depthMask(false);
    }

    private void cleanupRenderState() {
        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }
}