package win.blade.core.event.impl.player;

import net.minecraft.util.PlayerInput;
import win.blade.core.event.controllers.Event;

public class PlayerInputEvent extends Event {
    private PlayerInput input;

    public PlayerInputEvent(PlayerInput input) {
        this.input = input;
    }

    public PlayerInput getInput() {
        return input;
    }

    public void setInput(PlayerInput input) {
        this.input = input;
    }

    public void setJumping(boolean jump) {
        input = new PlayerInput(input.forward(), input.backward(), input.left(), input.right(), jump, input.sneak(), input.sprint());
    }

    public void setDirectional(boolean forward, boolean backward, boolean left, boolean right) {
        input = new PlayerInput(forward, backward, left, right, input.jump(), input.sneak(), input.sprint());
    }

    public void inputNone() {
        input = new PlayerInput(false, false, false, false, false, false, false);
    }

    public int forward() {
        return input.forward() ? 1 : input.backward() ? -1 : 0;
    }

    public float sideways() {
        return input.left() ? 1 : input.right() ? -1 : 0;
    }
}