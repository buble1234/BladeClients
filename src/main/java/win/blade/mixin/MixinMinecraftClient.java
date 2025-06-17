package win.blade.mixin;

import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import win.blade.core.Manager;
import win.blade.core.event.controllers.EventHolder;

@Mixin(MinecraftClient.class)
public class MixinMinecraftClient {

    @Inject(at = @At("HEAD"), method = "tick")
    private void onTick(CallbackInfo ci) {
        Manager.EVENT_BUS.post(EventHolder.getUpdateEvent());
    }
}
