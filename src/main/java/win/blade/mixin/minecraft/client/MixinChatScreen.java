package win.blade.mixin.minecraft.client;

import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.network.packet.c2s.play.CommandExecutionC2SPacket;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import win.blade.common.utils.system.LayoutUtil;

import static win.blade.common.utils.minecraft.MinecraftInstance.mc;


@Mixin(ChatScreen.class)
public abstract class MixinChatScreen extends Screen {

    @Shadow
    protected ChatInputSuggestor chatInputSuggestor;

    private boolean blade_lastLayoutWasRussian;

    protected MixinChatScreen(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        this.blade_lastLayoutWasRussian = LayoutUtil.isRussianLayout();
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void onRender(CallbackInfo ci) {
        boolean isRussian = LayoutUtil.isRussianLayout();
        if (isRussian != this.blade_lastLayoutWasRussian) {
            this.blade_lastLayoutWasRussian = isRussian;
            if (this.chatInputSuggestor != null) {
                this.chatInputSuggestor.refresh();
            }
        }
    }

    @Inject(method = "sendMessage(Ljava/lang/String;Z)V", at = @At("HEAD"), cancellable = true)
    private void onSendMessage(String message, boolean clientSide, CallbackInfo ci) {
        if (mc.player == null) {
            return;
        }
        if (message.startsWith("/ah me")) {
            String playerName = mc.player.getName().getString();
            String newMessage = message.substring(1).replace("me", playerName);

            ci.cancel();
            mc.player.networkHandler.sendPacket(new CommandExecutionC2SPacket(newMessage));
        }
    }
}