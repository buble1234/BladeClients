package win.blade.common.utils.player;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import win.blade.common.utils.minecraft.MinecraftInstance;

/**
 * Автор: NoCap
 * Дата создания: 28.06.2025
 */
public class PlayerUtility implements MinecraftInstance {

    public static boolean hasArmor(PlayerEntity player) {
        for (ItemStack item : player.getArmorItems()) {
            if (!item.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public static boolean isTeammate(PlayerEntity player) {
        if (mc.player == null) return false;
        int teamColorValue = mc.player.getTeamColorValue();
        return teamColorValue == player.getTeamColorValue() && teamColorValue != 16777215;
    }
}