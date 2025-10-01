package win.blade.mixin.minecraft.render;

import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Fog;
import net.minecraft.client.render.FogShape;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import win.blade.common.utils.color.ColorUtility;
import win.blade.core.Manager;
import win.blade.core.event.impl.render.FogEvent;

@Mixin(BackgroundRenderer.class)
public class BackGroundRendererMixin {



    @Inject(method = "getFogColor", at = @At(value = "HEAD"), cancellable = true)
    private static void getFogColorHook(Camera camera, float tickDelta, ClientWorld world, int clampedViewDistance, float skyDarkness, CallbackInfoReturnable<Vector4f> cir) {
        FogEvent event = new FogEvent();
        Manager.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            int color = event.getColor();
            cir.setReturnValue(new Vector4f(ColorUtility.redf(color), ColorUtility.greenf(color), ColorUtility.bluef(color), ColorUtility.alphaf(color)));
        }
    }

    @Inject(method = "applyFog", at = @At(value = "HEAD"), cancellable = true)
    private static void modifyFog(Camera camera, BackgroundRenderer.FogType fogType, Vector4f vector4f, float viewDistance, boolean thickenFog, float tickDelta, CallbackInfoReturnable<Fog> cir) {
        FogEvent event = new FogEvent();
        Manager.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            int color = event.getColor();
            cir.setReturnValue(new Fog(2.0F, event.getDistance(),  FogShape.CYLINDER, ColorUtility.redf(color), ColorUtility.greenf(color), ColorUtility.bluef(color), ColorUtility.alphaf(color)));
        }
    }
}
