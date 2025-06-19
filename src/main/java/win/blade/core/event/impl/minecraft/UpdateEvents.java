package win.blade.core.event.impl.minecraft;

import win.blade.core.event.controllers.Event;

public abstract class UpdateEvents extends Event {

    private UpdateEvents() {
    }

    public static class Update extends UpdateEvents {
        public Update() {
            super();
        }
    }

    public static class PlayerUpdate extends UpdateEvents {
        public PlayerUpdate() {
            super();
        }
    }

    public static class PostPlayerUpdate extends UpdateEvents {
        private int iterations;

        public int getIterations() {
            return iterations;
        }

        public void setIterations(int in) {
            iterations = in;
        }

        public PostPlayerUpdate() {
            super();
        }
    }
}