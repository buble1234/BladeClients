package win.blade.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.AccessibilityOnboardingScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.GameOptions;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import win.blade.common.gui.impl.screen.firstlaunch.FirstlaunchScreen;
import win.blade.core.Manager;
import win.blade.core.event.controllers.EventHolder;

@Mixin(MinecraftClient.class)
public class MixinMinecraftClient {

    @Shadow @Final public GameOptions options;

    @Inject(at = @At("HEAD"), method = "tick")
    private void onTick(CallbackInfo ci) {
        Manager.EVENT_BUS.post(EventHolder.getUpdateEvent());
    }


    @Inject(method = "createInitScreens", at = @At("HEAD"))
    private void ons(CallbackInfo ci){
        ((MinecraftClient) (Object) this).options.onboardAccessibility = true;
    }


    @ModifyVariable(method = "setScreen", at = @At("HEAD"), argsOnly = true)
    private Screen onSetScreen(Screen screen) {
        if (screen instanceof AccessibilityOnboardingScreen) {
            return new FirstlaunchScreen(screen::close);
        }
        return screen;
    }
}
