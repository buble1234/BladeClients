package win.blade.common.utils.player;

import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.MathHelper;
import win.blade.common.utils.minecraft.MinecraftInstance;

/**
 * Автор: NoCap
 * Дата создания: 17.06.2025
 */

public class SprintUtility implements MinecraftInstance {

    private static boolean emergencyStop = false;
    private static boolean wasSprinting = false;
    private static final boolean keepSprint = true;
    private static boolean autoSprint = false;
    private static long lastSprintToggle = 0;
    private static final long SPRINT_TOGGLE_DELAY = 50;

    public static boolean canStartSprinting() {
        if (mc.player == null) return false;

        return (mc.player.input.movementForward > 0 || mc.player.isSwimming()) &&
                !mc.player.hasStatusEffect(StatusEffects.BLINDNESS) &&
                !mc.player.isGliding() &&
                !mc.player.hasVehicle() &&
                !emergencyStop &&
                (mc.player.getHungerManager().getFoodLevel() > 6 || mc.player.getAbilities().allowFlying);
    }

    public static boolean canSprint() {
        if (mc.player == null) return false;

        return (mc.player.getHungerManager().getFoodLevel() > 6 || mc.player.isSwimming() || mc.player.getAbilities().allowFlying) &&
                !emergencyStop &&
                !mc.player.hasStatusEffect(StatusEffects.BLINDNESS);
    }

    public static void handleSprint() {
        if (mc.player == null) return;

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastSprintToggle < SPRINT_TOGGLE_DELAY) return;

        boolean shouldSprint = shouldAutoSprint();
        boolean currentlySprinting = mc.player.isSprinting();

        if (shouldSprint && !currentlySprinting && canStartSprinting()) {
            startSprinting();
        } else if (!shouldSprint && currentlySprinting && !keepSprint) {
            stopSprinting();
        }
    }

    private static boolean shouldAutoSprint() {
        if (!autoSprint || mc.player == null) return false;

        return mc.player.input.movementForward > 0 &&
                !mc.player.isSneaking() &&
                !emergencyStop;
    }

    public static void startSprinting() {
        if (mc.player == null) return;

        if (canStartSprinting()) {
            wasSprinting = mc.player.isSprinting();
            mc.player.setSprinting(true);
            lastSprintToggle = System.currentTimeMillis();
        }
    }

    public static void stopSprinting() {
        if (mc.player == null) return;

        wasSprinting = mc.player.isSprinting();
        mc.player.setSprinting(false);
        lastSprintToggle = System.currentTimeMillis();
    }

    public static void toggleSprint() {
        if (mc.player == null) return;

        if (mc.player.isSprinting()) {
            stopSprinting();
        } else {
            startSprinting();
        }
    }

    public static void handleAttackSprint() {
        if (mc.player == null) return;

        boolean currentlySprinting = mc.player.isSprinting();

        if (currentlySprinting) {
            wasSprinting = true;
            mc.player.setSprinting(false);

            if (keepSprint) {
                mc.execute(() -> {
                    if (mc.player != null && canSprint() && wasSprinting) {
                        mc.player.setSprinting(true);
                    }
                });
            }
        }
    }

    public static void resetSprintAfterAttack() {
        if (keepSprint && wasSprinting && mc.player != null && canSprint()) {
            mc.player.setSprinting(true);
        }
        wasSprinting = false;
    }

    public static boolean isEmergencyStop() {
        return emergencyStop;
    }

    public static void setEmergencyStop(boolean emergencyStop) {
        SprintUtility.emergencyStop = emergencyStop;
        if (emergencyStop && mc.player != null) {
            stopSprinting();
        }
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

    public static boolean isAutoSprint() {
        return autoSprint;
    }

    public static void setAutoSprint(boolean autoSprint) {
        SprintUtility.autoSprint = autoSprint;
    }

    public static float getSprintingSpeed() {
        if (mc.player == null) return 1.0f;

        float baseSpeed = 1.0f;

        if (mc.player.hasStatusEffect(StatusEffects.SPEED)) {
            int amplifier = mc.player.getStatusEffect(StatusEffects.SPEED).getAmplifier();
            baseSpeed *= 1.0f + (amplifier + 1) * 0.2f;
        }

        if (mc.player.hasStatusEffect(StatusEffects.SLOWNESS)) {
            int amplifier = mc.player.getStatusEffect(StatusEffects.SLOWNESS).getAmplifier();
            baseSpeed *= 1.0f - (amplifier + 1) * 0.15f;
        }

        if (mc.player.isSprinting()) {
            baseSpeed *= 1.3f;
        }

        return MathHelper.clamp(baseSpeed, 0.1f, 10.0f);
    }

    public static boolean canSprintInDirection(float yaw) {
        if (mc.player == null) return false;

        float playerYaw = mc.player.getYaw();
        float yawDiff = Math.abs(MathHelper.wrapDegrees(yaw - playerYaw));

        return yawDiff <= 90.0f && canSprint();
    }
}