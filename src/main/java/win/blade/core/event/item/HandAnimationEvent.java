package win.blade.core.event.item;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Hand;
import win.blade.core.event.controllers.Event;

public class HandAnimationEvent extends Event {
    private MatrixStack matrices;
    private Hand hand;
    private float swingProgress;

    public HandAnimationEvent(MatrixStack matrices, Hand hand, float swingProgress) {
        this.matrices = matrices;
        this.hand = hand;
        this.swingProgress = swingProgress;
    }

    public MatrixStack getMatrices() {
        return matrices;
    }

    public void setMatrices(MatrixStack matrices) {
        this.matrices = matrices;
    }

    public Hand getHand() {
        return hand;
    }

    public void setHand(Hand hand) {
        this.hand = hand;
    }

    public float getSwingProgress() {
        return swingProgress;
    }

    public void setSwingProgress(float swingProgress) {
        this.swingProgress = swingProgress;
    }
}