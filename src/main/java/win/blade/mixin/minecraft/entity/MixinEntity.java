package win.blade.mixin.minecraft.entity;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import win.blade.common.utils.minecraft.MinecraftInstance;
import win.blade.common.utils.aim.core.ViewDirection;
import win.blade.common.utils.aim.manager.AimManager;
import win.blade.common.utils.aim.manager.TargetTask;
import win.blade.core.Manager;
import win.blade.core.module.storage.move.NoPushModule;

import java.util.Optional;

@Mixin(Entity.class)
public class MixinEntity implements MinecraftInstance {

    @Inject(method = "getRotationVec(F)Lnet/minecraft/util/math/Vec3d;", at = @At("HEAD"), cancellable = true)
    private void onRotationVector(float tickDelta, CallbackInfoReturnable<Vec3d> cir) {
        Entity ent = (Entity)(Object)this;

        if (ent instanceof ClientPlayerEntity && ent == mc.player) {
            AimManager manager = AimManager.INSTANCE;
            ViewDirection direction = manager.getCurrentDirection();
            TargetTask task = manager.getActiveTask();

//            if (manager.isEnabled() && direction != null && task != null && (task.settings().enableSilentAim() || task.settings().enableMovementFix())) {
//                cir.setReturnValue(direction.asVector());
//            }
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
}