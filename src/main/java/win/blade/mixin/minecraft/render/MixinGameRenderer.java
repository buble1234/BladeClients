package win.blade.mixin.minecraft.render;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import win.blade.common.utils.math.MathUtility;
import win.blade.common.utils.minecraft.MinecraftInstance;
import win.blade.core.Manager;
import win.blade.core.event.controllers.EventHolder;
import win.blade.core.event.impl.render.RenderCancelEvents;

@Mixin(GameRenderer.class)
public abstract class MixinGameRenderer implements MinecraftInstance {

    @Shadow
    @Final
    private Camera camera;

    @Inject(method = "renderWorld", at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/GameRenderer;renderHand:Z", opcode = Opcodes.GETFIELD, ordinal = 0))
    public void hookWorldRender(RenderTickCounter tickCounter, CallbackInfo ci, @Local(ordinal = 2) Matrix4f matrix4f2) {
        // TODO: Исправить это
        var newMatStack = new MatrixStack();

        newMatStack.multiplyPositionMatrix(matrix4f2);

        Manager.EVENT_BUS.post(EventHolder.getWorldRenderEvent(newMatStack, this.camera, tickCounter.getTickDelta(false)));


        MathUtility.lastProjMat.set(RenderSystem.getProjectionMatrix());
        MathUtility.lastModMat.set(RenderSystem.getModelViewMatrix());
        MathUtility.lastWorldSpaceMatrix.set(newMatStack.peek().getPositionMatrix());
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;draw()V", ordinal = 1, shift = At.Shift.AFTER))
    private void hookScreenRender(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci, @Local DrawContext drawContext) {
        Manager.EVENT_BUS.post(EventHolder.getScreenRenderEvent(drawContext.getMatrices(), tickCounter.getTickDelta(false), drawContext));
    }

    // TODO: Не обязательно
//    @Redirect(method = "findCrosshairTarget", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;getRotationVec(F)Lnet/minecraft/util/math/Vec3d;"))
//    private Vec3d interceptCrosshairRotation(Entity entity, float tickDelta) {
//        if (entity instanceof ClientPlayerEntity) {
//            AimManager manager = AimManager.INSTANCE;
//            ViewDirection direction = manager.getCurrentDirection();
//
//            if (direction != null && manager.isEnabled()) {
//                return direction.asVector();
//            }
//        }
//
//        return entity.getRotationVec(tickDelta);
//    }

    @Inject(method = "tiltViewWhenHurt", at = @At("HEAD"), cancellable = true)
    private void onTiltViewWhenHurt(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        RenderCancelEvents.CameraShake event = new RenderCancelEvents.CameraShake();
        Manager.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }
}