package win.blade.mixin.minecraft.gui;


import net.minecraft.client.network.MultiplayerServerListPinger;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;



@Mixin(MultiplayerServerListPinger.class)
public class MixinMultiplayerServerListPinger {

    @Inject(method = "showError",
            at = @At("HEAD"), cancellable = true)
    private void showError(Text error, ServerInfo info, CallbackInfo ci) {
        info.label = Text.translatable("multiplayer.status.cannot_connect").formatted(Formatting.RED);
        info.playerCountLabel = ScreenTexts.EMPTY;
        ci.cancel();
    }


}
