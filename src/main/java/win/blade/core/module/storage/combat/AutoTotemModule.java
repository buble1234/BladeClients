package win.blade.core.module.storage.combat;

import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import win.blade.common.gui.impl.menu.settings.impl.BooleanSetting;
import win.blade.common.gui.impl.menu.settings.impl.MultiBooleanSetting;
import win.blade.common.gui.impl.menu.settings.impl.SliderSetting;
import win.blade.core.event.controllers.EventHandler;
import win.blade.core.event.impl.minecraft.UpdateEvents;
import win.blade.core.module.api.Category;
import win.blade.core.module.api.Module;
import win.blade.core.module.api.ModuleInfo;

/**
 * Автор: NoCap
 * Дата создания: 29.06.2025
 */
@ModuleInfo(
        name = "AutoTotem",
        category = Category.COMBAT,
        desc = "Автоматически берёт тотем"
)
public class AutoTotemModule extends Module {

    private final SliderSetting health = new SliderSetting(this, "Здоровье для свапа", 10.0f, 1.0f, 20.0f, 0.1f);

    private final MultiBooleanSetting swapOptions = new MultiBooleanSetting(this, "Опции свапа",
            BooleanSetting.of("Не прерывать использование", true),
            BooleanSetting.of("Не сменять при сфере в руке", true),
            BooleanSetting.of("Предпочитать не зачарованные", false)
    );

    @EventHandler
    public void onUpdate(UpdateEvents.PlayerUpdate event) {
        if (mc.player == null || mc.interactionManager == null) {
            return;
        }

        if (mc.currentScreen != null && !(mc.currentScreen instanceof InventoryScreen)) {
            return;
        }

        boolean isUsingItem = mc.player.isUsingItem();
        if (swapOptions.getValue("Не прерывать использование") && isUsingItem) {
            return;
        }

        boolean isSphereItem = isItemInOffHand(Items.PLAYER_HEAD);
        if (swapOptions.getValue("Не сменять при сфере в руке") && isSphereItem) {
            return;
        }

        if (mc.player.getOffHandStack().getItem() == Items.TOTEM_OF_UNDYING) {
            return;
        }

        float currentHealth = mc.player.getHealth() + mc.player.getAbsorptionAmount();
        if (currentHealth > health.getValue()) {
            return;
        }

        int totemSlot = findTotemSlot();

        if (totemSlot != -1) {
            swapTotem(totemSlot);
        }
    }

    private void swapTotem(int totemSlot) {
        int offhandSlotIndex = 45;

        mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, totemSlot, 0, SlotActionType.PICKUP, mc.player);
        mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, offhandSlotIndex, 0, SlotActionType.PICKUP, mc.player);
        mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, totemSlot, 0, SlotActionType.PICKUP, mc.player);
    }

    private int findTotemSlot() {
        if (mc.player == null || mc.player.currentScreenHandler == null) {
            return -1;
        }

        if (swapOptions.getValue("Предпочитать не зачарованные") && isTotemInHand()) {
            if (isEnchanted(mc.player.getMainHandStack()) || isEnchanted(mc.player.getOffHandStack())) {
                return findUnenchantedTotem();
            }
        }

        for (int i = 9; i <= 44; i++) {
            final ItemStack stack = mc.player.currentScreenHandler.getSlot(i).getStack();
            if (stack.getItem() == Items.TOTEM_OF_UNDYING) {
                return i;
            }
        }

        return -1;
    }

    public static boolean isItemInOffHand(Item item) {
        if (mc.player != null) {
            ItemStack offHandStack = mc.player.getOffHandStack();
            if(!offHandStack.isEmpty()){
                return offHandStack.getItem() == item;
            }
            return false;
        }
        return false;
    }

    private boolean isTotemInHand() {
        return mc.player.getMainHandStack().getItem() == Items.TOTEM_OF_UNDYING ||
                mc.player.getOffHandStack().getItem() == Items.TOTEM_OF_UNDYING;
    }

    private boolean isEnchanted(ItemStack stack) {
        return stack.getItem() == Items.TOTEM_OF_UNDYING && stack.hasEnchantments();
    }

    private int findUnenchantedTotem() {
        for (int i = 9; i <= 44; i++) {
            final ItemStack stack = mc.player.currentScreenHandler.getSlot(i).getStack();
            if (stack.getItem() == Items.TOTEM_OF_UNDYING && !stack.hasEnchantments()) {
                return i;
            }
        }
        return -1;
    }
}