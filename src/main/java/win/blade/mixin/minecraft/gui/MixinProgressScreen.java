package win.blade.mixin.minecraft.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ProgressScreen;
import net.minecraft.client.gui.screen.world.LevelLoadingScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import win.blade.common.gui.impl.screen.firstlaunch.FinishScreen;

/**
 * Автор Ieo117
 * Дата создания: 30.07.2025, в 18:38:30
 */
@Mixin(LevelLoadingScreen.class)
public class MixinProgressScreen {
    @Shadow
    private boolean done;

    private FinishScreen screen;

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    public void onRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (screen == null) {
            screen = new FinishScreen();
            screen.init();
        }
        MinecraftClient mc = MinecraftClient.getInstance();

        if (! this.done) {
            screen.width = mc.getWindow().getScaledWidth();
            screen.height = mc.getWindow().getScaledHeight();


            screen.render(context, mouseX, mouseY, delta);
        }

        ci.cancel();
//        else if(MinecraftClient.getInstance().currentScreen != null) {
//            MinecraftClient.getInstance().setScreen(null);
//        }
    }
}
