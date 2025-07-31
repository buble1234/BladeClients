package win.blade.common.utils.render.draw;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import win.blade.common.utils.color.ColorUtility;
import win.blade.common.utils.minecraft.MinecraftInstance;
import win.blade.common.utils.render.draw.storage.*;

/**
 * Автор: NoCap
 * Дата создания: 30.07.2025
 */
public class RendererUtility implements MinecraftInstance {

    public static final RendererUtility INSTANCE = new RendererUtility();

    public final BoxRenderer BOXES = new BoxRenderer();
    public final LineRenderer LINES = new LineRenderer();
    public final TesseractRenderer TESSERACT = new TesseractRenderer();

    private RendererUtility() {
    }

    public void setupRender() {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
    }

    public void resetRender() {
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    public Vec3d cameraPos() {
        return mc.gameRenderer.getCamera().getPos();
    }

    public static void drawTexturedQuad(MatrixStack matrixStack, Identifier texture, float x, float y, float width, float height, int color) {
        Matrix4f matrix = matrixStack.peek().getPositionMatrix();
        float[] c = ColorUtility.normalize(color);

        RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
        RenderSystem.setShaderTexture(0, texture);

        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        bufferBuilder.vertex(matrix, x, y, 0).texture(0, 0).color(c[0], c[1], c[2], c[3]);
        bufferBuilder.vertex(matrix, x, y + height, 0).texture(0, 1).color(c[0], c[1], c[2], c[3]);
        bufferBuilder.vertex(matrix, x + width, y + height, 0).texture(1, 1).color(c[0], c[1], c[2], c[3]);
        bufferBuilder.vertex(matrix, x + width, y, 0).texture(1, 0).color(c[0], c[1], c[2], c[3]);
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
    }
}