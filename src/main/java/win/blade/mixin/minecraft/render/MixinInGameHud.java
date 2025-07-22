package win.blade.mixin.minecraft.render;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.scoreboard.ScoreboardObjective;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import win.blade.common.utils.minecraft.MinecraftInstance;
import win.blade.core.Manager;
import win.blade.core.event.controllers.EventHolder;
import win.blade.core.event.impl.render.RenderCancelEvents;

@Mixin(InGameHud.class)
public abstract class MixinInGameHud implements MinecraftInstance {

    @Inject(at = @At("HEAD"), method = "render")
    public void onRenderScreen(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        Manager.EVENT_BUS.post(EventHolder.getPREScreenRenderEvent(new MatrixStack(), tickCounter.getTickDelta(false), context));
    }

    @Inject(method = "renderScoreboardSidebar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/scoreboard/ScoreboardObjective;)V", at = @At(value = "HEAD"), cancellable = true)
    private void onRenderScoreboardSidebar(DrawContext context, ScoreboardObjective objective, CallbackInfo ci) {
        RenderCancelEvents.Scoreboard event = new RenderCancelEvents.Scoreboard();
        Manager.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderNauseaOverlay", at = @At("HEAD"), cancellable = true)
    private void renderNauseaOverlayHook(DrawContext context, float nauseaStrength, CallbackInfo ci) {
        RenderCancelEvents.BadEffects event = new RenderCancelEvents.BadEffects();
        Manager.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderMiscOverlays", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderOverlay(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/util/Identifier;F)V", ordinal = 1), cancellable = true)
    private void onRenderFreezeOverlay(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        RenderCancelEvents.FreezeOverlay event = new RenderCancelEvents.FreezeOverlay();
        Manager.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderPortalOverlay", at = @At("HEAD"), cancellable = true)
    private void onRenderPortalOverlay(DrawContext context, float nauseaStrength, CallbackInfo ci) {
        RenderCancelEvents.PortalOverlay event = new RenderCancelEvents.PortalOverlay();
        Manager.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }
}
