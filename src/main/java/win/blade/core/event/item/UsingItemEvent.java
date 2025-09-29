package win.blade.core.event.item;

import win.blade.core.event.controllers.Event;

public class UsingItemEvent extends Event {
    private byte type;
    private boolean sprint;

    public UsingItemEvent(byte type, boolean sprint) {
        this.type = type;
        this.sprint = sprint;
    }

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public boolean isSprint() {
        return sprint;
    }

    public void setSprint(boolean sprint) {
        this.sprint = sprint;
    }
}