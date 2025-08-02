package win.blade.mixin.minecraft.render;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.profiler.Profilers;
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

import java.util.AbstractMap;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

@Mixin(LivingEntityRenderer.class)
public abstract class MixinLivingEntityRenderer<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>> implements MinecraftInstance {

    @Shadow protected abstract boolean isVisible(S state);

    private final Map<UUID, Float> prevBodyYawStorage = new WeakHashMap<>();
    private final Map<UUID, Float> bodyYawStorage = new WeakHashMap<>();
    private long lastTick = -1;


    @Inject(method = "updateRenderState(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;F)V", at = @At("TAIL"))
    private void updateVisualRotation(T livingEntity, S livingEntityRenderState, float tickDelta, CallbackInfo ci) {
        if (mc.player == null || livingEntity != mc.player) return;

        AimManager manager = AimManager.INSTANCE;
        if (!manager.isEnabled()) return;

        ViewDirection currentDirection = manager.getCurrentDirection();
        ViewDirection previousDirection = manager.getPreviousDirection();

        if (currentDirection != null && previousDirection != null) {
            long currentTick = livingEntity.getWorld().getTime();

            if (this.lastTick != currentTick) {
                this.lastTick = currentTick;

                float lastTickBodyYaw = bodyYawStorage.getOrDefault(livingEntity.getUuid(), livingEntity.bodyYaw);
                prevBodyYawStorage.put(livingEntity.getUuid(), lastTickBodyYaw);

                float bodyYawForCurrentTick = lastTickBodyYaw;
                float headYaw = currentDirection.yaw();
                float targetBodyYaw = bodyYawForCurrentTick;

                double deltaX = livingEntity.getX() - livingEntity.prevX;
                double deltaZ = livingEntity.getZ() - livingEntity.prevZ;
                float moveDistanceSq = (float) (deltaX * deltaX + deltaZ * deltaZ);

                if (moveDistanceSq > 0.0025000002F) {
                    float moveAngle = (float) MathHelper.atan2(deltaZ, deltaX) * 57.295776F - 90.0F;
                    float angleDiff = MathHelper.abs(MathHelper.wrapDegrees(headYaw) - moveAngle);

                    if (95.0F < angleDiff && angleDiff < 265.0F) {
                        targetBodyYaw = moveAngle - 180.0F;
                    } else {
                        targetBodyYaw = moveAngle;
                    }
                }

                if (livingEntity.handSwingProgress > 0.0F) {
                    targetBodyYaw = headYaw;
                }

                float bodyYawDelta = MathHelper.wrapDegrees(targetBodyYaw - bodyYawForCurrentTick);
                bodyYawForCurrentTick += bodyYawDelta * 0.3F;

                float headBodyDiff = MathHelper.wrapDegrees(headYaw - bodyYawForCurrentTick);
                if (Math.abs(headBodyDiff) > 50) {
                    bodyYawForCurrentTick += headBodyDiff - (MathHelper.sign(headBodyDiff) * 50);
                }

                bodyYawStorage.put(livingEntity.getUuid(), bodyYawForCurrentTick);
            }

            float prevBodyYaw = prevBodyYawStorage.getOrDefault(livingEntity.getUuid(), livingEntity.bodyYaw);
            float currentTickBodyYaw = bodyYawStorage.getOrDefault(livingEntity.getUuid(), livingEntity.bodyYaw);
            float interpolatedBodyYaw = MathHelper.lerpAngleDegrees(tickDelta, prevBodyYaw, currentTickBodyYaw);

            float renderYaw = MathHelper.lerpAngleDegrees(tickDelta, previousDirection.yaw(), currentDirection.yaw());
            float renderPitch = MathHelper.lerpAngleDegrees(tickDelta, previousDirection.pitch(), currentDirection.pitch());

            renderYaw = MathHelper.wrapDegrees(renderYaw);
            renderPitch = MathHelper.clamp(renderPitch, -90f, 90f);

            livingEntityRenderState.bodyYaw = renderYaw;
            livingEntityRenderState.yawDegrees = 0;
            livingEntityRenderState.bodyYaw = interpolatedBodyYaw;
            livingEntityRenderState.yawDegrees = MathHelper.wrapDegrees(renderYaw - interpolatedBodyYaw);
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