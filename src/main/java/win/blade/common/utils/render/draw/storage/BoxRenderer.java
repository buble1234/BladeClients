package win.blade.common.utils.render.draw.storage;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import win.blade.common.utils.render.draw.RendererUtility;

import java.awt.Color;

/**
 * Автор: NoCap
 * Дата создания: 30.07.2025
 */
public class BoxRenderer {

    public void drawFilled(MatrixStack matrices, Box box, Color color) {
        Box relativeBox = box.offset(-RendererUtility.INSTANCE.cameraPos().x, -RendererUtility.INSTANCE.cameraPos().y, -RendererUtility.INSTANCE.cameraPos().z);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        RendererUtility.INSTANCE.setupRender();
        RenderSystem.disableDepthTest();

        addVertices(buffer, matrices.peek().getPositionMatrix(), relativeBox, color);

        BufferRenderer.drawWithGlobalProgram(buffer.end());

        RenderSystem.enableDepthTest();
        RendererUtility.INSTANCE.resetRender();
    }

    public void drawFilledOutline(MatrixStack matrices, Box box, Color color, float thickness) {
        Color fillColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha() / 3);
        RendererUtility.INSTANCE.BOXES.drawFilled(matrices, box, fillColor);

        Box relativeBox = box.offset(-RendererUtility.INSTANCE.cameraPos().x, -RendererUtility.INSTANCE.cameraPos().y, -RendererUtility.INSTANCE.cameraPos().z);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        RendererUtility.INSTANCE.setupRender();
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        addVertices(buffer, matrix, relativeBox, color, thickness);

        BufferRenderer.drawWithGlobalProgram(buffer.end());
        RendererUtility.INSTANCE.resetRender();
    }

    private void addVertices(BufferBuilder buffer, Matrix4f matrix, Box box, Color color, float thickness) {
        LineRenderer lineRenderer = RendererUtility.INSTANCE.LINES;

        Vec3d v0 = new Vec3d(box.minX, box.minY, box.minZ); Vec3d v1 = new Vec3d(box.maxX, box.minY, box.minZ);
        Vec3d v2 = new Vec3d(box.maxX, box.minY, box.maxZ); Vec3d v3 = new Vec3d(box.minX, box.minY, box.maxZ);
        Vec3d v4 = new Vec3d(box.minX, box.maxY, box.minZ); Vec3d v5 = new Vec3d(box.maxX, box.maxY, box.minZ);
        Vec3d v6 = new Vec3d(box.maxX, box.maxY, box.maxZ); Vec3d v7 = new Vec3d(box.minX, box.maxY, box.maxZ);

        lineRenderer.addVertices(buffer, matrix, v0, v1, color, thickness); lineRenderer.addVertices(buffer, matrix, v1, v2, color, thickness);
        lineRenderer.addVertices(buffer, matrix, v2, v3, color, thickness); lineRenderer.addVertices(buffer, matrix, v3, v0, color, thickness);
        lineRenderer.addVertices(buffer, matrix, v4, v5, color, thickness); lineRenderer.addVertices(buffer, matrix, v5, v6, color, thickness);
        lineRenderer.addVertices(buffer, matrix, v6, v7, color, thickness); lineRenderer.addVertices(buffer, matrix, v7, v4, color, thickness);
        lineRenderer.addVertices(buffer, matrix, v0, v4, color, thickness); lineRenderer.addVertices(buffer, matrix, v1, v5, color, thickness);
        lineRenderer.addVertices(buffer, matrix, v2, v6, color, thickness); lineRenderer.addVertices(buffer, matrix, v3, v7, color, thickness);
    }

    private void addVertices(BufferBuilder buffer, Matrix4f matrix, Box box, Color color) {
        float r = color.getRed() / 255.0f;
        float g = color.getGreen() / 255.0f;
        float b = color.getBlue() / 255.0f;
        float a = color.getAlpha() / 255.0f;

        float minX = (float) box.minX; float minY = (float) box.minY; float minZ = (float) box.minZ;
        float maxX = (float) box.maxX; float maxY = (float) box.maxY; float maxZ = (float) box.maxZ;

        buffer.vertex(matrix, minX, minY, minZ).color(r, g, b, a); buffer.vertex(matrix, maxX, minY, minZ).color(r, g, b, a); buffer.vertex(matrix, maxX, minY, maxZ).color(r, g, b, a); buffer.vertex(matrix, minX, minY, maxZ).color(r, g, b, a);
        buffer.vertex(matrix, minX, maxY, minZ).color(r, g, b, a); buffer.vertex(matrix, minX, maxY, maxZ).color(r, g, b, a); buffer.vertex(matrix, maxX, maxY, maxZ).color(r, g, b, a); buffer.vertex(matrix, maxX, maxY, minZ).color(r, g, b, a);
        buffer.vertex(matrix, minX, minY, minZ).color(r, g, b, a); buffer.vertex(matrix, minX, maxY, minZ).color(r, g, b, a); buffer.vertex(matrix, maxX, maxY, minZ).color(r, g, b, a); buffer.vertex(matrix, maxX, minY, minZ).color(r, g, b, a);
        buffer.vertex(matrix, minX, minY, maxZ).color(r, g, b, a); buffer.vertex(matrix, maxX, minY, maxZ).color(r, g, b, a); buffer.vertex(matrix, maxX, maxY, maxZ).color(r, g, b, a); buffer.vertex(matrix, minX, maxY, maxZ).color(r, g, b, a);
        buffer.vertex(matrix, minX, minY, minZ).color(r, g, b, a); buffer.vertex(matrix, minX, minY, maxZ).color(r, g, b, a); buffer.vertex(matrix, minX, maxY, maxZ).color(r, g, b, a); buffer.vertex(matrix, minX, maxY, minZ).color(r, g, b, a);
        buffer.vertex(matrix, maxX, minY, minZ).color(r, g, b, a); buffer.vertex(matrix, maxX, maxY, minZ).color(r, g, b, a); buffer.vertex(matrix, maxX, maxY, maxZ).color(r, g, b, a); buffer.vertex(matrix, maxX, minY, maxZ).color(r, g, b, a);
    }
}