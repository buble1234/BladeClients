package win.blade.core.event.impl.player;

import net.minecraft.entity.Entity;
import win.blade.common.utils.render.msdf.MsdfFont;
import win.blade.core.event.controllers.Event;

public abstract class PlayerActionEvents extends Event {

    private PlayerActionEvents() {
    }

    public static class Attack extends PlayerActionEvents {
        private final Entity entity;
        private final boolean pre;

        public Attack(Entity entity, boolean pre) {
            super();
            this.entity = entity;
            this.pre = pre;
        }

        public Entity getEntity() {
            return entity;
        }

        public boolean isPre() {
            return pre;
        }
    }

    public static class Jump extends PlayerActionEvents {
        public Jump() {
            super();
        }
    }

    public static class CloseInventory extends PlayerActionEvents {
        private final int id;

        public CloseInventory(int id) {
            super();
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }
}