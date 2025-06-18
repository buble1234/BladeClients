package win.blade.mixin.minecraft.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import win.blade.common.utils.rotation.core.ViewDirection;
import win.blade.common.utils.rotation.manager.AimManager;

@Mixin(LivingEntityRenderer.class)
public class MixinLivingEntityRenderer<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>> {

    @Inject(method = "updateRenderState(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;F)V", at = @At("TAIL"))
    private void updateVisualRotation(T livingEntity, S livingEntityRenderState, float tickDelta, CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();

        if (mc.player == null || livingEntity != mc.player) return;

        AimManager manager = AimManager.INSTANCE;

        if (!manager.isEnabled()) return;

        ViewDirection currentDirection = manager.getCurrentDirection();
        ViewDirection previousDirection = manager.getPreviousDirection();

        if (currentDirection != null && previousDirection != null) {
            float renderYaw = MathHelper.lerpAngleDegrees(tickDelta, previousDirection.yaw(), currentDirection.yaw());
            float renderPitch = MathHelper.lerp(tickDelta, previousDirection.pitch(), currentDirection.pitch());

            livingEntityRenderState.bodyYaw = renderYaw;
            livingEntityRenderState.yawDegrees = 0;
            livingEntityRenderState.pitch = renderPitch;

            if (livingEntityRenderState instanceof PlayerEntityRenderState playerState) {
                //playerState.headYaw = renderYaw;
            }
        }
    }
}