package win.blade.common.utils.render.draw;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.util.math.Vec3d;
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
}