package win.blade.mixin.minecraft.gui;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import win.blade.Blade;
import win.blade.core.Manager;
import win.blade.core.module.storage.misc.ItemScrollerModule;

import javax.annotation.Nullable;

@Mixin(HandledScreen.class)
public abstract class MixinHandledScreen<T extends ScreenHandler> extends Screen {

    @Shadow @Final protected T handler;
    @Shadow @Nullable
    protected Slot focusedSlot;

    @Shadow protected abstract void onMouseClick(Slot slot, int slotId, int button, SlotActionType actionType);

    private long time = 0;

    protected MixinHandledScreen(Text title) {
        super(title);
    }


    @Inject(method = "render", at = @At("HEAD"))
    private void onRender(CallbackInfo ci) {
        ItemScrollerModule itemScroller = Manager.getModuleManagement().get((ItemScrollerModule.class));
        if (client == null || !itemScroller.isEnabled()) {
            return;
        }

        boolean isShiftDown = hasShiftDown();

        if (GLFW.glfwGetMouseButton(this.client.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS && isShiftDown && focusedSlot != null && focusedSlot.hasStack()) {
            if (System.currentTimeMillis() - time > itemScroller.delay.getValue()) {
                this.onMouseClick(focusedSlot, focusedSlot.id, 0, SlotActionType.QUICK_MOVE);
                time = System.currentTimeMillis();
            }
        }
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void onKeyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        ItemScrollerModule itemScroller = Manager.getModuleManagement().get((ItemScrollerModule.class));
        if (client == null || client.player == null || !itemScroller.isEnabled()) {
            return;
        }

        if (client.options.dropKey.matchesKey(keyCode, scanCode) && hasControlDown() && hasShiftDown()) {
            if (focusedSlot != null && focusedSlot.hasStack()) {
                ItemStack hoveredStack = focusedSlot.getStack();
                Item itemToDrop = hoveredStack.getItem();

                for (Slot slot : this.handler.slots) {
                    if (slot.hasStack() && slot.getStack().getItem() == itemToDrop) {
                        // Выбрасываем стак
                        this.onMouseClick(slot, slot.id, 1, SlotActionType.THROW);
                    }
                }

                // Отменяем стандартное действие, чтобы не выбросить предмет дважды
                cir.setReturnValue(true);
                cir.cancel();
            }
        }
    }
}