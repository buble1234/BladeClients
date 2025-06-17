package win.blade.common.utils.player;

import net.minecraft.entity.effect.StatusEffects;
import win.blade.common.utils.minecraft.MinecraftInstance;

public class SprintUtility implements MinecraftInstance {

    private static boolean emergencyStop = false;
    private static boolean wasSprinting = false;
    private static final boolean keepSprint = true;

    public static boolean canStartSprinting() {
        if (mc.player == null) return false;

        return (mc.player.input.movementForward > 0 || mc.player.isSwimming()) &&
                !mc.player.hasStatusEffect(StatusEffects.BLINDNESS) && !mc.player.isGliding() &&
                !mc.player.hasVehicle() && mc.player.getHungerManager().getFoodLevel() > 6;
    }

    public static boolean canSprint() {
        if (mc.player == null) return false;

        return mc.player.getHungerManager().getFoodLevel() > 6 || mc.player.isSwimming() || mc.player.getAbilities().allowFlying;
    }

    public static boolean isEmergencyStop() {
        return emergencyStop;
    }

    public static void setEmergencyStop(boolean emergencyStop) {
        SprintUtility.emergencyStop = emergencyStop;
    }

    public static boolean isWasSprinting() {
        return wasSprinting;
    }

    public static void setWasSprinting(boolean wasSprinting) {
        SprintUtility.wasSprinting = wasSprinting;
    }

    public static boolean isKeepSprint() {
        return keepSprint;
    }
}