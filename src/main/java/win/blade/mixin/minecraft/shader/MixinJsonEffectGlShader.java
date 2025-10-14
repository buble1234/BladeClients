package win.blade.mixin.minecraft.shader;

import net.minecraft.client.gl.JsonEffectGlShader;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import win.blade.common.utils.shader.ShaderNamespaceHelper;

@Mixin(JsonEffectGlShader.class)
public class MixinJsonEffectGlShader {

    @Redirect(method = "<init>", at = @At(value = "NEW", target = "(Ljava/lang/String;)Lnet/minecraft/util/Identifier;"))
    private Identifier redirectIdentifierInConstructor(String path) {
        String namespace = ShaderNamespaceHelper.getCustomNamespace();

        if (namespace != null && !namespace.equals("minecraft")) {
            return new Identifier(namespace, path);
        }

        return new Identifier(path);
    }

    @Redirect(method = "loadEffect", at = @At(value = "NEW", target = "(Ljava/lang/String;)Lnet/minecraft/util/Identifier;"))
    private static Identifier redirectIdentifierInLoadEffect(String path) {
        String namespace = ShaderNamespaceHelper.getCustomNamespace();

        if (namespace != null && !namespace.equals("minecraft")) {
            return new Identifier(namespace, path);
        }

        return new Identifier(path);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void clearNamespaceAfterInit(CallbackInfo ci) {
        ShaderNamespaceHelper.clearCustomNamespace();
    }
}