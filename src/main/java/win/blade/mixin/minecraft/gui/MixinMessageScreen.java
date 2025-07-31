package win.blade.mixin.minecraft.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.MessageScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import win.blade.common.gui.impl.screen.firstlaunch.FinishScreen;

/**
 * Автор Ieo117
 * Дата создания: 30.07.2025, в 19:25:02
 */
@Mixin(MessageScreen.class)
public class MixinMessageScreen {


    @Inject(method = "init", at = @At("HEAD"), cancellable = true)
    public void init(CallbackInfo ci){
        MinecraftClient.getInstance().setScreen(new FinishScreen());
        ci.cancel();
    }
}
