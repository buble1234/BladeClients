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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import win.blade.common.gui.impl.gui.setting.implement.BooleanSetting;
import win.blade.common.gui.impl.gui.setting.implement.GroupSetting;
import win.blade.common.utils.color.ColorUtility;
import win.blade.core.event.controllers.EventHandler;
import win.blade.core.event.impl.render.RenderEvents;
import win.blade.core.module.api.Category;
import win.blade.core.module.api.Module;
import win.blade.core.module.api.ModuleInfo;

import java.awt.*;

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

            Vec3d playerPos = mc.player.getPos();
            Vec3d centerPos = playerPos.add(0, -1.4, 0);

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
                return;
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
        if (mc.player == null || mc.world == null) return;

        MatrixStack matrixStack = event.getMatrixStack();
        Vec3d cameraPos = mc.gameRenderer.getCamera().getPos();

        matrixStack.push();
        matrixStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        Vec3d playerPos = mc.player.getPos();
        Vec3d start = playerPos.add(0.0, mc.player.getStandingEyeHeight(), 0.0);
        Vec3d lookVec = mc.player.getRotationVector();
        Vec3d end = start.add(lookVec.x * 4.0, lookVec.y * 4.0, lookVec.z * 4.0);

        RaycastContext context = new RaycastContext(start, end, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player);
        BlockHitResult rayResult = mc.world.raycast(context);

        float pitch = mc.player.getPitch();
        boolean isLookingDown = pitch > 45.0f;
        boolean isLookingUp = pitch < -45.0f;
        boolean isLookingHorizontal = !isLookingDown && !isLookingUp;

        Vec3d planePos;
        if (rayResult.getType() == HitResult.Type.BLOCK) {
            BlockPos hitBlock = rayResult.getBlockPos();
            Vec3d hitPos = rayResult.getPos();

            if (isLookingDown) {
                planePos = new Vec3d(
                        Math.floor(hitPos.x) + 0.5,
                        Math.floor(hitPos.y + 1.0) - 1.8 + 0.75,
                        Math.floor(hitPos.z) + 0.5
                );
            } else if (isLookingUp) {
                planePos = new Vec3d(
                        Math.floor(hitPos.x) + 0.5,
                        Math.floor(hitPos.y) - 0.75 + 1.6,
                        Math.floor(hitPos.z) + 0.5
                );
            } else {
                double offsetX = rayResult.getSide().getOffsetX() != 0 ? rayResult.getSide().getOffsetX() * 0.75 : 0;
                double offsetZ = rayResult.getSide().getOffsetZ() != 0 ? rayResult.getSide().getOffsetZ() * 0.75 : 0;
                planePos = new Vec3d(
                        Math.floor(hitPos.x) + 0.5 + offsetX,
                        Math.floor(hitPos.y) + 0.5 + 1.6,
                        Math.floor(hitPos.z) + 0.5 + offsetZ
                );
            }
        } else {
            double distance = 4.0;
            double dx = lookVec.x * distance;
            double dy = lookVec.y * distance;
            double dz = lookVec.z * distance;
            planePos = start.add(dx, dy, dz);
            double adjustedY = Math.floor(planePos.y) + (isLookingDown ? -1.8 + 0.75 : isLookingUp ? -0.75 + 1.6 : 0.5 + 1.6);
            planePos = new Vec3d(Math.floor(planePos.x) + 0.5, adjustedY, Math.floor(planePos.z) + 0.5);
        }

        setupRenderState();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);

        matrixStack.push();
        matrixStack.translate(planePos.x, planePos.y, planePos.z);

        if (isLookingHorizontal) {
            matrixStack.multiply(new Quaternionf().fromAxisAngleDeg(0, 1, 0, -mc.player.getYaw()));
        }

        float width = 4.0f;
        float height = 4.0f;
        float thickness = 1.5f;
        float halfWidth = width / 2.0f;
        float halfHeight = height / 2.0f;
        float halfThickness = thickness / 2.0f;

        float[][] vertices;
        if (isLookingHorizontal) {
            vertices = new float[][] {
                    {-halfWidth, -halfHeight, -halfThickness},
                    { halfWidth, -halfHeight, -halfThickness},
                    { halfWidth,  halfHeight, -halfThickness},
                    {-halfWidth,  halfHeight, -halfThickness},
                    {-halfWidth, -halfHeight,  halfThickness},
                    { halfWidth, -halfHeight,  halfThickness},
                    { halfWidth,  halfHeight,  halfThickness},
                    {-halfWidth,  halfHeight,  halfThickness}
            };
        } else {
            vertices = new float[][] {
                    {-halfWidth, -halfThickness, -halfHeight},
                    { halfWidth, -halfThickness, -halfHeight},
                    { halfWidth,  halfThickness, -halfHeight},
                    {-halfWidth,  halfThickness, -halfHeight},
                    {-halfWidth, -halfThickness,  halfHeight},
                    { halfWidth, -halfThickness,  halfHeight},
                    { halfWidth,  halfThickness,  halfHeight},
                    {-halfWidth,  halfThickness,  halfHeight}
            };
        }

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

        matrixStack.pop();
        cleanupRenderState();
        matrixStack.pop();
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