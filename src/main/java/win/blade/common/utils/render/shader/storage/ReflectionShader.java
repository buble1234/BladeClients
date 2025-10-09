package win.blade.common.utils.render.shader.storage;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.render.*;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import win.blade.common.utils.minecraft.MinecraftInstance;
import win.blade.common.utils.render.shader.Shader;
import win.blade.common.utils.render.shader.ShaderHelper;

import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;

public class ReflectionShader extends Shader implements MinecraftInstance {

    public ReflectionShader() {
        super("effects","reflection");
    }

    public static void startTorusRender(boolean useDepth) {
        ShaderHelper.initShadersIfNeeded();
        if (!ShaderHelper.isInitialized()) return;

        ShaderHelper.checkFramebuffers();
        SimpleFramebuffer torusFbo = ShaderHelper.getReflectionFbo();
        SimpleFramebuffer copyFbo = ShaderHelper.getCopyFbo();

        Framebuffer mainFbo = mc.getFramebuffer();
        GlStateManager._glBindFramebuffer(GL_READ_FRAMEBUFFER, mainFbo.fbo);
        GlStateManager._glBindFramebuffer(GL_DRAW_FRAMEBUFFER, copyFbo.fbo);
        GlStateManager._glBlitFrameBuffer(
                0, 0, mainFbo.textureWidth, mainFbo.textureHeight,
                0, 0, copyFbo.textureWidth, copyFbo.textureHeight,
                GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT, GL_NEAREST
        );

        torusFbo.clear();
        if (useDepth) {
            torusFbo.copyDepthFrom(mc.getFramebuffer());
            RenderSystem.enableDepthTest();
        } else {
            RenderSystem.disableDepthTest();
        }
        torusFbo.beginWrite(false);
    }

    public static void renderTorus(Matrix4f modelViewMat, Matrix4f projMat, float frequency, float outerRad, float innerRad) {
        if (!ShaderHelper.isInitialized()) return;

        ReflectionShader shader = ShaderHelper.getReflectionShader();
        SimpleFramebuffer copyFbo = ShaderHelper.getCopyFbo();

        shader.bind();
        shader.setUniformMatrix4f("ModelViewMat", false, modelViewMat);
        shader.setUniformMatrix4f("ProjMat", false, projMat);
        shader.setUniform1i("Tex0", 0);
        shader.setUniform1f("Freq", frequency);
        if (mc.gameRenderer.getCamera() != null) {
            Vec3d camPos = mc.gameRenderer.getCamera().getPos();
            shader.setUniform3f("CamPos", new Vector3f((float)camPos.x, (float)camPos.y, (float)camPos.z));
        }

        RenderSystem.activeTexture(GL_TEXTURE0);
        RenderSystem.bindTexture(copyFbo.getColorAttachment());

        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.LINES);
        int slices = 10;
        int loops = 30;

        Vector3f normal1 = new Vector3f(), normal2 = new Vector3f(), normal3 = new Vector3f(), normal4 = new Vector3f();

        for (int i = 0; i < slices; i++) {
            double theta = 2 * Math.PI * i / slices;
            double nextTheta = 2 * Math.PI * (i + 1) / slices;

            for (int j = 0; j < loops; j++) {
                double phi = 2 * Math.PI * j / loops;
                double nextPhi = 2 * Math.PI * (j + 1) / loops;

                float x1 = (float) ((outerRad + innerRad * cos(theta)) * cos(phi));
                float y1 = (float) ((outerRad + innerRad * cos(theta)) * sin(phi));
                float z1 = (float) (innerRad * sin(theta));

                float x2 = (float) ((outerRad + innerRad * cos(nextTheta)) * cos(phi));
                float y2 = (float) ((outerRad + innerRad * cos(nextTheta)) * sin(phi));
                float z2 = (float) (innerRad * sin(nextTheta));

                float x3 = (float) ((outerRad + innerRad * cos(nextTheta)) * cos(nextPhi));
                float y3 = (float) ((outerRad + innerRad * cos(nextTheta)) * sin(nextPhi));
                float z3 = (float) (innerRad * sin(nextTheta));

                float x4 = (float) ((outerRad + innerRad * cos(theta)) * cos(nextPhi));
                float y4 = (float) ((outerRad + innerRad * cos(theta)) * sin(nextPhi));
                float z4 = (float) (innerRad * sin(theta));

                normal1.set((float) (cos(theta) * cos(phi)), (float) (cos(theta) * sin(phi)), (float) sin(theta)).normalize();
                normal2.set((float) (cos(nextTheta) * cos(phi)), (float) (cos(nextTheta) * sin(phi)), (float) sin(nextTheta)).normalize();
                normal3.set((float) (cos(nextTheta) * cos(nextPhi)), (float) (cos(nextTheta) * sin(nextPhi)), (float) sin(nextTheta)).normalize();
                normal4.set((float) (cos(theta) * cos(nextPhi)), (float) (cos(theta) * sin(nextPhi)), (float) sin(theta)).normalize();

                bufferBuilder.vertex(x1, y1, z1).color(0,0,0,0).normal(normal1.x(), normal1.y(), normal1.z());
                bufferBuilder.vertex(x2, y2, z2).color(0,0,0,0).normal(normal2.x(), normal2.y(), normal2.z());
                bufferBuilder.vertex(x3, y3, z3).color(0,0,0,0).normal(normal3.x(), normal3.y(), normal3.z());
                bufferBuilder.vertex(x4, y4, z4).color(0,0,0,0).normal(normal4.x(), normal4.y(), normal4.z());
            }
        }
        BufferRenderer.draw(bufferBuilder.end());
        shader.unbind();
    }

    public static void endTorusRender(boolean useDepth) {
        SimpleFramebuffer torusFbo = ShaderHelper.getReflectionFbo();
        Shader passThroughShader = ShaderHelper.getPassThroughShader();

        mc.getFramebuffer().beginWrite(false);

        RenderSystem.depthMask(false);
        RenderSystem.disableDepthTest();

        passThroughShader.bind();
        passThroughShader.setUniform1i("Tex0", 0);
        passThroughShader.setUniformBool("Alpha", true);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.bindTexture(torusFbo.getColorAttachment());
        ShaderHelper.drawFullScreenQuad();
        RenderSystem.disableBlend();

        passThroughShader.unbind();

        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
    }

    public void setupHandUniforms(float opacity, float refraction) {
        bind();
        setUniformBool("is2D", true);
        setUniform1i("Tex0", 0);
        setUniform1i("normalMap", 1);
        setUniform1f("opacity", opacity);
        setUniform1f("refractionStrength", refraction);
    }
}