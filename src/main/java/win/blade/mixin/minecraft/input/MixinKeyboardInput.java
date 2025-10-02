package win.blade.mixin.minecraft.input;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.input.KeyboardInput;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.PlayerInput;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import win.blade.common.utils.minecraft.MinecraftInstance;
import win.blade.common.utils.aim.core.ViewDirection;
import win.blade.common.utils.aim.manager.AimManager;
import win.blade.common.utils.aim.manager.TargetTask;
import win.blade.common.utils.player.DirectionalInput;
import win.blade.core.Manager;
import win.blade.core.event.impl.player.SprintEvent;

@Mixin(KeyboardInput.class)
public class MixinKeyboardInput implements MinecraftInstance {


    @ModifyExpressionValue(method = "tick", at = @At(value = "NEW", target = "(ZZZZZZZ)Lnet/minecraft/util/PlayerInput;"))
    private PlayerInput modifyInput(PlayerInput original) {
        DirectionalInput directionalInput = new DirectionalInput(original);

        DirectionalInput transformedInput = transformDirection(directionalInput);

        SprintEvent sprintEvent = new SprintEvent(
                transformedInput,
                original.sprint(),
                SprintEvent.Source.INPUT
        );
        Manager.EVENT_BUS.post(sprintEvent);

        return new PlayerInput(
                transformedInput.isForwards(),
                transformedInput.isBackwards(),
                transformedInput.isLeft(),
                transformedInput.isRight(),
                original.jump(),
                original.sneak(),
                sprintEvent.getSprint()
        );
    }

    private DirectionalInput transformDirection(DirectionalInput input) {
        ClientPlayerEntity player = mc.player;
        AimManager aimManager = AimManager.INSTANCE;
        ViewDirection aimDirection = aimManager.getCurrentDirection();
        TargetTask task = aimManager.getActiveTask();

        if (task == null || aimDirection == null || player == null ||
                !task.settings().enableMovementFix() || !task.settings().enableSilentAim()) {
            return input;
        }

        float z = KeyboardInput.getMovementMultiplier(input.isForwards(), input.isBackwards());
        float x = KeyboardInput.getMovementMultiplier(input.isLeft(), input.isRight());

        float deltaYaw = player.getYaw() - aimDirection.yaw();

        float newX = x * MathHelper.cos(deltaYaw * 0.017453292f) - z * MathHelper.sin(deltaYaw * 0.017453292f);
        float newZ = z * MathHelper.cos(deltaYaw * 0.017453292f) + x * MathHelper.sin(deltaYaw * 0.017453292f);

        float movementSideways = Math.round(newX);
        float movementForward = Math.round(newZ);

        return new DirectionalInput(movementForward, movementSideways);
    }
}