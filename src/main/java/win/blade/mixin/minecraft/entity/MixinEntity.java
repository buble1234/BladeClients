package win.blade.mixin.minecraft.entity;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import win.blade.common.utils.minecraft.MinecraftInstance;
import win.blade.common.utils.aim.core.ViewDirection;
import win.blade.common.utils.aim.manager.AimManager;
import win.blade.common.utils.aim.manager.TargetTask;
import win.blade.core.Manager;
import win.blade.core.module.api.ModuleManager;
import win.blade.core.module.storage.move.NoPushModule;
import win.blade.core.module.storage.render.ShaderESP;

import java.util.Optional;

@Mixin(Entity.class)
public abstract class MixinEntity implements MinecraftInstance {

    @Shadow public abstract boolean isFireImmune();

    @Inject(method = "getRotationVec(F)Lnet/minecraft/util/math/Vec3d;", at = @At("HEAD"), cancellable = true)
    private void onRotationVector(float tickDelta, CallbackInfoReturnable<Vec3d> cir) {
        Entity ent = (Entity)(Object)this;

        if (ent instanceof ClientPlayerEntity && ent == mc.player) {
            AimManager manager = AimManager.INSTANCE;
            ViewDirection direction = manager.getCurrentDirection();
            TargetTask task = manager.getActiveTask();

            if (manager.isEnabled() && direction != null && task != null) {
                cir.setReturnValue(direction.asVector());
            }
        }
    }

    @ModifyArgs(method = "pushAwayFrom", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;addVelocity(DDD)V"))
    public void onPushAwayFrom(Args args) {
        Optional<NoPushModule> noPush = Manager.getModuleManagement().find(NoPushModule.class);
        if (noPush.isPresent() && noPush.get().isEnabled() && noPush.get().options.getValue("Сущностей")) {
            if ((Object) this == mc.player) {
                args.set(0, 0.);
                args.set(1, 0.);
                args.set(2, 0.);
            }
        }
    }

    @Inject(method = "updateVelocity", at = @At("HEAD"), cancellable = true)
    public void updateVelocityHook(float speed, Vec3d movementInput, CallbackInfo ci) {
        if ((Object) this == mc.player) {
            AimManager aimManager = AimManager.INSTANCE;
            ViewDirection aimDirection = aimManager.getCurrentDirection();
            TargetTask task = aimManager.getActiveTask();

            if (task != null && aimDirection != null && task.settings().enableMovementFix()) {
                ci.cancel();

                Vec3d correctedVelocity = fixMovementInput(movementInput, speed, aimDirection.yaw());
                mc.player.setVelocity(mc.player.getVelocity().add(correctedVelocity));
            }
        }
    }

    @Unique
    private Vec3d fixMovementInput(Vec3d movementInput, float speed, float targetYaw) {
        double d = movementInput.lengthSquared();
        if (d < 1.0E-7) {
            return Vec3d.ZERO;
        }

        Vec3d vec3d = (d > 1.0 ? movementInput.normalize() : movementInput).multiply(speed);
        float f = MathHelper.sin(targetYaw * MathHelper.RADIANS_PER_DEGREE);
        float g = MathHelper.cos(targetYaw * MathHelper.RADIANS_PER_DEGREE);

        return new Vec3d(
                vec3d.x * (double) g - vec3d.z * (double) f,
                vec3d.y,
                vec3d.z * (double) g + vec3d.x * (double) f
        );
    }

    @Inject(method = "isGlowing", at = @At("HEAD"), cancellable = true)
    private void injGlowing(CallbackInfoReturnable<Boolean> cir) {
        Entity entity = (Entity) (Object) this;

        if (entity instanceof PlayerEntity && ShaderESP.show) {
            cir.setReturnValue(true);
        }
    }
}