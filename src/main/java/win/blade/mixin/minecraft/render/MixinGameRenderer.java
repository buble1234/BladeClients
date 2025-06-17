package win.blade.mixin.minecraft.render;

import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import win.blade.core.Manager;
import win.blade.core.event.controllers.EventHolder;

@Mixin(GameRenderer.class)
public abstract class MixinGameRenderer {

//    @Shadow
//    @Final
//    private Camera camera;
//
//    @Inject(method = "renderWorld", at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/GameRenderer;renderHand:Z", opcode = org.objectweb.asm.Opcodes.GETFIELD))
//    public void onRenderWorld(RenderTickCounter renderTickCounter, CallbackInfo ci) {
//        MatrixStack matrices = new MatrixStack();
//
//        Manager.EVENT_BUS.post(EventHolder.getWorldRenderEvent(matrices, this.camera, renderTickCounter.getTickDelta(false)));
//    }
}