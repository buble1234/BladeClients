package win.blade.mixin.minecraft.client;

import net.minecraft.client.option.InactivityFpsLimiter;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.client.option.InactivityFpsLimiter;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Автор Ieo117
 * Дата создания: 30.07.2025, в 23:23:35
 */
@Mixin(InactivityFpsLimiter.class)
public class MixinInactivityFpsLimiter {

    /**
     * Инъекция в конец метода update.
     * Если метод должен был вернуть 60 (лимит FPS в меню), мы изменяем это значение на 240.
     * @param cir Информация о вызове для изменения возвращаемого значения.
     */
    @Inject(method = "update", at = @At("RETURN"), cancellable = true)
    private void modifyMenuFpsLimit(CallbackInfoReturnable<Integer> cir) {
        if (cir.getReturnValueI() == 60) {
            cir.setReturnValue(240);
        }
    }
}
