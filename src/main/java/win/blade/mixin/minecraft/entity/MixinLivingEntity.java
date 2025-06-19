package win.blade.mixin.minecraft.entity;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import win.blade.common.utils.minecraft.MinecraftInstance;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity implements MinecraftInstance {

    @Redirect(method = "jump", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;addVelocityInternal(Lnet/minecraft/util/math/Vec3d;)V"))
    private void correctJumpMovement(LivingEntity entity, Vec3d velocity) {
        entity.addVelocityInternal(velocity);
    }
}