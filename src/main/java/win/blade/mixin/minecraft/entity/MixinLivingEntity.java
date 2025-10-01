package win.blade.mixin.minecraft.entity;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import win.blade.common.utils.aim.core.ViewDirection;
import win.blade.common.utils.aim.manager.AimManager;
import win.blade.common.utils.aim.manager.TargetTask;
import win.blade.common.utils.minecraft.MinecraftInstance;
import win.blade.core.Manager;
import win.blade.core.event.controllers.EventHolder;
import win.blade.core.event.impl.player.PlayerActionEvents;
import win.blade.core.event.item.SwingDurationEvent;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity implements MinecraftInstance {

    @Shadow public abstract boolean hasStatusEffect(RegistryEntry<StatusEffect> effect);
    @Unique private final MinecraftClient client = MinecraftClient.getInstance();

    @Shadow
    @Nullable
    public abstract StatusEffectInstance getStatusEffect(RegistryEntry<StatusEffect> effect);
    @Inject(method = "jump", at = @At("HEAD"), cancellable = true)
    private void onJumpEvent(CallbackInfo ci) {
        if ((Object) this != mc.player) {
            return;
        }

        final PlayerActionEvents.Jump event = EventHolder.getJumpEvent();
        Manager.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }
    @Shadow protected abstract double getEffectiveGravity();
    @Shadow protected abstract void checkGlidingCollision(double d, double e);


    @SuppressWarnings("all")
    @Inject(method = "travelGliding", at = @At("HEAD"), cancellable = true)
    private void onTravelGliding(CallbackInfo ci) {
        if (!((Object) this instanceof ClientPlayerEntity player)) {
            return;
        }

        AimManager manager = AimManager.INSTANCE;
        ViewDirection direction = manager.getCurrentDirection();
        TargetTask task = manager.getActiveTask();

        if (manager.isEnabled() && direction != null && task != null && task.settings().enableMovementFix()) {
            ci.cancel();


            Vec3d oldVelocity = player.getVelocity();
            double horizontalSpeedBefore = oldVelocity.horizontalLength();

            Vec3d rotationVector = direction.asVector();
            float pitch = direction.pitch();

            float pitchRad = pitch * 0.017453292F;
            double rotVecHorizontalLength = Math.sqrt(rotationVector.x * rotationVector.x + rotationVector.z * rotationVector.z);
            double oldHorizontalSpeed = oldVelocity.horizontalLength();
            double gravity = getEffectiveGravity();

            double cosPitchSquared = MathHelper.square(Math.cos(pitchRad));

            Vec3d newVelocity = oldVelocity.add(0.0, gravity * (-1.0 + cosPitchSquared * 0.75), 0.0);

            double temp_i;
            if (newVelocity.y < 0.0 && rotVecHorizontalLength > 0.0) {
                temp_i = newVelocity.y * -0.1 * cosPitchSquared;
                newVelocity = newVelocity.add(rotationVector.x * temp_i / rotVecHorizontalLength, temp_i, rotationVector.z * temp_i / rotVecHorizontalLength);
            }

            if (pitchRad < 0.0F && rotVecHorizontalLength > 0.0) {
                temp_i = oldHorizontalSpeed * (double)(-MathHelper.sin(pitchRad)) * 0.04;
                newVelocity = newVelocity.add(-rotationVector.x * temp_i / rotVecHorizontalLength, temp_i * 3.2, -rotationVector.z * temp_i / rotVecHorizontalLength);
            }

            if (rotVecHorizontalLength > 0.0) {
                newVelocity = newVelocity.add((rotationVector.x / rotVecHorizontalLength * oldHorizontalSpeed - newVelocity.x) * 0.1, 0.0, (rotationVector.z / rotVecHorizontalLength * oldHorizontalSpeed - newVelocity.z) * 0.1);
            }

            newVelocity = newVelocity.multiply(0.9900000095367432, 0.9800000190734863, 0.9900000095367432);

            player.setVelocity(newVelocity);
            player.move(MovementType.SELF, player.getVelocity());

            if (!player.getWorld().isClient) {
                double horizontalSpeedAfter = player.getVelocity().horizontalLength();
                checkGlidingCollision(horizontalSpeedBefore, horizontalSpeedAfter);
            }
        }
    }

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


    @Inject(method = "getHandSwingDuration", at = @At("HEAD"), cancellable = true)
    private void swingProgressHook(CallbackInfoReturnable<Integer> cir) {
        if ((Object) this != client.player) {
            return;
        }

        SwingDurationEvent event = new SwingDurationEvent();
        Manager.EVENT_BUS.post(event);

        if (event.isCancelled()) {
            float animation = event.getAnimation();
            if (StatusEffectUtil.hasHaste(client.player)) animation *= (6 - (1 + StatusEffectUtil.getHasteAmplifier(client.player)));
            else animation *= (hasStatusEffect(StatusEffects.MINING_FATIGUE) ? 6 + (1 + getStatusEffect(StatusEffects.MINING_FATIGUE).getAmplifier()) * 2 : 6);
            cir.setReturnValue((int) animation);
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



    @Redirect(
            method = "travelInFluid(Lnet/minecraft/util/math/Vec3d;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;updateVelocity(FLnet/minecraft/util/math/Vec3d;)V")
    )
    private void redirectFluidMovement(LivingEntity instance, float speed, Vec3d movementInput) {
        if (instance instanceof ClientPlayerEntity) {
            AimManager manager = AimManager.INSTANCE;
            ViewDirection direction = manager.getCurrentDirection();
            TargetTask task = manager.getActiveTask();

            if (manager.isEnabled() && direction != null && task != null && task.settings().enableMovementFix()) {
                blade$updateVelocityWithCorrectedYaw(instance, speed, movementInput, direction.yaw());
                return;
            }
        }

        instance.updateVelocity(speed, movementInput);
    }

    @Unique
    private void blade$updateVelocityWithCorrectedYaw(LivingEntity entity, float speed, Vec3d movementInput, float yaw) {
        double d = movementInput.lengthSquared();
        if (d < 1.0E-7) {
            return;
        }

        Vec3d vec3d = (d > 1.0 ? movementInput.normalize() : movementInput).multiply(speed);
        float f = MathHelper.sin(yaw * 0.017453292F);
        float g = MathHelper.cos(yaw * 0.017453292F);

        Vec3d correctedVelocity = new Vec3d(vec3d.x * (double)g - vec3d.z * (double)f, vec3d.y, vec3d.z * (double)g + vec3d.x * (double)f);

        entity.setVelocity(entity.getVelocity().add(correctedVelocity));
    }

}