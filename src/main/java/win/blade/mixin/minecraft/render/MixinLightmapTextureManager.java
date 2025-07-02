package win.blade.mixin.minecraft.render;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.render.LightmapTextureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import win.blade.core.Manager;
import win.blade.core.event.controllers.EventHolder;
import win.blade.core.event.impl.minecraft.OptionEvents;

/**
 * Автор: NoCap
 * Дата создания: 02.07.2025
 */
@Mixin(LightmapTextureManager.class)
public class MixinLightmapTextureManager {

    @WrapOperation(method = "update(F)V " , at = @At(value = "INVOKE", target = "Ljava/lang/Double;floatValue()F", ordinal = 1))
    private float onUpdate(Double instance, Operation<Float> original) {
        OptionEvents.Gamma event = EventHolder.getGammaEvent(original.call(instance));
        Manager.EVENT_BUS.post(event);
        return event.getGamma();
    }
}