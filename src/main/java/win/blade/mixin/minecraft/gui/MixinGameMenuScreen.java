package win.blade.mixin.minecraft.gui;

import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import win.blade.common.gui.impl.screen.options.PauseScreen;
import win.blade.core.Manager;

import java.net.URI;
import java.net.URISyntaxException;

import static win.blade.common.utils.minecraft.MinecraftInstance.mc;

@Mixin(GameMenuScreen.class)
public class MixinGameMenuScreen {

    @Inject(method = "init", at = @At("HEAD"), cancellable = true)
    private void onInit(CallbackInfo ci) {
        if (!Manager.isPanic()) {
            mc.setScreen(new PauseScreen(null));
            ci.cancel();
        }
    }

    @ModifyArg(
            method = "disconnect",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/MinecraftClient;setScreen(Lnet/minecraft/client/gui/screen/Screen;)V"
            )
    )
    private Screen blade$replaceMultiplayerScreenOnDisconnect(Screen screen) {
        if (screen instanceof net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen && !Manager.isPanic()) {
            return new win.blade.common.gui.impl.screen.multiplayer.MultiplayerScreen();

        }

        return screen;
    }

    @ModifyArg(method = "addFeedbackAndBugsButtons", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/GameMenuScreen;createUrlButton(Lnet/minecraft/client/gui/screen/Screen;Lnet/minecraft/text/Text;Ljava/net/URI;)Lnet/minecraft/client/gui/widget/ButtonWidget;", ordinal = 0), index = 2)
    private static URI replaceFeedbackLink(URI originalUri) {
        if (Manager.isPanic()) {
            return originalUri;
        }

        String link = "https://discord.gg/bladerecode";
        try {
            return new URI(link);
        } catch (URISyntaxException e) {
            return originalUri;
        }
    }

    @ModifyArg(method = "addFeedbackAndBugsButtons", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/GameMenuScreen;createUrlButton(Lnet/minecraft/client/gui/screen/Screen;Lnet/minecraft/text/Text;Ljava/net/URI;)Lnet/minecraft/client/gui/widget/ButtonWidget;", ordinal = 1), index = 2)
    private static URI replaceBugReportLink(URI originalUri) {
        if (Manager.isPanic()) {
            return originalUri;
        }

        String link = "https://t.me/bladeclient_tg";
        try {
            return new URI(link);
        } catch (URISyntaxException e) {
            return originalUri;
        }
    }


}