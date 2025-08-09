package win.blade.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.AccessibilityOnboardingScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import win.blade.common.gui.impl.MainScreen;
import win.blade.common.gui.impl.screen.firstlaunch.FirstlaunchScreen;
import win.blade.core.Manager;
import win.blade.core.event.controllers.EventHolder;
import win.blade.core.event.impl.render.WorldLoadEvent;

import javax.annotation.Nullable;

@Mixin(MinecraftClient.class)
public class MixinMinecraftClient {

    @Shadow @Final public GameOptions options;

    @Inject(at = @At("HEAD"), method = "tick")
    private void onTick(CallbackInfo ci) {
        Manager.EVENT_BUS.post(EventHolder.getUpdateEvent());
    }


    @ModifyVariable(method = "setScreen", at = @At("HEAD"), argsOnly = true)
    private Screen onSetScreen(Screen screen) {
        if (screen instanceof AccessibilityOnboardingScreen) {
            return new FirstlaunchScreen(screen::close);
        }
        return screen;
    }

    @Inject(method = "getWindowTitle", at = @At(value = "HEAD"), cancellable = true)
    private void getWindowTitle(CallbackInfoReturnable<String> cir) {
        if (!Manager.isPanic()) {
            cir.setReturnValue("Blade Client v1.0");
        }
    }

    @Inject(method = "setWorld", at = @At("HEAD"))
    private void onSetWorld(@Nullable ClientWorld world, CallbackInfo ci) {
        Manager.EVENT_BUS.post(new WorldLoadEvent());
    }

    @Inject(method = "scheduleStop", at = @At("HEAD"), cancellable = true)
    private void onQuit(CallbackInfo ci){
        try {
            win.blade.core.Manager.executorService.execute(() -> win.blade.common.utils.config.ConfigManager.instance.saveConfig("default"));
        } catch (Throwable ignored) {
            try { win.blade.common.utils.config.ConfigManager.instance.saveConfig("default"); } catch (Throwable ignored2) {}
        }
        if(!Manager.canQuit()){
            ci.cancel();
        }
    }
}
