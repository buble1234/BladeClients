package win.blade.mixin.minecraft.entity;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import win.blade.common.utils.rotation.core.ViewDirection;
import win.blade.common.utils.rotation.manager.AimManager;
import win.blade.common.utils.rotation.manager.TargetTask;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity {

    @Redirect(
            method = "jump",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;addVelocityInternal(Lnet/minecraft/util/math/Vec3d;)V")
    )
    private void correctJumpMovement(LivingEntity entity, Vec3d velocity) {
        if (entity != MinecraftClient.getInstance().player) {
            entity.addVelocityInternal(velocity);
            return;
        }

        AimManager manager = AimManager.INSTANCE;
        TargetTask task = manager.getActiveTask();
        ViewDirection aimDirection = manager.getCurrentDirection();

        if (task == null || !task.settings().enableMovementFix() || aimDirection == null) {
            entity.addVelocityInternal(velocity);
            return;
        }

        float yawRad = aimDirection.yaw() * 0.017453292F;
        Vec3d correctedVelocity = new Vec3d(
                -MathHelper.sin(yawRad) * 0.2,
                velocity.y,
                MathHelper.cos(yawRad) * 0.2
        );

        entity.addVelocityInternal(correctedVelocity);
    }
}