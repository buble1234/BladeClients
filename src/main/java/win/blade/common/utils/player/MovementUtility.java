package win.blade.common.utils.player;

import net.minecraft.client.option.KeyBinding;

import java.util.Objects;
import java.util.stream.Stream;

import static win.blade.common.utils.minecraft.MinecraftInstance.mc;

/**
 * Автор: NoCap
 * Дата создания: 17.06.2025
 */

public final class MovementUtility {

    public static boolean isMoving() {
        return mc.player != null && mc.world != null && mc.player.input != null && (mc.player.input.movementForward != 0.0 || mc.player.input.movementSideways != 0.0);
    }

    public static KeyBinding[] getMovementKeys(boolean includeSneak) {
        return Stream.of(
                        mc.options.forwardKey,
                        mc.options.backKey,
                        mc.options.leftKey,
                        mc.options.rightKey,
                        mc.options.jumpKey,
                        mc.options.sprintKey,
                        includeSneak ? mc.options.sneakKey : null
                ).filter(Objects::nonNull)
                .toArray(KeyBinding[]::new);
    }

    public static double getSpeed() {
        return Math.hypot(mc.player.getVelocity().x, mc.player.getVelocity().z);
    }
}
