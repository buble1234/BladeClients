package win.blade.mixin.minecraft.input;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.KeyboardInput;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import win.blade.common.utils.rotation.core.ViewDirection;
import win.blade.common.utils.rotation.manager.AimManager;

@Mixin(KeyboardInput.class)
public class MixinKeyboardInput {

    @Inject(method = "tick", at = @At("TAIL"))
    private void correctMovement(CallbackInfo ci) {
        var input = (KeyboardInput) (Object) this;
        var manager = AimManager.INSTANCE;
        var task = manager.getActiveTask();

        if (task == null || !task.settings().enableMovementFix()) {
            return;
        }

        var player = MinecraftClient.getInstance().player;
        ViewDirection aimDirection = manager.getCurrentDirection();

        if (player == null || aimDirection == null) return;

        float yawDelta = (player.getYaw() - aimDirection.yaw()) * 0.017453292f;
        float forward = input.movementForward;
        float sideways = input.movementSideways;

        input.movementSideways = Math.round(sideways * MathHelper.cos(yawDelta) - forward * MathHelper.sin(yawDelta));
        input.movementForward = Math.round(forward * MathHelper.cos(yawDelta) + sideways * MathHelper.sin(yawDelta));
    }
}