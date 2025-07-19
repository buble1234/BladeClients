package win.blade.core.module.storage.misc;

import net.minecraft.client.util.InputUtil;
import net.minecraft.item.Item;
import net.minecraft.screen.slot.SlotActionType;
import org.lwjgl.glfw.GLFW;
import win.blade.common.gui.impl.menu.settings.impl.SliderSetting;
import win.blade.core.event.controllers.EventHandler;
import win.blade.core.event.impl.input.InputEvents;
import win.blade.core.module.api.Category;
import win.blade.core.module.api.Module;
import win.blade.core.module.api.ModuleInfo;

/**
 * Автор: NoCap
 * Дата создания: 16.07.2025
 */
@ModuleInfo(
        name = "ItemScroller",
        category = Category.MISC,
        desc = "Убирает задержку на клики по слотам"
)
public class ItemScrollerModule extends Module {

    public final SliderSetting delay = new SliderSetting(this, "Задержка", 50, 0, 100, 1);

    private boolean pauseListening = false;
    private long lastClickTime = 0;

    @EventHandler
    public void onClick(InputEvents.ClickSlot e) {
        if (mc.player == null || mc.world == null) return;

        if (System.currentTimeMillis() - lastClickTime < delay.getValue()) {
            return;
        }

        if ((isKeyPressed(GLFW.GLFW_KEY_LEFT_SHIFT) || isKeyPressed(GLFW.GLFW_KEY_RIGHT_SHIFT))
                && (isKeyPressed(GLFW.GLFW_KEY_LEFT_CONTROL) || isKeyPressed(GLFW.GLFW_KEY_RIGHT_CONTROL))
                && e.getSlotActionType() == SlotActionType.THROW
                && !pauseListening) {

            Item copy = mc.player.currentScreenHandler.slots.get(e.getSlot()).getStack().getItem();
            pauseListening = true;

            for (int i2 = 0; i2 < mc.player.currentScreenHandler.slots.size(); ++i2) {
                if (mc.player.currentScreenHandler.slots.get(i2).getStack().getItem() == copy)
                    mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, i2, 1, SlotActionType.THROW, mc.player);
            }

            pauseListening = false;
            lastClickTime = System.currentTimeMillis();
        }
    }

    public boolean isKeyPressed(int button) {
        if (button == -1) return false;

        long handle = mc.getWindow().getHandle();

        if (button >= 0 && button <= GLFW.GLFW_MOUSE_BUTTON_LAST) {
            return GLFW.glfwGetMouseButton(handle, button) == GLFW.GLFW_PRESS;
        }

        return InputUtil.isKeyPressed(handle, button);
    }
}