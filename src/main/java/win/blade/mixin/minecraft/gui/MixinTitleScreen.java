package win.blade.mixin.minecraft.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import win.blade.common.gui.impl.MainScreen;
import win.blade.common.utils.minecraft.MinecraftInstance;
import win.blade.core.Manager;

@Mixin(TitleScreen.class)
public class MixinTitleScreen implements MinecraftInstance {

    @Inject(method = "init", at = @At("RETURN"))
    public void postInitHook(CallbackInfo callbackInfo) {
        if (!Manager.isPanic()) {
            mc.setScreen(new MainScreen());
        }
    }
}
