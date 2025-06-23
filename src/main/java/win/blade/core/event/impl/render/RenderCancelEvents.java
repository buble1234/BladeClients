package win.blade.core.event.impl.render;

import win.blade.core.event.controllers.Event;

/**
 * Автор: NoCap
 * Дата создания: 23.06.2025
 */

public abstract class RenderCancelEvents extends Event {

    private boolean cancelled;

    protected RenderCancelEvents() {
        this.cancelled = false;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public static class FireOverlay extends RenderCancelEvents {
        public FireOverlay() {
            super();
        }
    }

    public static class BossBar extends RenderCancelEvents {
        public BossBar() {
            super();
        }
    }

    public static class Scoreboard extends RenderCancelEvents {
        public Scoreboard() {
            super();
        }
    }

    public static class CameraShake extends RenderCancelEvents {
        public CameraShake() {
            super();
        }
    }

    public static class BadEffects extends RenderCancelEvents {
        public BadEffects() {
            super();
        }
    }

    public static class Weather extends RenderCancelEvents {
        public Weather() {
            super();
        }
    }
}