package win.blade.common.utils.browser;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;

/**
 * Автор: NoCap
 * Дата создания: 27.07.2025
 */
public class BrowserRenderer {

    public static void renderBrowser(DrawContext context, BrowserTab tab, int x, int y, int width, int height) {
        if (tab == null || !tab.isVisible()) {
            return;
        }

        tab.sendExternalBeginFrame();

        Identifier textureIdentifier = tab.getTextureIdentifier();

        if (textureIdentifier == null) {
            return;
        }
        
        tab.drawn = false;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX);
        RenderSystem.setShaderTexture(0, textureIdentifier);

        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();
        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);

        bufferBuilder.vertex(matrix, x, y + height, 0).texture(0.0f, 1.0f);
        bufferBuilder.vertex(matrix, x + width, y + height, 0).texture(1.0f, 1.0f);
        bufferBuilder.vertex(matrix, x + width, y, 0).texture(1.0f, 0.0f);
        bufferBuilder.vertex(matrix, x, y, 0).texture(0.0f, 0.0f);
        
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        
        RenderSystem.disableBlend();
        
        tab.drawn = true;
    }
}