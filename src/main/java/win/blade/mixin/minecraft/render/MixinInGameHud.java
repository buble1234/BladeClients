package win.blade.mixin.minecraft.render;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import win.blade.common.utils.minecraft.MinecraftInstance;
import win.blade.core.Manager;
import win.blade.core.event.controllers.EventHolder;

@Mixin(InGameHud.class)
public abstract class MixinInGameHud implements MinecraftInstance {

    @Inject(at = @At("HEAD"), method = "render")
    public void onRenderScreen(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        Manager.EVENT_BUS.post(EventHolder.getScreenRenderEvent(new MatrixStack(), tickCounter.getTickDelta(false), context));
    }

}
