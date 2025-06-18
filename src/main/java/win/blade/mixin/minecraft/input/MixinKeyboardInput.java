package win.blade.mixin.minecraft.input;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.KeyboardInput;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import win.blade.common.utils.rotation.core.ViewDirection;
import win.blade.common.utils.rotation.manager.AimManager;
import win.blade.common.utils.rotation.manager.TargetTask;

@Mixin(KeyboardInput.class)
public class MixinKeyboardInput extends MixinInput {

    @Inject(method = "tick", at = @At("TAIL"))
    private void fixStrafeMovement(CallbackInfo ci) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        AimManager manager = AimManager.INSTANCE;
        ViewDirection aimDirection = manager.getCurrentDirection();
        TargetTask task = manager.getActiveTask();

        if (player == null || task == null || aimDirection == null || !task.settings().enableMovementFix()) {
            return;
        }

        float originalForward = this.movementForward;
        float originalSideways = this.movementSideways;

        float deltaYaw = player.getYaw() - aimDirection.yaw();
        float deltaYawRad = deltaYaw * 0.017453292f;

        float correctedSideways = originalSideways * MathHelper.cos(deltaYawRad) - originalForward * MathHelper.sin(deltaYawRad);
        float correctedForward = originalForward * MathHelper.cos(deltaYawRad) + originalSideways * MathHelper.sin(deltaYawRad);

        this.movementSideways = correctedSideways;
        this.movementForward = correctedForward;
    }
}