package win.blade.mixin.minecraft.render;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import org.joml.Matrix4f;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import win.blade.common.utils.math.MathUtility;
import win.blade.common.utils.minecraft.MinecraftInstance;
import win.blade.common.utils.render.shader.ShaderHelper;
import win.blade.core.Manager;
import win.blade.core.event.controllers.EventHolder;
import win.blade.core.event.impl.render.FovEvent;
import win.blade.core.event.impl.render.RenderCancelEvents;
import win.blade.core.event.impl.render.WorldChangeEvent;
import win.blade.core.module.storage.render.AspectRatioModule;
import win.blade.core.module.storage.render.FogBlur;

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
        Manager.EVENT_BUS.post(EventHolder.getPOSTScreenRenderEvent(drawContext.getMatrices(), tickCounter.getTickDelta(false), drawContext));
    }

    @Inject(method = "tiltViewWhenHurt", at = @At("HEAD"), cancellable = true)
    private void onTiltViewWhenHurt(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        RenderCancelEvents.CameraShake event = new RenderCancelEvents.CameraShake();
        Manager.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Shadow @Final private MinecraftClient client;

    private ClientWorld lastWorld = null;

    @Inject(method = "render", at = @At("HEAD"))
    private void onFrameStart(CallbackInfo ci) {
        if (this.client.world != this.lastWorld) {
            this.lastWorld = this.client.world;

            Manager.EVENT_BUS.post(new WorldChangeEvent());
        }
    }

    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
    private void onGetFov(Camera camera, float tickDelta, boolean changingFov, CallbackInfoReturnable<Float> cir) {
        FovEvent event = new FovEvent(cir.getReturnValue());
        Manager.EVENT_BUS.post(event);
        cir.setReturnValue(event.getFov());
    }

    @Shadow private float zoom;
    @Shadow private float zoomX;
    @Shadow private float zoomY;

    @Inject(method = "getBasicProjectionMatrix(F)Lorg/joml/Matrix4f;", at = @At("TAIL"), cancellable = true)
    private void onGetBasicProjectionMatrix(float fov, CallbackInfoReturnable<Matrix4f> cir) {
        AspectRatioModule module = Manager.getModuleManagement().get(AspectRatioModule.class);

        if (!module.isEnabled()) {
            return;
        }

        float aspectRatioValue;
        String mode = module.multiplier.getSelected();

        switch (mode) {
            case "16:9":
                aspectRatioValue = 16.0f / 9.0f;
                break;
            case "16:10":
                aspectRatioValue = 16.0f / 10.0f;
                break;
            case "21:9":
                aspectRatioValue = 21.0f / 9.0f;
                break;
            case "4:3":
                aspectRatioValue = 4.0f / 3.0f;
                break;
            case "Кастомное":
                aspectRatioValue = module.customRatio.getValue();
                break;
            default:
                if (this.client.getWindow().getFramebufferHeight() == 0) return;
                aspectRatioValue = (float) this.client.getWindow().getFramebufferWidth() / (float) this.client.getWindow().getFramebufferHeight();
                break;
        }

        if (aspectRatioValue <= 0) return;

        Matrix4f perspectiveMatrix = new Matrix4f();
        perspectiveMatrix.setPerspective(
                (float) Math.toRadians(fov),
                aspectRatioValue,
                0.05F,
                this.client.gameRenderer.getFarPlaneDistance()
        );

        Matrix4f zoomMatrix = new Matrix4f().identity();
        if (this.zoom != 1.0F) {
            zoomMatrix.translate(this.zoomX, -this.zoomY, 0.0F);
            zoomMatrix.scale(this.zoom, this.zoom, 1.0F);
        }

        zoomMatrix.mul(perspectiveMatrix);
        cir.setReturnValue(zoomMatrix);
    }




}