package win.blade.core.event.impl.render;

import win.blade.core.event.controllers.Event;

/**
 * Автор: NoCap
 * Дата создания: 23.06.2025
 */

public abstract class RenderCancelEvents extends Event {

    protected RenderCancelEvents() {
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

    public static class UnderWaterOverlay extends RenderCancelEvents {
        public UnderWaterOverlay() {
            super();
        }
    }

    public static class FreezeOverlay extends RenderCancelEvents {
        public FreezeOverlay() {
            super();
        }
    }

    public static class PortalOverlay extends RenderCancelEvents {
        public PortalOverlay() {
            super();
        }
    }
}