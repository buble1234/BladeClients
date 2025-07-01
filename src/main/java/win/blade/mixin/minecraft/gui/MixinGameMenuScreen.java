package win.blade.mixin.minecraft.gui;


import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import win.blade.common.gui.impl.screen.multiplayer.MultiplayerScreen;

@Mixin(GameMenuScreen.class)
public class MixinGameMenuScreen {

    @ModifyArg(
            method = "disconnect",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/MinecraftClient;setScreen(Lnet/minecraft/client/gui/screen/Screen;)V"
            )
    )
    private Screen blade$replaceMultiplayerScreenOnDisconnect(Screen screen) {
        if (screen instanceof net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen) {
            return new win.blade.common.gui.impl.screen.multiplayer.MultiplayerScreen();
        }
        return screen;
    }
}
