package win.blade.core.event.item;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import win.blade.core.event.controllers.Event;

public class HandOffsetEvent extends Event {
    private MatrixStack matrices;
    private ItemStack stack;
    private Hand hand;

    public HandOffsetEvent(MatrixStack matrices, ItemStack stack, Hand hand) {
        this.matrices = matrices;
        this.stack = stack;
        this.hand = hand;
    }

    public MatrixStack getMatrices() {
        return matrices;
    }

    public void setMatrices(MatrixStack matrices) {
        this.matrices = matrices;
    }

    public ItemStack getStack() {
        return stack;
    }

    public void setStack(ItemStack stack) {
        this.stack = stack;
    }

    public Hand getHand() {
        return hand;
    }

    public void setHand(Hand hand) {
        this.hand = hand;
    }
}