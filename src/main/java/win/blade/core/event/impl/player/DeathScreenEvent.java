package win.blade.core.event.impl.player;

import win.blade.core.event.controllers.Event;

public class DeathScreenEvent extends Event {
    private int ticksSinceDeath;

    public DeathScreenEvent(int ticksSinceDeath) {
        this.ticksSinceDeath = ticksSinceDeath;
    }

    public int getTicksSinceDeath() {
        return ticksSinceDeath;
    }
}