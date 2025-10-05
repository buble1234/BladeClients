package win.blade.mixin.minecraft.client;

import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.minecraft.world.RaycastContext;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import win.blade.common.utils.minecraft.MinecraftInstance;
import win.blade.common.utils.aim.core.ViewDirection;
import win.blade.common.utils.aim.manager.AimManager;
import win.blade.core.Manager;
import win.blade.core.event.impl.render.CameraClipEvent;
import win.blade.core.module.storage.player.FreeCam;

import java.util.Optional;

@Mixin(Camera.class)
public abstract class MixinCamera implements MinecraftInstance {

    @Shadow
    protected abstract void setRotation(float yaw, float pitch);

    @Inject(method = "update", at = @At(value = "TAIL"), cancellable = true)
    private void onCameraUpdate(BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci) {
        var manager = AimManager.INSTANCE;
        var task = manager.getActiveTask();

        if (task == null || !task.settings().enableViewSync()) {
            return;
        }

        if (manager.shouldInterpolate()) {
            ViewDirection current = manager.getCurrentDirection();
            ViewDirection previous = manager.getPreviousDirection();

            float deltaYaw = current.yaw() - previous.yaw();
            deltaYaw = ((deltaYaw % 360) + 540) % 360 - 180;

            // -370 = -10 + 540 = -530 % 360 = -170 - 180 = -350
            // 370 = 10 + 540 = 550 % 360 = 180 - 180 = 0

            float yaw = previous.yaw() + deltaYaw * tickDelta;
            yaw = MathHelper.wrapDegrees(yaw);

            float pitch = MathHelper.lerp(tickDelta, previous.pitch(), current.pitch());
            pitch = MathHelper.clamp(pitch, -90f, 90f);

            setRotation(yaw, pitch);

            if (focusedEntity != null) {
                focusedEntity.setYaw(yaw);
                focusedEntity.setPitch(pitch);
                focusedEntity.prevYaw = yaw;
                focusedEntity.prevPitch = pitch;
            }
        }
    }

    @Inject(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;setPos(DDD)V", shift = At.Shift.AFTER))
    private void hookFreeCamModifiedPosition(BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci) {
        Optional<FreeCam> freeCamOpt = Manager.getModuleManagement().find(FreeCam.class);
        if (freeCamOpt.isPresent() && freeCamOpt.get().isEnabled()) {
            freeCamOpt.get().applyCameraPosition((Camera) (Object) this, focusedEntity, tickDelta);
        }
    }

    @Shadow
    private Vec3d pos;
    @Shadow
    private BlockView area;
    @Shadow
    private Entity focusedEntity;
    @Shadow
    private Vector3f horizontalPlane;

    @Inject(method = "clipToSpace", at = @At("HEAD"), cancellable = true)
    private void clipToSpace(float cameraDist, CallbackInfoReturnable<Float> cir) {
        CameraClipEvent e = new CameraClipEvent(cameraDist, true);
        Manager.EVENT_BUS.post(e);

        if (!e.getRaytrace()) {
            cir.setReturnValue(e.getDistance());
            return;
        }

        float distance = e.getDistance();

        for(int i = 0; i < 8; ++i) {
            float h = (float)((i & 1) * 2 - 1);
            float j = (float)((i >> 1 & 1) * 2 - 1);
            float k = (float)((i >> 2 & 1) * 2 - 1);

            Vec3d vec3d = this.pos.add((double)(h * 0.1F), (double)(j * 0.1F), (double)(k * 0.1F));
            Vec3d vec3d2 = vec3d.add((new Vec3d(this.horizontalPlane)).multiply((double)(-distance)));
            HitResult hitResult = this.area.raycast(new RaycastContext(vec3d, vec3d2, RaycastContext.ShapeType.VISUAL, RaycastContext.FluidHandling.NONE, this.focusedEntity));

            if (hitResult.getType() != HitResult.Type.MISS) {
                float l = (float)hitResult.getPos().squaredDistanceTo(this.pos);
                if (l < MathHelper.square(distance)) {
                    distance = MathHelper.sqrt(l);
                }
            }
        }

        cir.setReturnValue(distance);
    }
}