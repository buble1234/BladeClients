package win.blade.core.event.impl.minecraft;

import net.minecraft.entity.Entity;
import win.blade.core.event.controllers.Event;

/**
 * Автор: NoCap
 * Дата создания: 02.07.2025
 */
public class OptionEvents extends Event {

    private OptionEvents() {
    }

    public static class Gamma extends OptionEvents {
        private float gamma;

        public Gamma(float gamma) {
            super();
            this.gamma = gamma;
        }

        public float getGamma() {
            return gamma;
        }

        public void setGamma(float gamma) {
            this.gamma = gamma;
        }
    }
}