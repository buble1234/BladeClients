package win.blade.mixin.accessor;

import net.minecraft.entity.player.PlayerPosition;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Автор: NoCap
 * Дата создания: 29.06.2025
 */
@Mixin(PlayerPositionLookS2CPacket.class)
public interface PlayerPositionLookS2CPacketAccessor {
    @Mutable
    @Accessor("change")
    void setChange(PlayerPosition change);

    @Accessor("change")
    PlayerPosition getChange();
}