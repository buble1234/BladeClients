package win.blade.mixin.minecraft.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import win.blade.common.gui.impl.gui.setting.Setting;
import win.blade.common.gui.impl.gui.setting.implement.BooleanSetting;
import win.blade.common.utils.minecraft.MinecraftInstance;
import win.blade.common.utils.aim.core.ViewDirection;
import win.blade.common.utils.aim.manager.AimManager;
import win.blade.common.utils.aim.manager.TargetTask;
import win.blade.core.Manager;
import win.blade.core.event.controllers.EventHolder;
import win.blade.core.event.impl.player.MotionEvent;
import win.blade.core.event.impl.player.PlayerActionEvents;
import win.blade.core.module.storage.move.NoPushModule;
import win.blade.core.module.storage.player.FreeCam;

import java.util.Optional;

@Mixin(value = ClientPlayerEntity.class)
public abstract class MixinClientPlayerEntity extends AbstractClientPlayerEntity implements MinecraftInstance {

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

    @Inject(method = "pushOutOfBlocks", at = @At("HEAD"), cancellable = true)
    private void onPushOutOfBlocks(double x, double d, CallbackInfo info) {
        Optional<NoPushModule> noPushOpt = Manager.getModuleManagement().find(NoPushModule.class);

        if (noPushOpt.isPresent() && noPushOpt.get().isEnabled()) {
            NoPushModule noPush = noPushOpt.get();

            Setting blockSetting = noPush.options.getSubSetting("Блоков");

            if (blockSetting instanceof BooleanSetting && ((BooleanSetting) blockSetting).getValue()) {
                info.cancel();
            }
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onPlayerTick(CallbackInfo ci) {
        if (mc.player == null || mc.world == null) return;
        Manager.EVENT_BUS.post(EventHolder.getPlayerUpdateEvent());
        AimManager.INSTANCE.tick();
    }

    @Redirect(method = "sendMovementPackets", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getYaw()F"))
    private float redirectYawInMovementPackets(ClientPlayerEntity instance) {
        AimManager manager = AimManager.INSTANCE;
        ViewDirection direction = manager.getCurrentDirection();
        TargetTask task = manager.getActiveTask();

        if (manager.isEnabled() && direction != null && task != null) {
            return direction.yaw();
        }
        return instance.getYaw();
    }

    @Redirect(method = "sendMovementPackets", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getPitch()F"))
    private float redirectPitchInMovementPackets(ClientPlayerEntity instance) {
        AimManager manager = AimManager.INSTANCE;
        ViewDirection direction = manager.getCurrentDirection();
        TargetTask task = manager.getActiveTask();

        if (manager.isEnabled() && direction != null && task != null) {
            return direction.pitch();
        }
        return instance.getPitch();
    }

    @ModifyExpressionValue(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getYaw()F"))
    private float modifyTickYaw(float original) {
        AimManager manager = AimManager.INSTANCE;
        ViewDirection direction = manager.getCurrentDirection();
        TargetTask task = manager.getActiveTask();

        if (manager.isEnabled() && direction != null && task != null) {
            return direction.yaw();
        }
        return original;
    }

    @ModifyExpressionValue(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getPitch()F"))
    private float modifyTickPitch(float original) {
        AimManager manager = AimManager.INSTANCE;
        ViewDirection direction = manager.getCurrentDirection();
        TargetTask task = manager.getActiveTask();

        if (manager.isEnabled() && direction != null && task != null) {
            return direction.pitch();
        }
        return original;
    }

    @ModifyExpressionValue(method = "tickMovement", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/player/PlayerAbilities;allowFlying:Z"))
    private boolean hookFreeCamPreventCreativeFly(boolean original) {
        Optional<FreeCam> freeCamOpt = Manager.getModuleManagement().find(FreeCam.class);
        if (freeCamOpt.isPresent() && freeCamOpt.get().isEnabled()) {
            return true;
        }
        return original;
    }

    @Shadow
    public net.minecraft.client.input.Input input;

    @Inject(
            method = "sendMovementPackets",
            at = @At("HEAD"),
            cancellable = true
    )
    private void blade$preMotion(CallbackInfo ci) {
        ClientPlayerEntity self = (ClientPlayerEntity)(Object)this;

        double  x   = self.getX();
        double  y   = self.getY();
        double  z   = self.getZ();
        float   yaw = self.getYaw();
        float   pitch = self.getPitch();

        boolean onGround  = self.isOnGround();
        boolean sneaking  = this.input != null && this.input.playerInput.sneak();
        boolean sprinting = self.isSprinting();

        MotionEvent event = new MotionEvent(
                x, y, z,
                yaw, pitch,
                onGround,
                sneaking,
                sprinting);

        Manager.EVENT_BUS.post(event);

        if (event.isCancelled()) {
            ci.cancel();
            return;
        }

        if (event.getX() != x || event.getY() != y || event.getZ() != z) {
            self.setPosition(event.getX(), event.getY(), event.getZ());
        }

        if (event.getYaw() != yaw)   event.setYaw(event.getYaw());
        if (event.getPitch() != pitch) self.setPitch(event.getPitch());

        if (event.isSprinting() != sprinting) self.setSprinting(event.isSprinting());
//        if (event.isSneaking()  != sneaking  && this.input != null)
//            this.input.sneaking = event.isSneaking();
    }

}