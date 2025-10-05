package win.blade.mixin.minecraft.gui;

import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import win.blade.Blade;
import win.blade.common.utils.ignore.IgnoreManager;
import win.blade.core.Manager;
import win.blade.core.module.storage.misc.BetterMinecraftModule;

import java.util.List;
import java.util.Map;

@Mixin(ChatHud.class)
public abstract class MixinChatHud {

    private String last = null;
    private int repeatC = 1;

    @Shadow @Final private List<ChatHudLine> messages;
    @Shadow private void refresh() {}

    @Inject(method = "addMessage(Lnet/minecraft/text/Text;)V", at = @At("HEAD"), cancellable = true)
    private void onAddMessage(Text message, CallbackInfo ci) {
        String messageText = message.getString();

        if (IgnoreManager.instance != null) {
            if (IgnoreManager.instance.shouldHideMessage(messageText)) {
                ci.cancel();
                return;
            }
        }

        BetterMinecraftModule betterMinecraft = Manager.getModuleManagement().get(BetterMinecraftModule.class);
        if (!betterMinecraft.isEnabled() || !betterMinecraft.antiSpam.getValue()) {
            last = null;
            repeatC = 1;
            return;
        }

        if (last != null && last.equals(messageText)) {
            ci.cancel();

            repeatC++;

            if (!this.messages.isEmpty()) {
                ChatHudLine lastLine = this.messages.get(0);

                MutableText updatedText = Text.empty()
                        .append(message)
                        .append(Text.literal(" (x" + repeatC + ")").formatted(Formatting.GRAY));

                this.messages.set(0, new ChatHudLine(lastLine.creationTick(), updatedText, lastLine.signature(), lastLine.indicator()));
            }

            this.refresh();

        } else {
            this.repeatC = 1;
            this.last = messageText;
        }
    }
}