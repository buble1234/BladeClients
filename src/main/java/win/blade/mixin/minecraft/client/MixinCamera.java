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
import win.blade.common.utils.minecraft.MinecraftInstance;
import win.blade.common.utils.aim.core.ViewDirection;
import win.blade.common.utils.aim.manager.AimManager;

@Mixin(Camera.class)
public abstract class MixinCamera implements MinecraftInstance {

    @Shadow
    protected abstract void setRotation(float yaw, float pitch);

    @Inject(method = "update", at = @At(value = "TAIL"), cancellable = true)
    private void onCameraUpdate(BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci) {
        var manager = AimManager.INSTANCE;
        var task = manager.getActiveTask();

        if (task == null || !task.settings().enableViewSync()) {
            return;
        }

        if (manager.shouldInterpolate()) {
            ViewDirection current = manager.getCurrentDirection();
            ViewDirection previous = manager.getPreviousDirection();

            float deltaYaw = current.yaw() - previous.yaw();
            deltaYaw = ((deltaYaw % 360) + 540) % 360 - 180;

            float yaw = previous.yaw() + deltaYaw * tickDelta;
            yaw = MathHelper.wrapDegrees(yaw);

            float pitch = MathHelper.lerp(tickDelta, previous.pitch(), current.pitch());
            pitch = MathHelper.clamp(pitch, -90f, 90f);

            setRotation(yaw, pitch);

            if (focusedEntity != null) {
                focusedEntity.setYaw(yaw);
                focusedEntity.setPitch(pitch);
                focusedEntity.prevYaw = yaw;
                focusedEntity.prevPitch = pitch;
            }
        }
    }
}