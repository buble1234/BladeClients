package win.blade.mixin.minecraft.client;

import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import win.blade.common.utils.rotation.core.ViewDirection;
import win.blade.common.utils.rotation.manager.AimManager;

@Mixin(Camera.class)
public abstract class MixinCamera {

    @Shadow
    protected abstract void setRotation(float yaw, float pitch);

    @Inject(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;setPos(DDD)V", shift = At.Shift.AFTER))
    private void hookCameraUpdate(BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci) {
        var manager = AimManager.INSTANCE;
        var task = manager.getActiveTask();

        if (task == null || !task.settings().enableViewSync()) return;

        if (manager.shouldInterpolate()) {
            ViewDirection current = manager.getCurrentDirection();
            ViewDirection previous = manager.getPreviousDirection();

            float yaw = MathHelper.lerp(tickDelta, previous.yaw(), current.yaw());
            float pitch = MathHelper.lerp(tickDelta, previous.pitch(), current.pitch());
            setRotation(yaw, pitch);
        }
    }
}