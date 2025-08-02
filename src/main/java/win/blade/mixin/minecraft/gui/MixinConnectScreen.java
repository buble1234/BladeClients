package win.blade.mixin.minecraft.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import win.blade.common.gui.impl.screen.firstlaunch.FinishScreen;

/**
 * Автор Ieo117
 * Дата создания: 30.07.2025, в 19:29:20
 */
@Mixin(ConnectScreen.class)
public class MixinConnectScreen extends Screen {
    FinishScreen finishScreen;

    public MixinConnectScreen(Text title) {
        super(title);

    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    public void onRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci){
        ci.cancel();

        if(finishScreen == null){
            finishScreen = new FinishScreen().notClose();
            finishScreen.init();
        }

        var mc = MinecraftClient.getInstance();
        finishScreen.width = mc.getWindow().getScaledWidth();
        finishScreen.height = mc.getWindow().getScaledHeight();
        finishScreen.render(context, mouseX, mouseY, delta);
    }

}
