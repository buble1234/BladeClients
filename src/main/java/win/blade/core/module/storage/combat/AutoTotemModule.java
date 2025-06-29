package win.blade.core.module.storage.combat;

import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
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

    @EventHandler
    public void onUpdate(UpdateEvents.PlayerUpdate event) {
        if (mc.player == null || mc.interactionManager == null) {
            return;
        }

        if (mc.currentScreen != null && !(mc.currentScreen instanceof InventoryScreen)) {
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
            int offhandSlotIndex = 45;

            mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, totemSlot, 0, SlotActionType.PICKUP, mc.player);
            mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, offhandSlotIndex, 0, SlotActionType.PICKUP, mc.player);
            mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, totemSlot, 0, SlotActionType.PICKUP, mc.player);
        }
    }

    private int findTotemSlot() {
        if (mc.player == null || mc.player.currentScreenHandler == null) {
            return -1;
        }

        for (int i = 9; i <= 44; i++) {
            final ItemStack stack = mc.player.currentScreenHandler.getSlot(i).getStack();
            if (stack.getItem() == Items.TOTEM_OF_UNDYING) {
                return i;
            }
        }

        return -1;
    }
}