package win.blade.mixin.minecraft.network;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.command.CommandSource;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import win.blade.common.utils.system.LayoutUtil;
import win.blade.core.Manager;

@Mixin(ClientPlayNetworkHandler.class)
public class MixinClientPlayNetworkHandler {

    @Inject(method = "sendChatMessage", at = @At("HEAD"), cancellable = true)
    private void sendChatMessageHook(@NotNull String message, CallbackInfo ci) {
        if (!Manager.isPanic()) {
            String prefix = Manager.getCommandManager().getPrefix();
            if (message.startsWith(prefix)) {
                ci.cancel();

                String commandBody = message.substring(prefix.length());
                if (commandBody.isEmpty()) {
                    return;
                }

                String commandName = commandBody.split(" ")[0];

                boolean ruCommand = LayoutUtil.isCyrillic(commandName);
                boolean ruLayout = LayoutUtil.isRussianLayout();

                if (ruCommand == ruLayout) {
                    CommandDispatcher<CommandSource> dispatcher = ruLayout
                            ? Manager.getCommandManager().getRussian()
                            : Manager.getCommandManager().getEnglish();

                    try {
                        dispatcher.execute(commandBody, Manager.getCommandManager().getSource());
                    } catch (CommandSyntaxException ignored) {
                    }
                }
            }
        }
    }
}