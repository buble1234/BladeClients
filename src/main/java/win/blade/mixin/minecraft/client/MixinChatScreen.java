package win.blade.mixin.minecraft.client;

import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import win.blade.common.utils.system.LayoutUtil;


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
}