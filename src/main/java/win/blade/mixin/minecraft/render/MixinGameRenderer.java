package win.blade.mixin.minecraft.render;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.ProjectionType;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameOverlayRenderer;
import net.minecraft.client.render.*;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.profiler.Profilers;
import net.minecraft.world.GameMode;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Quaternionf;
import org.joml.Vector3f;
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
import win.blade.common.utils.shader.framebuffers.MaskedBlurFramebuffer;
import win.blade.core.Manager;
import win.blade.core.event.controllers.EventHolder;
import win.blade.core.event.impl.render.FovEvent;
import win.blade.core.event.impl.render.RenderCancelEvents;
import win.blade.core.event.impl.render.WorldChangeEvent;
import win.blade.core.module.storage.render.AspectRatioModule;
import win.blade.core.module.storage.render.HandsModule;

@Mixin(GameRenderer.class)
public abstract class MixinGameRenderer implements MinecraftInstance {

    @Shadow
    @Final
    private Camera camera;

    @Shadow public abstract float getFarPlaneDistance();

//    @Redirect(method = "renderWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/GameRenderer;renderHand(Lnet/minecraft/client/render/Camera;FLorg/joml/Matrix4f;)V"))
//    private void redirectRenderHand(GameRenderer gameRenderer, Camera camera, float tickDelta, Matrix4f matrix4f) {
//        if (Manager.getModuleManagement().get(HandsModule.class).isEnabled()) {
//            RenderSystem.colorMask(false, false, false, false);
//
//            gameRenderer.renderHand(camera, tickDelta, matrix4f);
//
//            RenderSystem.colorMask(true, true, true, true);
//
//            HandsModule.render(getFarPlaneDistance());
//        } else {
//            gameRenderer.renderHand(camera, tickDelta, matrix4f);
//        }
//    }

    @Shadow private LightmapTextureManager lightmapTextureManager;
    @Shadow @Final private MinecraftClient client;
    @Shadow @Final private HeldItemRenderer firstPersonRenderer;
    @Shadow @Final private BufferBuilderStorage buffers;

    @Shadow private boolean renderingPanorama;
    @Shadow private float zoom;
    @Shadow private float zoomX;
    @Shadow private float zoomY;

    @Shadow public abstract Matrix4f getBasicProjectionMatrix(float fovDegrees);
    @Shadow public abstract float getFov(Camera camera, float tickDelta, boolean changingFov);

    @Shadow abstract void tiltViewWhenHurt(MatrixStack matrices, float tickDelta);
    @Shadow abstract void bobView(MatrixStack matrices, float tickDelta);

    @Inject(method = "renderHand", at = @At("HEAD"), cancellable = true)
    public void renderHandWithBlur(Camera camera, float tickDelta, Matrix4f matrix4f, CallbackInfo ci) {
        if (this.renderingPanorama) {
            return;
        }

        ci.cancel();

        boolean bl = this.client.getCameraEntity() instanceof LivingEntity && ((LivingEntity)this.client.getCameraEntity()).isSleeping();
        if (this.client.options.getPerspective().isFirstPerson() && !bl && !this.client.options.hudHidden && this.client.interactionManager.getCurrentGameMode() != GameMode.SPECTATOR) {
            MaskedBlurFramebuffer.use(() -> {
                Matrix4f matrix4f2 = this.getBasicProjectionMatrix(this.getFov(camera, tickDelta, false));
                RenderSystem.setProjectionMatrix(matrix4f2, ProjectionType.PERSPECTIVE);

                MatrixStack matrixStack = new MatrixStack();
                matrixStack.push();
                matrixStack.multiplyPositionMatrix(matrix4f.invert(new Matrix4f()));

                Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
                matrix4fStack.pushMatrix().mul(matrix4f);

                this.tiltViewWhenHurt(matrixStack, tickDelta);
                if ((Boolean)this.client.options.getBobView().getValue()) {
                    this.bobView(matrixStack, tickDelta);
                }

                this.lightmapTextureManager.enable();
                this.firstPersonRenderer.renderItem(tickDelta, matrixStack, this.buffers.getEntityVertexConsumers(), this.client.player, this.client.getEntityRenderDispatcher().getLight(this.client.player, tickDelta));
                this.lightmapTextureManager.disable();

                matrix4fStack.popMatrix();
                matrixStack.pop();
            });

            MaskedBlurFramebuffer.draw(15, 15.0f);

            Matrix4f mainMatrix4f2 = this.getBasicProjectionMatrix(this.getFov(camera, tickDelta, false));
            RenderSystem.setProjectionMatrix(mainMatrix4f2, ProjectionType.PERSPECTIVE);

            if (this.client.options.getPerspective().isFirstPerson() && !bl) {
                MatrixStack overlayStack = new MatrixStack();
                VertexConsumerProvider.Immediate immediate = this.buffers.getEntityVertexConsumers();
                InGameOverlayRenderer.renderOverlays(this.client, overlayStack, immediate);
                immediate.draw();
            }
        }
    }

    @Inject(method = "renderWorld", at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/GameRenderer;renderHand:Z", opcode = Opcodes.GETFIELD, ordinal = 0))
    public void hookWorldRender(RenderTickCounter tickCounter, CallbackInfo ci, @Local(ordinal = 2) Matrix4f matrix4f2) {
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

    private ClientWorld lastWorld = null;

    @Inject(method = "render", at = @At("HEAD"))
    private void onFrameStart(CallbackInfo ci) {
        ShaderHelper.initShadersIfNeeded();
        ShaderHelper.checkFramebuffers();

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