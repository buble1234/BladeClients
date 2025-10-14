package win.blade.mixin.minecraft.shader;

import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.PostEffectProcessor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import win.blade.common.utils.shader.ShaderNamespaceHelper;
import win.blade.common.utils.shader.ShaderTarget;
import win.blade.mixin.accessor.AccessorPostProcessShader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mixin(PostEffectProcessor.class)
public class MixinShaderEffect implements ShaderTarget {

    private final List<String> fakedBufferNames = new ArrayList<>();

    @Shadow
    @Final
    private Map<String, Framebuffer> targetsByName;

    @Shadow
    @Final
    private List<PostProcessShader> passes;

    @Override
    public List<PostProcessShader> getPasses() {
        return passes;
    }

    @Override
    public void addFakeTarget(String name, Framebuffer buffer) {
        Framebuffer previousFramebuffer = this.targetsByName.get(name);
        if (previousFramebuffer == buffer) {
            return;
        }
        if (previousFramebuffer != null) {
            for (PostProcessShader pass : this.passes) {
                if (pass.input == previousFramebuffer) {
                    ((AccessorPostProcessShader) pass).setInput(buffer);
                }
                if (pass.output == previousFramebuffer) {
                    ((AccessorPostProcessShader) pass).setOutput(buffer);
                }
            }
            this.targetsByName.remove(name);
            this.fakedBufferNames.remove(name);
        }

        this.targetsByName.put(name, buffer);
        this.fakedBufferNames.add(name);
    }

    @Inject(method = "close", at = @At("HEAD"))
    void deleteFakeBuffers(CallbackInfo ci) {
        for (String fakedBufferName : fakedBufferNames) {
            targetsByName.remove(fakedBufferName);
        }
    }

    @ModifyVariable(
            method = "parsePass",
            at = @At(value = "INVOKE_ASSIGN",
                    target = "Lnet/minecraft/util/JsonHelper;getString(Lcom/google/gson/JsonObject;Ljava/lang/String;)Ljava/lang/String;",
                    ordinal = 0),
            ordinal = 0
    )
    private String modifyProgramName(String programName) {
        if (programName.contains(":")) {
            String[] parts = programName.split(":", 2);
            String namespace = parts[0];
            String name = parts[1];

            ShaderNamespaceHelper.setCustomNamespace(namespace);

            return name;
        }

        return programName;
    }
}