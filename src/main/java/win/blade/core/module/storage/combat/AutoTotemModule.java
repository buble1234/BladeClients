package win.blade.core.module.storage.combat;

import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import win.blade.common.gui.impl.gui.setting.implement.BooleanSetting;
import win.blade.common.gui.impl.gui.setting.implement.GroupSetting;
import win.blade.common.gui.impl.gui.setting.implement.ValueSetting;
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
        desc = "Автоматически берет тотем в левую руку."
)
public class AutoTotemModule extends Module {

    private final ValueSetting health = new ValueSetting("Здоровье для свапа", "Уровень здоровья для активации.")
            .setValue(10.0f).range(1.0f, 20.0f);

    private final GroupSetting swapOptions = new GroupSetting("Опции свапа", "Дополнительные условия для смены тотема.").settings(
            new BooleanSetting("Не прерывать использование", "Не менять тотем во время использования предметов.").setValue(true),
            new BooleanSetting("Не сменять при сфере в руке", "Не менять тотем, если в руке сфера.").setValue(true),
            new BooleanSetting("Предпочитать не зачарованные", "Сначала использует тотемы без чар.").setValue(false)
    );

    public AutoTotemModule() {
        addSettings(health, swapOptions);
    }

    private BooleanSetting getBooleanSetting(GroupSetting group, String name) {
        return (BooleanSetting) group.getSubSetting(name);
    }

    @EventHandler
    public void onUpdate(UpdateEvents.PlayerUpdate event) {
        if (mc.player == null || mc.interactionManager == null) {
            return;
        }

        if (mc.currentScreen != null && !(mc.currentScreen instanceof InventoryScreen)) {
            return;
        }

        boolean isUsingItem = mc.player.isUsingItem();
        if (getBooleanSetting(swapOptions, "Не прерывать использование").getValue() && isUsingItem) {
            return;
        }

        boolean isSphereInHand = isItemInOffHand(Items.PLAYER_HEAD);
        if (getBooleanSetting(swapOptions, "Не сменять при сфере в руке").getValue() && isSphereInHand) {
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

        if (!mc.player.currentScreenHandler.getCursorStack().isEmpty()) {
            mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, totemSlot, 0, SlotActionType.PICKUP, mc.player);
        }
    }

    private int findTotemSlot() {
        if (mc.player == null || mc.player.currentScreenHandler == null) {
            return -1;
        }

        if (getBooleanSetting(swapOptions, "Предпочитать не зачарованные").getValue()) {
            int unenchantedTotemSlot = findUnenchantedTotem();
            if (unenchantedTotemSlot != -1) {
                return unenchantedTotemSlot;
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
            return !offHandStack.isEmpty() && offHandStack.getItem() == item;
        }
        return false;
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