package win.blade.mixin.minecraft.client;

import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import win.blade.core.Manager;
import win.blade.core.event.block.BlockBreakingEvent;
import win.blade.core.event.controllers.EventHolder;
import win.blade.core.event.impl.input.InputEvents;

import static win.blade.common.utils.minecraft.MinecraftInstance.mc;

@Mixin(ClientPlayerInteractionManager.class)
public class MixinClientPlayerInteractionManager {

    @Inject(method = "clickSlot", at = @At("HEAD"), cancellable = true)
    public void clickSlotHook(int syncId, int slotId, int button, SlotActionType actionType, PlayerEntity player, CallbackInfo ci) {
        if (mc.player == null || mc.world == null) return;
        InputEvents.ClickSlot event = EventHolder.getClickSlotEvent(actionType, slotId, button, syncId);
        Manager.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "updateBlockBreakingProgress", at = @At("HEAD"))
    private void injectBlockBreaking(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        Manager.EVENT_BUS.post(new BlockBreakingEvent(pos, direction));
    }

}