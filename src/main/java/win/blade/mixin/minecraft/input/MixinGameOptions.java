package win.blade.mixin.minecraft.input;

import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.Perspective;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import win.blade.core.Manager;
import win.blade.core.event.impl.render.PerspectiveEvent;

@Mixin(GameOptions.class)
public abstract class MixinGameOptions {

    @Shadow
    public abstract Perspective getPerspective();

    @Inject(method = "setPerspective", at = @At("HEAD"), cancellable = true)
    private void onSetPerspective(Perspective perspective, CallbackInfo ci) {
        Perspective previous = this.getPerspective();

        PerspectiveEvent event = new PerspectiveEvent(perspective, previous, true);
        Manager.EVENT_BUS.post(event);

        if(event.getPerspective() != perspective) {
            ci.cancel();
            ((GameOptions)(Object)this).setPerspective(event.getPerspective());
        }
    }
}