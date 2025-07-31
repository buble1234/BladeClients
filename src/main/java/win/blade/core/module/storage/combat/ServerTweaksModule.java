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
import win.blade.common.gui.impl.gui.setting.implement.BooleanSetting;
import win.blade.common.gui.impl.gui.setting.implement.GroupSetting;
import win.blade.common.utils.friends.FriendManager;
import win.blade.common.utils.network.PacketUtility;
import win.blade.core.event.controllers.EventHandler;
import win.blade.core.event.impl.network.PacketEvent;
import win.blade.core.module.api.Category;
import win.blade.core.module.api.Module;
import win.blade.core.module.api.ModuleInfo;
import win.blade.mixin.accessor.PlayerPositionLookS2CPacketAccessor;

/**
 * Автор: NoCap
 * Дата создания: 24.07.2025
 * Рефакторинг под новый API: 14.07.2024
 */
@ModuleInfo(
        name = "ServerTweaks",
        category = Category.COMBAT
)
public class ServerTweaksModule extends Module {

    private final GroupSetting tweaks = new GroupSetting("Убрать", "").settings(
            new BooleanSetting("Серверный поворот", "").setValue(true),
            new BooleanSetting("Серверный свап слотов", "").setValue(false),
            new BooleanSetting("Урон по друзьям", "").setValue(false)
    );

    public ServerTweaksModule() {
        addSettings(tweaks);
    }

    private BooleanSetting getBooleanSetting(GroupSetting group, String name) {
        return (BooleanSetting) group.getSubSetting(name);
    }

    @EventHandler
    public void onNoServerRotate(PacketEvent.Receive e) {
        if (mc.player == null || mc.world == null) return;
        if (getBooleanSetting(tweaks, "Серверный поворот").getValue()) {
            if (e.getPacket() instanceof PlayerPositionLookS2CPacket packet) {
                PlayerPositionLookS2CPacketAccessor accessor = e.getPacket();
                PlayerPosition originalChange = accessor.getChange();

                PlayerPosition modifiedChange = new PlayerPosition(originalChange.position(), originalChange.deltaMovement(), mc.player.getYaw(), mc.player.getPitch());

                accessor.setChange(modifiedChange);
            }
        }
    }

    @EventHandler
    public void onNoServerSwapSlot(PacketEvent.Receive e) {
        if (mc.player == null || mc.world == null) return;
        if (getBooleanSetting(tweaks, "Серверный свап слотов").getValue()) {
            if (e.getPacket() instanceof UpdateSelectedSlotS2CPacket) {
                e.cancel();
                PacketUtility.sendPacket(new UpdateSelectedSlotC2SPacket(mc.player.getInventory().selectedSlot));
            }
        }
    }

    @EventHandler
    public void onNoFriendDamage(PacketEvent.Send e) {
        if (mc.player == null || mc.world == null) return;
        if (getBooleanSetting(tweaks, "Урон по друзьям").getValue()) {
            if (e.getPacket() instanceof PlayerInteractEntityC2SPacket packet) {
                Entity entity = getEntity(packet);

                if (entity instanceof PlayerEntity player) {
                    String playerName = player.getName().getString();
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