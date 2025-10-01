package win.blade.mixin.minecraft.input;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.input.KeyboardInput;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.PlayerInput;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import win.blade.common.utils.minecraft.MinecraftInstance;
import win.blade.common.utils.aim.core.ViewDirection;
import win.blade.common.utils.aim.manager.AimManager;
import win.blade.common.utils.aim.manager.TargetTask;
import win.blade.core.Manager;
import win.blade.core.event.impl.player.PlayerInputEvent;

import java.awt.event.InputEvent;

@Mixin(KeyboardInput.class)
public class MixinKeyboardInput implements MinecraftInstance {

    @SuppressWarnings("all")
    @Inject(method = "tick", at = @At("TAIL"))
    private void fixMovementCorrection(CallbackInfo ci) {
        KeyboardInput input = (KeyboardInput) (Object) this;
        ClientPlayerEntity player = mc.player;
        AimManager aimManager = AimManager.INSTANCE;
        ViewDirection aimDirection = aimManager.getCurrentDirection();
        TargetTask task = aimManager.getActiveTask();

        if (task == null || aimDirection == null || player == null || !task.settings().enableMovementFix() || !task.settings().enableSilentAim()) {
            return;
        }

        if (player.isRiding()) {
            return;
        }

        float fixRotation = aimDirection.yaw();

        if (Float.isNaN(fixRotation)) {
            return;
        }

        PlayerInput currentInput = input.playerInput;

        float z = KeyboardInput.getMovementMultiplier(currentInput.forward(), currentInput.backward());
        float x = KeyboardInput.getMovementMultiplier(currentInput.left(), currentInput.right());

        if (z == 0.0f && x == 0.0f) {
            return;
        }

        float deltaYaw = player.getYaw() - fixRotation;

        while (deltaYaw > 180.0f) deltaYaw -= 360.0f;
        while (deltaYaw < -180.0f) deltaYaw += 360.0f;

        if (Math.abs(deltaYaw) < 1.0f) {
            return;
        }

        float newX = x * MathHelper.cos(deltaYaw * 0.017453292f) - z * MathHelper.sin(deltaYaw * 0.017453292f);
        float newZ = z * MathHelper.cos(deltaYaw * 0.017453292f) + x * MathHelper.sin(deltaYaw * 0.017453292f);

        newX = (float) Math.round(newX);
        newZ = (float) Math.round(newZ);

        boolean newForward = newZ > 0.0f;
        boolean newBackward = newZ < 0.0f;
        boolean newLeft = newX > 0.0f;
        boolean newRight = newX < 0.0f;

        input.playerInput = new PlayerInput(
                newForward,
                newBackward,
                newLeft,
                newRight,
                currentInput.jump(),
                currentInput.sneak(),
                currentInput.sprint()
        );

        input.movementForward = KeyboardInput.getMovementMultiplier(newForward, newBackward);
        input.movementSideways = KeyboardInput.getMovementMultiplier(newLeft, newRight);
    }

//    @ModifyExpressionValue(method = "tick", at = @At(value = "NEW", target = "(ZZZZZZZ)Lnet/minecraft/util/PlayerInput;"))
//    private PlayerInput tickHook(PlayerInput original) {
//        PlayerInputEvent event = new PlayerInputEvent(original);
//        Manager.EVENT_BUS.post(event);
//        input(event);
//        return transformInput(event.getInput());
//    }
//    public boolean canMove = true;
//
//    public void input(PlayerInputEvent e) {
//        if (!canMove) e.inputNone();
//    }
//
//
//    @Unique
//    private PlayerInput transformInput(PlayerInput input) {
//        AimManager manager = AimManager.INSTANCE;
//        ViewDirection direction = manager.getCurrentDirection();
//        TargetTask task = manager.getActiveTask();
//
//        if (mc.player == null || direction == null || task == null || !(task.settings().enableMovementFix() && task.settings().enableSilentAim())) {
//            return input;
//        }
//
//        float deltaYaw = mc.player.getYaw() - direction.yaw();
//        float z = KeyboardInput.getMovementMultiplier(input.forward(), input.backward());
//        float x = KeyboardInput.getMovementMultiplier(input.left(), input.right());
//        float newX = x * MathHelper.cos(deltaYaw * 0.017453292f) - z * MathHelper.sin(deltaYaw * 0.017453292f);
//        float newZ = z * MathHelper.cos(deltaYaw * 0.017453292f) + x * MathHelper.sin(deltaYaw * 0.017453292f);
//        int movementSideways = Math.round(newX), movementForward = Math.round(newZ);
//
//        return new PlayerInput(movementForward > 0F, movementForward < 0F, movementSideways > 0F, movementSideways < 0F, input.jump(), input.sneak(), input.sprint());
//    }
}