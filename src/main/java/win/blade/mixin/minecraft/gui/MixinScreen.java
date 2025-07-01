package win.blade.mixin.minecraft.gui;

import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import win.blade.common.gui.impl.MainScreen;
import win.blade.common.gui.impl.screen.OptionsScreen;
import win.blade.common.utils.minecraft.MinecraftInstance;

@Mixin(Screen.class)
public class MixinScreen implements MinecraftInstance {

    @Inject(method = "renderBackground", at = @At("HEAD"), cancellable = true)
    public void postInitHook(CallbackInfo callbackInfo) {
        callbackInfo.cancel();
    }

}
