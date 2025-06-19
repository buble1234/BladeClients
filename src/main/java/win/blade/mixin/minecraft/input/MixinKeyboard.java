package win.blade.mixin.minecraft.input;

import net.minecraft.client.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import win.blade.common.utils.minecraft.MinecraftInstance;
import win.blade.core.Manager;
import win.blade.core.event.controllers.EventHolder;

@Mixin(Keyboard.class)
public class MixinKeyboard implements MinecraftInstance {

    @Inject(at = @At("HEAD"), method = "onKey")
    private void injOnKey(long window, int key, int scancode, int i, int modifiers, CallbackInfo ci) {
        boolean whitelist = mc.currentScreen == null;
        if (!whitelist) return;
        if (i == 2) i = 1;
        Manager.EVENT_BUS.post(EventHolder.getKeyboardEvent(key, i));
    }
}