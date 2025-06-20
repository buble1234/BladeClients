package win.blade.mixin.minecraft.input;

import net.minecraft.client.input.KeyboardInput;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import win.blade.common.utils.minecraft.MinecraftInstance;
import win.blade.common.utils.aim.core.ViewDirection;
import win.blade.common.utils.aim.manager.AimManager;
import win.blade.common.utils.aim.manager.TargetTask;

@Mixin(KeyboardInput.class)
public class MixinKeyboardInput implements MinecraftInstance {

    // Todo: сделать свободную коррекцию
//    @Inject(method = "tick", at = @At("TAIL"))
//    private void fixMovementCorrection(CallbackInfo ci) {
//        KeyboardInput input = (KeyboardInput) (Object) this;
//        ClientPlayerEntity player = mc.player;
//        AimManager aimManager = AimManager.INSTANCE;
//        ViewDirection aimDirection = aimManager.getCurrentDirection();
//        TargetTask task = aimManager.getActiveTask();
//
//        if (task == null || aimDirection == null || player == null || !task.settings().enableMovementFix()) {
//            return;
//        }
//
//        float originalForward = input.movementForward;
//        float originalSideways = input.movementSideways;
//
//        if (originalForward == 0.0f && originalSideways == 0.0f) {
//            return;
//        }
//
//        float playerYaw = MathHelper.wrapDegrees(player.getYaw());
//        float aimYaw = MathHelper.wrapDegrees(aimDirection.yaw());
//        float deltaYaw = MathHelper.wrapDegrees(aimYaw - playerYaw) * 0.017453292f;
//
//        float correctedSideways = originalSideways * MathHelper.cos(deltaYaw) - originalForward * MathHelper.sin(deltaYaw);
//        float correctedForward = originalForward * MathHelper.cos(deltaYaw) + originalSideways * MathHelper.sin(deltaYaw);
//
//        input.movementSideways = Math.round(correctedSideways);
//        input.movementForward = Math.round(correctedForward);
//    }
}