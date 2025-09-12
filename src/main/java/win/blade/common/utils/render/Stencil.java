package win.blade.common.utils.render;

import win.blade.common.utils.minecraft.MinecraftInstance;

import static org.lwjgl.opengl.EXTFramebufferObject.*;
import static org.lwjgl.opengl.EXTPackedDepthStencil.GL_DEPTH_STENCIL_EXT;
import static org.lwjgl.opengl.GL11.*;

/**
 * Автор NoCap (фулл переписал)
 */
public class Stencil implements MinecraftInstance {

    private static int originalDepthAttachment = -1;
    private static boolean stencilInitialized = false;
    private static int stencilDepthBufferID = -1;

    public static void push() {
        var framebuffer = mc.getFramebuffer();

        if (!stencilInitialized && framebuffer.depthAttachment > -1) {
            originalDepthAttachment = framebuffer.depthAttachment;

            stencilDepthBufferID = glGenRenderbuffersEXT();
            glBindRenderbufferEXT(GL_RENDERBUFFER_EXT, stencilDepthBufferID);
            glRenderbufferStorageEXT(GL_RENDERBUFFER_EXT, GL_DEPTH_STENCIL_EXT,
                    mc.getWindow().getWidth(), mc.getWindow().getHeight());

            glFramebufferRenderbufferEXT(GL_FRAMEBUFFER_EXT, GL_STENCIL_ATTACHMENT_EXT,
                    GL_RENDERBUFFER_EXT, stencilDepthBufferID);
            glFramebufferRenderbufferEXT(GL_FRAMEBUFFER_EXT, GL_DEPTH_ATTACHMENT_EXT,
                    GL_RENDERBUFFER_EXT, stencilDepthBufferID);

            stencilInitialized = true;
        }

        glStencilMask(0xFF);
        glClear(GL_STENCIL_BUFFER_BIT);
        glEnable(GL_STENCIL_TEST);
        glStencilFunc(GL_ALWAYS, 1, 0xFF);
        glStencilOp(GL_REPLACE, GL_REPLACE, GL_REPLACE);
        glDisable(GL_DEPTH_TEST);
        glColorMask(false, false, false, false);
    }

    public static void read(int ref) {
        glColorMask(true, true, true, true);
        glStencilFunc(GL_EQUAL, ref, 0xFF);
        glStencilOp(GL_KEEP, GL_KEEP, GL_KEEP);
    }

    public static void pop() {
        glDisable(GL_STENCIL_TEST);
        glEnable(GL_DEPTH_TEST);
        glColorMask(true, true, true, true);
        glStencilMask(0xFF);

        restoreFramebuffer();
    }

    public static void erase(int ref) {
        glColorMask(true, true, true, true);
        glStencilFunc(GL_NOTEQUAL, ref, 0xFF);
        glStencilOp(GL_KEEP, GL_KEEP, GL_KEEP);
    }

    private static void restoreFramebuffer() {
    }

    public static void cleanup() {
        if (stencilDepthBufferID > -1) {
            glDeleteRenderbuffersEXT(stencilDepthBufferID);
            stencilDepthBufferID = -1;
        }
        stencilInitialized = false;
        originalDepthAttachment = -1;
    }
}