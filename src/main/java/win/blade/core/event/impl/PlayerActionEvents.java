package win.blade.core.event.impl;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
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
}