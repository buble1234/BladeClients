package win.blade.core.module.storage.combat;

import io.netty.buffer.Unpooled;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerPosition;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.network.packet.s2c.play.UpdateSelectedSlotS2CPacket;
import org.jetbrains.annotations.NotNull;
import win.blade.common.gui.impl.menu.settings.impl.BooleanSetting;
import win.blade.common.gui.impl.menu.settings.impl.MultiBooleanSetting;
import win.blade.common.utils.friends.FriendManager;
import win.blade.common.utils.network.PacketUtility;
import win.blade.core.Manager;
import win.blade.core.event.controllers.EventHandler;
import win.blade.core.event.impl.network.PacketEvent;
import win.blade.core.module.api.Category;
import win.blade.core.module.api.Module;
import win.blade.core.module.api.ModuleInfo;
import win.blade.mixin.accessor.PlayerPositionLookS2CPacketAccessor;

/**
 * Автор: NoCap
 * Дата создания: 24.07.2025
 */
@ModuleInfo(
        name = "ServerTweaks",
        category = Category.COMBAT
)
public class ServerTweaksModule extends Module {

    private final MultiBooleanSetting tweaks = new MultiBooleanSetting(this, "Убрать",
            BooleanSetting.of("Серверный поворот", true),
            BooleanSetting.of("Серверный свап слотов", false),
            BooleanSetting.of("Урон по друзьям", false)
    );

    @EventHandler
    public void NoServerRotate(PacketEvent.Receive e) {
        if (mc.player == null || mc.world == null) return;
        if (tweaks.getValue("Серверный поворот")) {
            if (e.getPacket() instanceof PlayerPositionLookS2CPacket packet) {
                PlayerPositionLookS2CPacketAccessor accessor = e.getPacket();
                PlayerPosition originalChange = accessor.getChange();

                PlayerPosition modifiedChange = new PlayerPosition(originalChange.position(), originalChange.deltaMovement(), mc.player.getYaw(), mc.player.getPitch());

                accessor.setChange(modifiedChange);
            }
        }
    }

    @EventHandler
    public void NoServerSwapSlot(PacketEvent.Receive e) {
        if (mc.player == null || mc.world == null) return;
        if (tweaks.getValue("Серверный свап слотов")) {
            if (e.getPacket() instanceof UpdateSelectedSlotS2CPacket) {
                e.cancel();
                PacketUtility.sendPacket(new UpdateSelectedSlotC2SPacket(mc.player.getInventory().selectedSlot));
            }
        }
    }
    @EventHandler
    public void NoFriendDamage(PacketEvent.Send e) {
        if (mc.player == null || mc.world == null) return;
        if (tweaks.getValue("Урон по друзьям")) {
            if (e.getPacket() instanceof PlayerInteractEntityC2SPacket packet) {
                Entity entity = getEntity(packet);
                if (entity instanceof PlayerEntity player) {

                    String playerName = player.getNameForScoreboard();
                    if (FriendManager.instance.hasFriend(playerName)) {
                        e.cancel();
                    }
                }
            }
        }
    }

    public static Entity getEntity(@NotNull PlayerInteractEntityC2SPacket packet) {
        PacketByteBuf packetBuf = new PacketByteBuf(Unpooled.buffer());
        packet.write(packetBuf);
        return mc.world.getEntityById(packetBuf.readVarInt());
    }
}