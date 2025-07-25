package win.blade.core.event.impl.input;


import net.minecraft.screen.slot.SlotActionType;
import win.blade.core.Manager;
import win.blade.core.event.controllers.Event;

public abstract class InputEvents extends Event {

    public static class Keyboard extends InputEvents {
        private final int key;
        private final int action;

        public Keyboard(int key, int action) {
            super();
            this.key = key;
            this.action = action;
        }

        public int getKey() {
            return key;
        }

        public int getAction() {
            return action;
        }
    }

    public static class KeyboardRelease extends InputEvents{
        private final int key;

        public KeyboardRelease(int key){
            this.key = key;
        }

        public int getKey(){
            return key;
        }
    }

    public static class Mouse extends InputEvents {
        private double x, y;
        private final int button;
        private final int action;

        public Mouse(int button, int action) {
            super();
            this.button = button;
            this.action = action;
        }

        public void post(double mx, double my) {
            this.x = mx;
            this.y = my;
            Manager.EVENT_BUS.post(this);
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

        public int getButton() {
            return button;
        }

        public int getAction() {
            return action;
        }
    }

    public static class MouseScroll extends InputEvents {
        private final double horizontal;
        private final double vertical;

        public MouseScroll(double horizontal, double vertical) {
            super();
            this.horizontal = horizontal;
            this.vertical = vertical;
        }

        public double getHorizontal() {
            return horizontal;
        }

        public double getVertical() {
            return vertical;
        }
    }

    public static class ClickSlot extends InputEvents {
        private final SlotActionType slotActionType;
        private final int slot, button, id;

        public ClickSlot(SlotActionType slotActionType, int slot, int button, int id) {
            super();
            this.slot = slot;
            this.button = button;
            this.id = id;
            this.slotActionType = slotActionType;
        }

        public SlotActionType getSlotActionType() {
            return slotActionType;
        }

        public int getSlot() {
            return slot;
        }

        public int getButton() {
            return button;
        }

        public int getId() {
            return id;
        }
    }
}