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

@Mixin(LivingEntityRenderer.class)
public abstract class MixinLivingEntityRenderer<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>> implements MinecraftInstance {

    @Shadow protected abstract boolean isVisible(S state);

    @Inject(method = "updateRenderState(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;F)V",
            at = @At("HEAD"),
            cancellable = true)
    private void updateVisualRotation(T livingEntity, S livingEntityRenderState, float tickDelta, CallbackInfo ci) {
        // Убеждаемся, что это наш игрок
        if (mc.player == null || livingEntity != mc.player) return;

        AimManager manager = AimManager.INSTANCE;
        if (!manager.isEnabled() || !manager.shouldInterpolate()) {
            return;
        }

        // Даем оригинальному методу заполнить все поля, КРОМЕ ротаций
        // Сначала вызываем его, потом переписываем
        // Это более совместимый подход, чем HEAD + cancel
    }

    // Правильнее будет оставить TAIL, но исправить логику. Давайте вернемся к этому.
    // Проблема в том, что HEAD + cancel требует копирования всего метода.
    // Давайте починим ваш TAIL инжект.

    @Inject(method = "updateRenderState(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;F)V", at = @At("TAIL"))
    private void overrideVisualRotation_atTail(T livingEntity, S livingEntityRenderState, float tickDelta, CallbackInfo ci) {
        if (mc.player == null || livingEntity != mc.player) return;

        AimManager manager = AimManager.INSTANCE;
        if (!manager.isEnabled() || !manager.shouldInterpolate()) return;

        // Оригинальный метод уже отработал. Теперь мы просто переписываем его результаты своими.
        ViewDirection currentDirection = manager.getCurrentDirection();
        ViewDirection previousDirection = manager.getPreviousDirection();
        Map.Entry<Vec3d, Vec3d> rotationInfo = manager.rotationInfo;

        Vec3d prevVec = rotationInfo.getKey();
        Vec3d newVec = rotationInfo.getValue();

        float renderHeadYaw = (float) MathHelper.lerpAngleDegrees(tickDelta, prevVec.x, newVec.x);
        float renderBodyYaw = (float) MathHelper.lerpAngleDegrees(tickDelta, prevVec.y, newVec.y);
        float renderPitch = MathHelper.lerpAngleDegrees(tickDelta, previousDirection.pitch(), currentDirection.pitch());

        // Переписываем значения, рассчитанные ванильным кодом
        livingEntityRenderState.bodyYaw = renderBodyYaw;
        livingEntityRenderState.yawDegrees = MathHelper.wrapDegrees(renderHeadYaw - renderBodyYaw);
        livingEntityRenderState.pitch = renderPitch;
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