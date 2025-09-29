package win.blade.common.utils.math;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class RenderUtility {

    private static final List<Line> lineQueue = new ArrayList<>();
    private static final List<Line> lineDepthQueue = new ArrayList<>();
    private static final List<Quad> quadQueue = new ArrayList<>();
    private static final List<Quad> quadDepthQueue = new ArrayList<>();
    private static final Tessellator tessellator = Tessellator.getInstance();

    private RenderUtility() {}

    public static void drawBox(MatrixStack matrices, Box box, int color, float width) {
        drawBox(matrices.peek(), box, color, width, true, true, false);
    }

    public static void drawBox(MatrixStack.Entry entry, Box box, int color, float width, boolean line, boolean fill, boolean depth) {
        double x1 = box.minX;
        double y1 = box.minY;
        double z1 = box.minZ;
        double x2 = box.maxX;
        double y2 = box.maxY;
        double z2 = box.maxZ;

        if (fill) {
            int fillColor = (color & 0x00FFFFFF) | ((int) (((color >> 24) & 0xFF) * 0.1f) << 24);
            drawQuad(entry, new Vec3d(x1, y1, z1), new Vec3d(x2, y1, z1), new Vec3d(x2, y1, z2), new Vec3d(x1, y1, z2), fillColor, depth);
            drawQuad(entry, new Vec3d(x1, y1, z1), new Vec3d(x1, y2, z1), new Vec3d(x2, y2, z1), new Vec3d(x2, y1, z1), fillColor, depth);
            drawQuad(entry, new Vec3d(x2, y1, z1), new Vec3d(x2, y2, z1), new Vec3d(x2, y2, z2), new Vec3d(x2, y1, z2), fillColor, depth);
            drawQuad(entry, new Vec3d(x1, y1, z2), new Vec3d(x2, y1, z2), new Vec3d(x2, y2, z2), new Vec3d(x1, y2, z2), fillColor, depth);
            drawQuad(entry, new Vec3d(x1, y1, z1), new Vec3d(x1, y1, z2), new Vec3d(x1, y2, z2), new Vec3d(x1, y2, z1), fillColor, depth);
            drawQuad(entry, new Vec3d(x1, y2, z1), new Vec3d(x1, y2, z2), new Vec3d(x2, y2, z2), new Vec3d(x2, y2, z1), fillColor, depth);
        }

        if (line) {
            drawLine(entry, new Vec3d(x1, y1, z1), new Vec3d(x2, y1, z1), color, width, depth);
            drawLine(entry, new Vec3d(x2, y1, z1), new Vec3d(x2, y1, z2), color, width, depth);
            drawLine(entry, new Vec3d(x2, y1, z2), new Vec3d(x1, y1, z2), color, width, depth);
            drawLine(entry, new Vec3d(x1, y1, z2), new Vec3d(x1, y1, z1), color, width, depth);
            drawLine(entry, new Vec3d(x1, y1, z2), new Vec3d(x1, y2, z2), color, width, depth);
            drawLine(entry, new Vec3d(x1, y1, z1), new Vec3d(x1, y2, z1), color, width, depth);
            drawLine(entry, new Vec3d(x2, y1, z2), new Vec3d(x2, y2, z2), color, width, depth);
            drawLine(entry, new Vec3d(x2, y1, z1), new Vec3d(x2, y2, z1), color, width, depth);
            drawLine(entry, new Vec3d(x1, y2, z1), new Vec3d(x2, y2, z1), color, width, depth);
            drawLine(entry, new Vec3d(x2, y2, z1), new Vec3d(x2, y2, z2), color, width, depth);
            drawLine(entry, new Vec3d(x2, y2, z2), new Vec3d(x1, y2, z2), color, width, depth);
            drawLine(entry, new Vec3d(x1, y2, z2), new Vec3d(x1, y2, z1), color, width, depth);
        }
    }

    public static void drawLine(MatrixStack.Entry entry, Vec3d start, Vec3d end, int color, float width, boolean depth) {
        Line line = new Line(entry, start, end, color, color, width);
        if (depth) lineDepthQueue.add(line);
        else lineQueue.add(line);
    }

    public static void renderQueues() {
        if (!lineQueue.isEmpty()) {
            GL11.glEnable(GL11.GL_POLYGON_SMOOTH);
            Set<Float> widths = lineQueue.stream().map(line -> line.width).collect(Collectors.toCollection(LinkedHashSet::new));
            RenderSystem.enableBlend();
            RenderSystem.disableCull();
            RenderSystem.disableDepthTest();
            RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_CONSTANT_ALPHA);
            RenderSystem.setShader(ShaderProgramKeys.RENDERTYPE_LINES);
            widths.forEach(width -> {
                RenderSystem.lineWidth(width);
                BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);
                lineQueue.stream().filter(line -> line.width == width).forEach(line -> vertexLine(line.entry, buffer, line.start.toVector3f(), line.end.toVector3f(), line.colorStart, line.colorEnd));
                BufferRenderer.drawWithGlobalProgram(buffer.end());
            });
            RenderSystem.enableDepthTest();
            RenderSystem.enableCull();
            RenderSystem.disableBlend();
            lineQueue.clear();
            GL11.glDisable(GL11.GL_POLYGON_SMOOTH);
        }

        if (!quadQueue.isEmpty()) {
            RenderSystem.enableBlend();
            RenderSystem.disableCull();
            RenderSystem.disableDepthTest();
            RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_CONSTANT_ALPHA);
            RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
            BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
            quadQueue.forEach(quad -> vertexQuad(quad.entry, buffer, quad.x.toVector3f(), quad.y.toVector3f(), quad.w.toVector3f(), quad.z.toVector3f(), quad.color));
            BufferRenderer.drawWithGlobalProgram(buffer.end());
            RenderSystem.enableDepthTest();
            RenderSystem.enableCull();
            RenderSystem.disableBlend();
            quadQueue.clear();
        }

        if (!lineDepthQueue.isEmpty()) {
            GL11.glEnable(GL11.GL_POLYGON_SMOOTH);
            Set<Float> widths = lineDepthQueue.stream().map(line -> line.width).collect(Collectors.toCollection(LinkedHashSet::new));
            RenderSystem.enableBlend();
            RenderSystem.disableCull();
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_CONSTANT_ALPHA);
            RenderSystem.setShader(ShaderProgramKeys.RENDERTYPE_LINES);
            widths.forEach(width -> {
                RenderSystem.lineWidth(width);
                BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);
                lineDepthQueue.stream().filter(line -> line.width == width).forEach(line -> vertexLine(line.entry, buffer, line.start.toVector3f(), line.end.toVector3f(), line.colorStart, line.colorEnd));
                BufferRenderer.drawWithGlobalProgram(buffer.end());
            });
            RenderSystem.depthMask(true);
            RenderSystem.enableCull();
            RenderSystem.disableBlend();
            lineDepthQueue.clear();
            GL11.glDisable(GL11.GL_POLYGON_SMOOTH);
        }

        if (!quadDepthQueue.isEmpty()) {
            RenderSystem.enableBlend();
            RenderSystem.disableCull();
            RenderSystem.enableDepthTest();
            RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_CONSTANT_ALPHA);
            RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
            BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
            quadDepthQueue.forEach(quad -> vertexQuad(quad.entry, buffer, quad.x.toVector3f(), quad.y.toVector3f(), quad.w.toVector3f(), quad.z.toVector3f(), quad.color));
            BufferRenderer.drawWithGlobalProgram(buffer.end());
            RenderSystem.enableCull();
            RenderSystem.disableBlend();
            quadDepthQueue.clear();
        }
    }

    private static void vertexLine(MatrixStack.Entry entry, VertexConsumer buffer, Vector3f start, Vector3f end, int startColor, int endColor) {
        Matrix4f positionMatrix = entry.getPositionMatrix();
        Vector3f normal = getNormal(start, end);
        buffer.vertex(positionMatrix, start.x, start.y, start.z).color(startColor).normal(entry, normal.x, normal.y, normal.z);
        buffer.vertex(positionMatrix, end.x, end.y, end.z).color(endColor).normal(entry, normal.x, normal.y, normal.z);
    }

    private static void vertexQuad(MatrixStack.Entry entry, VertexConsumer buffer, Vector3f vec1, Vector3f vec2, Vector3f vec3, Vector3f vec4, int color) {
        Matrix4f positionMatrix = entry.getPositionMatrix();
        buffer.vertex(positionMatrix, vec1.x, vec1.y, vec1.z).color(color);
        buffer.vertex(positionMatrix, vec2.x, vec2.y, vec2.z).color(color);
        buffer.vertex(positionMatrix, vec3.x, vec3.y, vec3.z).color(color);
        buffer.vertex(positionMatrix, vec4.x, vec4.y, vec4.z).color(color);
    }

    private static Vector3f getNormal(Vector3f start, Vector3f end) {
        Vector3f normal = new Vector3f(start).sub(end);
        float sqrt = MathHelper.sqrt(normal.lengthSquared());
        if (sqrt == 0) return new Vector3f(0, 0, 0);
        return normal.div(sqrt);
    }

    private static void drawQuad(MatrixStack.Entry entry, Vec3d x, Vec3d y, Vec3d w, Vec3d z, int color, boolean depth) {
        Quad quad = new Quad(entry, x, y, w, z, color);
        if (depth) quadDepthQueue.add(quad);
        else quadQueue.add(quad);
    }

    private record Line(MatrixStack.Entry entry, Vec3d start, Vec3d end, int colorStart, int colorEnd, float width) {}
    private record Quad(MatrixStack.Entry entry, Vec3d x, Vec3d y, Vec3d w, Vec3d z, int color) {}
}