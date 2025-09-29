package win.blade.mixin.minecraft.entity;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import win.blade.common.utils.minecraft.MinecraftInstance;
import win.blade.core.Manager;
import win.blade.core.event.controllers.EventHolder;
import win.blade.core.event.impl.player.KeepSprintEvent;
import win.blade.core.event.impl.player.PlayerActionEvents;

@SuppressWarnings("all")
@Mixin(PlayerEntity.class)
public class MixinPlayerEntity implements MinecraftInstance {


    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    private void onAttack(Entity target, CallbackInfo ci) {
        if ((Object) this != MinecraftClient.getInstance().player) {
            return;
        }
        final PlayerActionEvents.Attack event = EventHolder.getAttackEvent(target, false);
        Manager.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;setSprinting(Z)V", shift = At.Shift.AFTER))
    public void attackHook(CallbackInfo callbackInfo) {
        Manager.EVENT_BUS.post(new KeepSprintEvent());
    }
}
