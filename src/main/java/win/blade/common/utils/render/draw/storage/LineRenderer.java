package win.blade.common.utils.render.draw.storage;

import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import win.blade.common.utils.render.draw.RendererUtility;

import java.awt.Color;

/**
 * Автор: NoCap
 * Дата создания: 30.07.2025
 * (ОТРЕДАКТИРОВАНО ДЛЯ ИСПРАВЛЕНИЯ ОШИБОК)
 */
public class LineRenderer {

    public void draw(MatrixStack matrices, Vec3d startWorld, Vec3d endWorld, Color color, float thickness) {
        Vec3d startRelative = startWorld.subtract(RendererUtility.INSTANCE.cameraPos());
        Vec3d endRelative = endWorld.subtract(RendererUtility.INSTANCE.cameraPos());

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        RendererUtility.INSTANCE.setupRender();

        addVertices(buffer, matrices.peek().getPositionMatrix(), startRelative, endRelative, color, thickness);

        BufferRenderer.drawWithGlobalProgram(buffer.end());

        RendererUtility.INSTANCE.resetRender();
    }


    public void addVertices(BufferBuilder buffer, Matrix4f matrix, Vec3d start, Vec3d end, Color color, float thickness) {
        float r = color.getRed() / 255.0f;
        float g = color.getGreen() / 255.0f;
        float b = color.getBlue() / 255.0f;
        // ИСПРАВЛЕНО: Теперь используется альфа-канал из объекта Color
        float a = color.getAlpha() / 255.0f;

        Vec3d lineDir = end.subtract(start);
        if (lineDir.lengthSquared() == 0) return;

        // Вектор от камеры (в относительных координатах это 0,0,0) до середины линии
        Vec3d camToMidpoint = start.add(end).multiply(0.5);

        // Находим вектор, перпендикулярный и линии, и направлению взгляда. Это дает "ширину" линии.
        Vec3d sideDir = lineDir.crossProduct(camToMidpoint).normalize();

        // Защита от случая, когда камера смотрит ровно вдоль линии.
        // В этом случае crossProduct равен нулю, и линия "схлопывается".
        if (sideDir.lengthSquared() == 0) {
            // Просто берем любой другой перпендикулярный вектор
            sideDir = lineDir.crossProduct(new Vec3d(0, 1, 0)).normalize();
            if(sideDir.lengthSquared() == 0) {
                sideDir = lineDir.crossProduct(new Vec3d(1, 0, 0)).normalize();
            }
        }

        Vec3d offset = sideDir.multiply(thickness / 2.0);

        Vec3d v1 = start.add(offset);
        Vec3d v2 = start.subtract(offset);
        Vec3d v3 = end.subtract(offset);
        Vec3d v4 = end.add(offset);

        buffer.vertex(matrix, (float)v1.x, (float)v1.y, (float)v1.z).color(r, g, b, a);
        buffer.vertex(matrix, (float)v2.x, (float)v2.y, (float)v2.z).color(r, g, b, a);
        buffer.vertex(matrix, (float)v3.x, (float)v3.y, (float)v3.z).color(r, g, b, a);
        buffer.vertex(matrix, (float)v4.x, (float)v4.y, (float)v4.z).color(r, g, b, a);
    }
}