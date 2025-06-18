package win.blade.common.utils.player;

import static win.blade.common.utils.minecraft.MinecraftInstance.mc;

public final class MovementUtility {

    public static boolean isMoving() {
        return mc.player != null && mc.world != null && mc.player.input != null && (mc.player.input.movementForward != 0.0 || mc.player.input.movementSideways != 0.0);
    }

    public static double getSpeed() {
        return Math.hypot(mc.player.getVelocity().x, mc.player.getVelocity().z);
    }
}
