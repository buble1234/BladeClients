package win.blade.mixin.minecraft.entity;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import win.blade.common.utils.rotation.core.ViewDirection;
import win.blade.common.utils.rotation.manager.AimManager;

@Mixin(Entity.class)
public class MixinEntity {

    @Inject(method = "getRotationVec(F)Lnet/minecraft/util/math/Vec3d;", at = @At("HEAD"), cancellable = true)
    private void interceptRotationVector(float tickDelta, CallbackInfoReturnable<Vec3d> cir) {
        Entity self = (Entity)(Object)this;

        if (self instanceof ClientPlayerEntity && self == MinecraftClient.getInstance().player) {
            AimManager manager = AimManager.INSTANCE;
            ViewDirection direction = manager.getCurrentDirection();

            if (direction != null && manager.isEnabled()) {
                cir.setReturnValue(direction.asVector());
            }
        }
    }
}