package win.blade.mixin.minecraft.entity;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import win.blade.common.utils.aim.core.ViewDirection;
import win.blade.common.utils.aim.manager.AimManager;
import win.blade.common.utils.aim.manager.TargetTask;
import win.blade.common.utils.minecraft.MinecraftInstance;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity implements MinecraftInstance {

    @Redirect(method = "jump", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;addVelocityInternal(Lnet/minecraft/util/math/Vec3d;)V"))
    private void correctJumpMovement(LivingEntity entity, Vec3d velocity) {
        if (entity == mc.player) {
            AimManager manager = AimManager.INSTANCE;
            ViewDirection direction = manager.getCurrentDirection();
            TargetTask task = manager.getActiveTask();

            if (manager.isEnabled() && direction != null && task != null && task.settings().enableMovementFix()) {
                Vec3d correctedJumpVelocity = correctJumpDirection(velocity, direction.yaw());
                entity.addVelocityInternal(correctedJumpVelocity);
            } else {
                entity.addVelocityInternal(velocity);
            }
        } else {
            entity.addVelocityInternal(velocity);
        }
    }

    @Unique
    private Vec3d correctJumpDirection(Vec3d originalVelocity, float targetYaw) {
        double yVelocity = originalVelocity.y;

        double horizontalLength = Math.sqrt(originalVelocity.x * originalVelocity.x + originalVelocity.z * originalVelocity.z);

        if (horizontalLength < 1.0E-7) {
            return originalVelocity;
        }

        float yawRad = targetYaw * 0.017453292F;
        double newX = -Math.sin(yawRad) * horizontalLength;
        double newZ = Math.cos(yawRad) * horizontalLength;

        return new Vec3d(newX, yVelocity, newZ);
    }
}