package win.blade.core.event.impl.player;


import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import win.blade.core.event.controllers.Event;

public final class MotionEvent extends Event {
    private double x, y, z;
    private float  yaw, pitch;
    private boolean onGround;
    private boolean sneaking;
    private boolean sprinting;

    private static final MinecraftClient MC = MinecraftClient.getInstance();

    public MotionEvent(double x, double y, double z,
                       float  yaw, float  pitch,
                       boolean onGround,
                       boolean sneaking,
                       boolean sprinting) {
        this.x          = x;
        this.y          = y;
        this.z          = z;
        this.yaw        = yaw;
        this.pitch      = pitch;
        this.onGround   = onGround;
        this.sneaking   = sneaking;
        this.sprinting  = sprinting;
    }

    public double  getX()          { return x; }
    public double  getY()          { return y; }
    public double  getZ()          { return z; }
    public float   getYaw()        { return yaw; }
    public float   getPitch()      { return pitch; }
    public boolean isOnGround()    { return onGround; }
    public boolean isSneaking()    { return sneaking; }
    public boolean isSprinting()   { return sprinting; }

    public void setX(double x)               { this.x = x; }
    public void setY(double y)               { this.y = y; }
    public void setZ(double z)               { this.z = z; }
    public void setOnGround(boolean ground)  { this.onGround = ground; }
    public void setSneaking(boolean sneak)   { this.sneaking = sneak; }
    public void setSprinting(boolean sprint) { this.sprinting = sprint; }

    public void setYaw(float yaw) {
        this.yaw = yaw;

        ClientPlayerEntity p = MC.player;
        if (p != null) {
            p.setYaw(yaw);
            p.setHeadYaw(yaw);
            p.setBodyYaw(yaw);
        }
    }

    /** Обновляет pitch на клиенте. */
    public void setPitch(float pitch) {
        this.pitch = pitch;

        ClientPlayerEntity p = MC.player;
        if (p != null) {
            p.setPitch(pitch);
        }
    }
}
