package win.blade.core.module.storage.player;

import net.minecraft.client.option.Perspective;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d;
import win.blade.common.gui.impl.gui.setting.implement.BooleanSetting;
import win.blade.common.gui.impl.gui.setting.implement.ValueSetting;
import win.blade.core.Manager;
import win.blade.core.event.controllers.EventHandler;
import win.blade.core.event.impl.network.PacketEvent;
import win.blade.core.event.impl.player.MotionEvent;
import win.blade.core.module.api.Category;
import win.blade.core.module.api.Module;
import win.blade.core.module.api.ModuleInfo;
import win.blade.common.utils.aim.manager.AimManager;
import win.blade.mixin.accessor.CameraAccessor;

@ModuleInfo(name = "FreeCam", category = Category.PLAYER, desc = "Позволяет летать камерой отдельно от тела.")
public class FreeCam extends Module {

    private final ValueSetting speed = new ValueSetting("Скорость", "").setValue(1f).range(0.1f, 5f);
    public final BooleanSetting cameraInteract = new BooleanSetting("Взаимодействие", "Позволяет ломать/ставить блоки с позиции камеры.").setValue(true);

    private CameraPosition cameraPosition;
    private Vec3d originalPosition;
    private float originalYaw;
    private float originalPitch;

    private static class CameraPosition {
        private Vec3d currentPos;
        private Vec3d lastPos;

        public CameraPosition(Vec3d startPos) {
            this.currentPos = startPos;
            this.lastPos = startPos;
        }

        public void update(Vec3d velocity) {
            this.lastPos = this.currentPos;
            this.currentPos = this.currentPos.add(velocity);
        }

        public Vec3d interpolate(float tickDelta) {
            return lastPos.lerp(currentPos, tickDelta);
        }

        public Vec3d getPos() {
            return currentPos;
        }
    }

    public FreeCam() {
        addSettings(speed, cameraInteract);
    }

    @Override
    public void onEnable() {
        if (mc.player == null) {
            this.toggle();
            return;
        }

        originalPosition = mc.player.getPos();
        originalYaw = mc.player.getYaw();
        originalPitch = mc.player.getPitch();

        cameraPosition = new CameraPosition(mc.player.getEyePos());

        mc.player.getAbilities().flying = true;
    }

    @Override
    public void onDisable() {
        if (mc.player == null || originalPosition == null) {
            return;
        }

        mc.player.getAbilities().flying = mc.player.isCreative();
        mc.player.noClip = false;

        mc.player.setPos(originalPosition.x, originalPosition.y, originalPosition.z);
        mc.player.setYaw(originalYaw);
        mc.player.setPitch(originalPitch);

        cameraPosition = null;
    }

    @EventHandler
    public void onPlayerMove(MotionEvent event) {
        if (mc.player == null || cameraPosition == null) return;

        mc.player.noClip = true;

        float moveSpeed = speed.getValue();

        mc.player.setVelocity(0, 0, 0);

        double forward = mc.player.input.movementForward;
        double strafe = mc.player.input.movementSideways;
        float yaw = mc.player.getYaw();

        double verticalSpeed = 0;
        if (mc.options.jumpKey.isPressed()) {
            verticalSpeed = moveSpeed;
        }
        if (mc.options.sneakKey.isPressed()) {
            verticalSpeed = -moveSpeed;
        }

        if (forward != 0.0 || strafe != 0.0) {
            if (forward != 0.0 && strafe != 0.0) {
                float angle = yaw + (forward > 0.0f ? 0 : 180) + (strafe > 0.0f ? -90 : 90) * (forward != 0f ? 0.5f : 1f) ;
                double rad = Math.toRadians(angle);
                double motionX = -Math.sin(rad) * moveSpeed;
                double motionZ = Math.cos(rad) * moveSpeed;
                cameraPosition.update(new Vec3d(motionX, verticalSpeed, motionZ));
            } else if (forward != 0.0) {
                float angle = yaw + (forward > 0.0f ? 0 : 180);
                double rad = Math.toRadians(angle);
                double motionX = -Math.sin(rad) * moveSpeed;
                double motionZ = Math.cos(rad) * moveSpeed;
                cameraPosition.update(new Vec3d(motionX, verticalSpeed, motionZ));
            } else {
                float angle = yaw + (strafe > 0.0f ? -90 : 90);
                double rad = Math.toRadians(angle);
                double motionX = -Math.sin(rad) * moveSpeed;
                double motionZ = Math.cos(rad) * moveSpeed;
                cameraPosition.update(new Vec3d(motionX, verticalSpeed, motionZ));
            }
        } else {
            cameraPosition.update(new Vec3d(0, verticalSpeed, 0));
        }

        if (mc.player.age % 10 == 0) {
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.OnGroundOnly( mc.player.isOnGround(), mc.player.horizontalCollision));
        }

        event.setX(0);
        event.setY(0);
        event.setZ(0);
        event.cancel();
    }
    @EventHandler
    public void onPacketSend(PacketEvent.Send event) {
        if (!this.isEnabled()) return;

        if (event.getPacket() instanceof PlayerMoveC2SPacket) {
            if (!(AimManager.INSTANCE.getActiveTask() != null && cameraInteract.getValue())) {
                event.cancel();
            }
        }
    }


    public void applyCameraPosition(net.minecraft.client.render.Camera camera, Entity entity, float tickDelta) {
        if (!isEnabled() || cameraPosition == null || entity != mc.player) {
            return;
        }
        Vec3d interpolatedPos = cameraPosition.interpolate(tickDelta);
        ((CameraAccessor) camera).callSetPos(interpolatedPos.getX(), interpolatedPos.getY(), interpolatedPos.getZ());
    }
    public boolean shouldRenderPlayer(LivingEntity entity) {
        if (!isEnabled() || entity != mc.player) {
            return entity.isSleeping();
        }
        return !mc.gameRenderer.getCamera().isThirdPerson();
    }

    public Vec3d modifyRaycast(Vec3d original, Entity entity) {
        if (!isEnabled() || cameraPosition == null || entity != mc.player || !cameraInteract.getValue()) {
            return original;
        }
        return cameraPosition.getPos();
    }
}