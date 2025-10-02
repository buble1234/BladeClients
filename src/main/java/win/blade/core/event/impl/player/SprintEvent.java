package win.blade.core.event.impl.player;

import win.blade.common.utils.player.DirectionalInput;
import win.blade.core.event.controllers.Event;

/**
 * Автор: NoCap
 * Дата создания: 02.10.2025
 */
public class SprintEvent extends Event {

    DirectionalInput input;
    boolean sprint;
    Source source;

    public SprintEvent(DirectionalInput input, boolean sprint, Source source) {
        this.input = input;
        this.sprint = sprint;
        this.source = source;
    }

    public enum Source {
        INPUT,
        NETWORK,
        MOVEMENT
    }

    public Source getSource() {
        return source;
    }

    public void setSprint(boolean sprint) {
        this.sprint = sprint;
    }

    public boolean getSprint() {
        return sprint;
    }

    public DirectionalInput getInput() {
        return input;
    }
}
