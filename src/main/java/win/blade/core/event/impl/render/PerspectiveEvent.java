package win.blade.core.event.impl.render;

import net.minecraft.client.option.Perspective;
import win.blade.core.event.controllers.Event;

/**
 * Автор: NoCap
 * Дата создания: 06.10.2025
 */
public class PerspectiveEvent extends Event {
    private Perspective perspective;
    private final Perspective previous;
    private final boolean changing;

    public PerspectiveEvent(Perspective current, Perspective previous, boolean changing) {
        this.perspective = current;
        this.previous = previous;
        this.changing = changing;
    }

    public Perspective getPerspective() {
        return perspective;
    }

    public void setPerspective(Perspective perspective) {
        this.perspective = perspective;
    }

    public Perspective getPrevious() {
        return previous;
    }

    public boolean isChanging() {
        return changing;
    }
}