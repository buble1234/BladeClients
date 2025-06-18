package win.blade.common.utils.player;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.AxeItem;
import net.minecraft.util.Hand;
import win.blade.common.utils.minecraft.MinecraftInstance;
import win.blade.common.utils.player.SprintUtility;

public class AttackUtility implements MinecraftInstance {
    
    public enum AttackMode {
        OLD("1.8"),
        NEW("1.9");

        private final String name;
        
        AttackMode(String name) {
            this.name = name;
        }
        
        @Override
        public String toString() {
            return name;
        }
    }
    
    public enum CriticalMode {
        NONE("None"),
        ALWAYS("Always"),
        ADAPTIVE("Adaptive");

        private final String name;
        
        CriticalMode(String name) {
            this.name = name;
        }
        
        @Override
        public String toString() {
            return name;
        }
    }
}