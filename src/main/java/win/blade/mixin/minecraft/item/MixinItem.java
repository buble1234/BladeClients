package win.blade.mixin.minecraft.item;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import win.blade.common.utils.rotation.core.ViewDirection;
import win.blade.common.utils.rotation.manager.AimManager;

@Mixin(Item.class)
public class MixinItem {

    @Redirect(
            method = "raycast",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;getYaw()F")
    )
    private static float interceptRaycastYaw(PlayerEntity player) {
        if (player != MinecraftClient.getInstance().player) {
            return player.getYaw();
        }

        ViewDirection direction = AimManager.INSTANCE.getCurrentDirection();
        return direction != null ? direction.yaw() : player.getYaw();
    }

    @Redirect(
            method = "raycast",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;getPitch()F")
    )
    private static float interceptRaycastPitch(PlayerEntity player) {
        if (player != MinecraftClient.getInstance().player) {
            return player.getPitch();
        }

        ViewDirection direction = AimManager.INSTANCE.getCurrentDirection();
        return direction != null ? direction.pitch() : player.getPitch();
    }
}