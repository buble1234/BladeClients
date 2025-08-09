package win.blade.mixin.minecraft.render;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.*;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import win.blade.core.Manager;
import win.blade.core.event.impl.render.ChunkRenderEvent;
import win.blade.core.event.impl.render.RenderCancelEvents;
import win.blade.core.module.storage.render.ShaderESP;
import win.blade.mixin.accessor.WorldRendererAccessor;

@Mixin(WorldRenderer.class)
public class MixinWorldRenderer implements WorldRendererAccessor {

    @Shadow
    private Frustum frustum;

    @Override
    public Frustum getFrustum() {
        return this.frustum;
    }


    @Inject(method = "renderWeather", at = @At("HEAD"), cancellable = true)
    private void renderWeatherHook(FrameGraphBuilder frameGraphBuilder, Vec3d pos, float tickDelta, Fog fog, CallbackInfo ci) {
        RenderCancelEvents.Weather event = new RenderCancelEvents.Weather();
        Manager.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @WrapOperation(
            method = "renderLayer",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gl/GlUniform;set(FFF)V")
    )
    private void onSetUniform(GlUniform instance, float x, float y, float z, Operation<Void> original,
                              @Local ChunkBuilder.BuiltChunk builtChunk) {

        Vec3d offset = new Vec3d(0, 0, 0);


        ChunkRenderEvent event = new ChunkRenderEvent(builtChunk, offset);
        Manager.EVENT_BUS.post(event);

        Vec3d calculatedOffset = event.getOffset();

        original.call(instance, x + (float) calculatedOffset.getX(), y + (float) calculatedOffset.getY(), z + (float) calculatedOffset.getZ());
    }
}