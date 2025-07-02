package win.blade.common.utils.render;


import win.blade.common.utils.minecraft.MinecraftInstance;

import static org.lwjgl.opengl.EXTFramebufferObject.*;
import static org.lwjgl.opengl.EXTFramebufferObject.GL_RENDERBUFFER_EXT;
import static org.lwjgl.opengl.EXTPackedDepthStencil.GL_DEPTH_STENCIL_EXT;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;

import static org.lwjgl.opengl.GL11.GL_STENCIL_TEST;

/**
 * Автор Ieo117
 * Дата создания: 23.06.2025, в 13:21:34
 */

public class Stencil implements MinecraftInstance {

    public static void push() {
        var framebuffer = mc.getFramebuffer();
        if (framebuffer.depthAttachment > -1) {
            mc.getFramebuffer().beginWrite(false);

            glDeleteRenderbuffersEXT(framebuffer.depthAttachment);
            final int stencilDepthBufferID = glGenRenderbuffersEXT();
            glBindRenderbufferEXT(GL_RENDERBUFFER_EXT, stencilDepthBufferID);
            glRenderbufferStorageEXT(GL_RENDERBUFFER_EXT, GL_DEPTH_STENCIL_EXT, mc.getWindow().getWidth(), mc.getWindow().getHeight());
            glFramebufferRenderbufferEXT(GL_FRAMEBUFFER_EXT, GL_STENCIL_ATTACHMENT_EXT, GL_RENDERBUFFER_EXT, stencilDepthBufferID);
            glFramebufferRenderbufferEXT(GL_FRAMEBUFFER_EXT, GL_DEPTH_ATTACHMENT_EXT, GL_RENDERBUFFER_EXT, stencilDepthBufferID);
            framebuffer.depthAttachment = -1;
        }

        glStencilMask(0xFF);
        glClear(GL_STENCIL_BUFFER_BIT);
        glEnable(GL_STENCIL_TEST);
        glStencilFunc(GL_ALWAYS, 1, 1);
        glStencilOp(GL_REPLACE, GL_REPLACE, GL_REPLACE);
        glDisable(GL_DEPTH_TEST);
        glColorMask(false, false, false, false);
    }

    public static void read(int ref) {
        glColorMask(true, true, true, true);
        glStencilFunc(GL_EQUAL, ref, 1);
        glStencilOp(GL_KEEP, GL_KEEP, GL_KEEP);
    }

    public static void pop() {
        glDisable(GL_STENCIL_TEST);
        glEnable(GL_DEPTH_TEST);
    }

}
//
//    public static void erase(int ref) {
//        glColorMask(true, true, true, true);
//        glStencilFunc(GL_NOTEQUAL, ref, 1);
//        glStencilOp(GL_KEEP, GL_KEEP, GL_KEEP);
//    }
