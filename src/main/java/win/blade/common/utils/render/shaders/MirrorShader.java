package win.blade.common.utils.render.shaders;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.*;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.Window;
import net.minecraft.resource.ResourceFactory;
import org.lwjgl.opengl.GL30;
import win.blade.common.utils.resource.ResourceUtility;
import java.awt.Color;

public class MirrorShader {
    
    private static final ShaderProgramKey MIRROR_SHADER_KEY = new ShaderProgramKey(
        ResourceUtility.getShaderIdentifier("effects", "mirror"),
        VertexFormats.POSITION, Defines.EMPTY
    );
    
    private GlUniform sizeUniform;
    private GlUniform radiusUniform;
    private GlUniform smoothnessUniform;
    private GlUniform inputResolutionUniform;
    private GlUniform brightnessUniform;
    private GlUniform color1Uniform;
    private GlUniform reflectionIntensityUniform;
    private GlUniform distortionStrengthUniform;
    private GlUniform timeUniform;
    private GlUniform cameraPosUniform;
    
    private Window window;
    private SimpleFramebuffer input;
    private long startTime;
    private boolean initialized = false;
    
    public MirrorShader() {
        this.startTime = System.nanoTime();
        this.window = MinecraftClient.getInstance().getWindow();
        this.input = new SimpleFramebuffer(
            this.window.getFramebufferWidth(),
            this.window.getFramebufferHeight(), 
            true
        );
    }
    
    public void setParameters(float width, float height, float radius, float smoothness, float brightness, float reflectionIntensity, float distortionStrength, Color color) {
        if (!initialized) setUp();
        
        setupBuffer();
        bind();
        
        float scale = (float) window.getScaleFactor();
        
        if (sizeUniform != null)
            sizeUniform.set(width * scale, height * scale);
            
        if (radiusUniform != null)
            radiusUniform.set(radius * scale, radius * scale, radius * scale, radius * scale);
            
        if (smoothnessUniform != null)
            smoothnessUniform.set(smoothness * scale);
            
        if (brightnessUniform != null)
            brightnessUniform.set(brightness);
            
        if (reflectionIntensityUniform != null)
            reflectionIntensityUniform.set(reflectionIntensity);
            
        if (distortionStrengthUniform != null)
            distortionStrengthUniform.set(distortionStrength);
            
        if (timeUniform != null) {
            float time = (System.nanoTime() - startTime) / 1_000_000_000.0f;
            timeUniform.set(time);
        }
        
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null && cameraPosUniform != null) {
            cameraPosUniform.set(
                (float) mc.player.getX(),
                (float) mc.player.getEyeY(),
                (float) mc.player.getZ()
            );
        }
        
        if (color1Uniform != null) {
            float r = color.getRed() / 255.0f;
            float g = color.getGreen() / 255.0f;
            float b = color.getBlue() / 255.0f;
            float a = color.getAlpha() / 255.0f;
            color1Uniform.set(r, g, b, a);
        }
        
        use();
    }
    
    private void setupBuffer() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (input.textureWidth != mc.getFramebuffer().textureWidth ||
            input.textureHeight != mc.getFramebuffer().textureHeight) {
            input.resize(mc.getFramebuffer().textureWidth,
                        mc.getFramebuffer().textureHeight);
        } else {
            input.clear();
        }
    }
    
    private void bind() {
        MinecraftClient mc = MinecraftClient.getInstance();
        Framebuffer renderTarget = mc.getFramebuffer();
        
        input.beginWrite(false);
        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, renderTarget.fbo);
        GL30.glBlitFramebuffer(0, 0, renderTarget.textureWidth, renderTarget.textureHeight,
                              0, 0, renderTarget.textureWidth, renderTarget.textureHeight,
                              GL30.GL_COLOR_BUFFER_BIT, GL30.GL_NEAREST);
        renderTarget.beginWrite(false);
        
        if (inputResolutionUniform != null)
            inputResolutionUniform.set((float) renderTarget.textureWidth,
                                     (float) renderTarget.textureHeight);
    }
    
    private void setUp() {
        ShaderProgram program = RenderSystem.setShader(MIRROR_SHADER_KEY);
        if (program == null) return;
        
        this.sizeUniform = program.getUniform("Size");
        this.radiusUniform = program.getUniform("Radius");
        this.smoothnessUniform = program.getUniform("Smoothness");
        this.inputResolutionUniform = program.getUniform("InputResolution");
        this.brightnessUniform = program.getUniform("Brightness");
        this.color1Uniform = program.getUniform("color1");
        this.reflectionIntensityUniform = program.getUniform("ReflectionIntensity");
        this.distortionStrengthUniform = program.getUniform("DistortionStrength");
        this.timeUniform = program.getUniform("Time");
        this.cameraPosUniform = program.getUniform("CameraPos");
        
        this.initialized = true;
    }
    
    public void use() {
        ShaderProgram program = RenderSystem.setShader(MIRROR_SHADER_KEY);
        if (program != null) {
            program.addSamplerTexture("InputSampler", input.getColorAttachment());
        }
    }
    
    public void cleanup() {
        if (input != null) {
            input.delete();
        }
    }
}