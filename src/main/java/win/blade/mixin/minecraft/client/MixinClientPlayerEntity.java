package win.blade.mixin.minecraft.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import win.blade.common.utils.rotation.core.ViewDirection;
import win.blade.common.utils.rotation.manager.AimManager;
import win.blade.core.Manager;
import win.blade.core.event.controllers.EventHolder;
import win.blade.core.event.impl.player.PlayerActionEvents;

@Mixin(value = ClientPlayerEntity.class)
public abstract class MixinClientPlayerEntity extends AbstractClientPlayerEntity {

    public MixinClientPlayerEntity(ClientWorld world, GameProfile profile) {
        super(world, profile);
    }

    @Inject(method = "closeHandledScreen", at = @At("HEAD"), cancellable = true)
    private void onCloseHandledScreen(CallbackInfo info) {
        PlayerActionEvents.CloseInventory event = EventHolder.getCloseInventoryEvent(this.currentScreenHandler.syncId);
        Manager.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            info.cancel();
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onPlayerTick(CallbackInfo ci) {
        AimManager.INSTANCE.tick();
    }

    @ModifyExpressionValue(method = {"sendMovementPackets", "tick"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getYaw()F"))
    private float interceptYaw(float original) {
        ViewDirection direction = AimManager.INSTANCE.getServerDirection();
        return direction != null ? direction.yaw() : original;
    }

    @ModifyExpressionValue(method = {"sendMovementPackets", "tick"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getPitch()F"))
    private float interceptPitch(float original) {
        ViewDirection direction = AimManager.INSTANCE.getServerDirection();
        return direction != null ? direction.pitch() : original;
    }
}