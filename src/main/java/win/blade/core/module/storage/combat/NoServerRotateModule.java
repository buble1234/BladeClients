package win.blade.core.module.storage.combat;

import net.minecraft.entity.player.PlayerPosition;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import win.blade.core.event.controllers.EventHandler;
import win.blade.core.event.impl.network.PacketEvent;
import win.blade.core.module.api.Category;
import win.blade.core.module.api.Module;
import win.blade.core.module.api.ModuleInfo;
import win.blade.mixin.accessor.PlayerPositionLookS2CPacketAccessor;

/**
 * Автор: NoCap
 * Дата создания: 29.06.2025
 */
@ModuleInfo(
        name = "NoServerRotate",
        category = Category.COMBAT,
        desc = "Не позволяет серверу вас развернуть"
)
public class NoServerRotateModule extends Module {

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive e) {
        if (mc.player == null || mc.world == null) return;

        if (e.getPacket() instanceof PlayerPositionLookS2CPacket packet) {
            PlayerPositionLookS2CPacketAccessor accessor = e.getPacket();
            PlayerPosition originalChange = accessor.getChange();

            PlayerPosition modifiedChange = new PlayerPosition(originalChange.position(), originalChange.deltaMovement(), mc.player.getYaw(), mc.player.getPitch());

            accessor.setChange(modifiedChange);
        }
    }
}
