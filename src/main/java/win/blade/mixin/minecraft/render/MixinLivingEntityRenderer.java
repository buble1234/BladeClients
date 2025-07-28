package win.blade.mixin.minecraft.render;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import win.blade.common.utils.minecraft.MinecraftInstance;
import win.blade.common.utils.aim.core.ViewDirection;
import win.blade.common.utils.aim.manager.AimManager;
import win.blade.core.Manager;
import win.blade.core.module.storage.misc.SeeInvisiblesModule;

@Mixin(LivingEntityRenderer.class)
public abstract class MixinLivingEntityRenderer<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>> implements MinecraftInstance {

    @Shadow protected abstract boolean isVisible(S state);

    @Inject(method = "updateRenderState(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;F)V", at = @At("TAIL"))
    private void updateVisualRotation(T livingEntity, S livingEntityRenderState, float tickDelta, CallbackInfo ci) {
        if (mc.player == null || livingEntity != mc.player) return;

        AimManager manager = AimManager.INSTANCE;
        if (!manager.isEnabled()) return;

        ViewDirection currentDirection = manager.getCurrentDirection();
        ViewDirection previousDirection = manager.getPreviousDirection();

        if (currentDirection != null && previousDirection != null) {
            float renderYaw = MathHelper.lerpAngleDegrees(tickDelta, previousDirection.yaw(), currentDirection.yaw());
            float renderPitch = MathHelper.lerpAngleDegrees(tickDelta, previousDirection.pitch(), currentDirection.pitch());

            renderYaw = MathHelper.wrapDegrees(renderYaw);
            renderPitch = MathHelper.clamp(renderPitch, -90f, 90f);

            livingEntityRenderState.bodyYaw = renderYaw;
            livingEntityRenderState.yawDegrees = 0;
            livingEntityRenderState.pitch = renderPitch;
        }
    }

    @Redirect(
            method = "render(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/model/EntityModel;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;III)V")
    )
    private void onRenderModel(M model, MatrixStack matrices, VertexConsumer vertices, int light, int overlay, int color, S livingEntityRenderState) {
        int finalColor = color;

        boolean isCurrentlyVisible = isVisible(livingEntityRenderState);
        boolean isTranslucent = !isCurrentlyVisible && !livingEntityRenderState.invisibleToPlayer;

        if (isTranslucent) {
            SeeInvisiblesModule seeInvisibles = Manager.getModuleManagement().get(SeeInvisiblesModule.class);
            if (seeInvisibles != null && seeInvisibles.isEnabled()) {
                float alpha = seeInvisibles.alpha.getValue();
                int alphaInt = (int)(alpha * 255.0f);

                int rgb = finalColor & 0x00FFFFFF;
                finalColor = (alphaInt << 24) | rgb;
            }
        }

        model.render(matrices, vertices, light, overlay, finalColor);
    }
}