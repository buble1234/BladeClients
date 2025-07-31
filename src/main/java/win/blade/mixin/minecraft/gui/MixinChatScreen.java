package win.blade.mixin.minecraft.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import win.blade.Blade;
import win.blade.core.Manager;
import win.blade.core.module.storage.misc.BetterMinecraftModule;

@Mixin(ChatScreen.class)
public class MixinChatScreen {

    @Shadow protected TextFieldWidget chatField;

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;fill(IIIII)V"))
    private void onRenderBackground(DrawContext context, int x1, int y1, int x2, int y2, int color) {
        BetterMinecraftModule betterMinecraft = Manager.getModuleManagement().get(BetterMinecraftModule.class);

        if (betterMinecraft.isEnabled() && betterMinecraft.simpleChat.getValue()) {
            int newX2 = 2 + this.chatField.getCharacterX(this.chatField.getText().length()) + 4;
            context.fill(x1, y1, newX2, y2, color);
        } else {
            context.fill(x1, y1, x2, y2, color);
        }
    }
}