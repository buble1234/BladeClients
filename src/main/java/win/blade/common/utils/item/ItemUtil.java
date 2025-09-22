package win.blade.common.utils.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import win.blade.common.utils.minecraft.MinecraftInstance;

public class ItemUtil implements MinecraftInstance {

    public static int maxUseTick(Item item) {
        return maxUseTick(item.getDefaultStack());
    }

    public static int maxUseTick(ItemStack stack) {
        return switch (stack.getUseAction()) {
            case EAT, DRINK -> 32;
            case CROSSBOW, SPEAR -> 10;
            case BOW -> 20;
            case BLOCK -> 0;
            default -> stack.getMaxUseTime(mc.player);
        };
    }

    public static float getCooldownProgress(Item item) {
        if (mc.player == null) return 0;

        ItemStack itemStack = item.getDefaultStack();


        float cooldownProgress = mc.player.getItemCooldownManager().getCooldownProgress(itemStack, mc.getRenderTickCounter().getTickDelta(false));

        if (cooldownProgress > 0.0f) {
            return cooldownProgress * 1.0f;
        }

        return 0;
    }
}