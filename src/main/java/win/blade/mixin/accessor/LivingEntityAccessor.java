package win.blade.mixin.accessor;

import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Автор: NoCap
 * Дата создания: 29.06.2025
 */
@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {
    @Accessor("lastAttackedTicks")
    int getLastAttackedTicks();
    @Accessor("jumpingCooldown")
    int getLastJumpCooldown();
    @Accessor("jumpingCooldown")
    void setLastJumpCooldown(int val);
}