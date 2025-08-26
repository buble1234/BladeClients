package win.blade.core.module.storage.combat;

import io.netty.buffer.Unpooled;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import win.blade.common.utils.network.PacketUtility;
import win.blade.core.event.controllers.EventHandler;
import win.blade.core.event.impl.network.PacketEvent;
import win.blade.core.module.api.Category;
import win.blade.core.module.api.Module;
import win.blade.core.module.api.ModuleInfo;

/**
 * Автор: NoCap
 * Дата создания: 03.08.2025
 */
@ModuleInfo(
        name = "Criticals",
        category = Category.COMBAT,
        desc = "Делает все атаки критическими без прыжка."
)
public class CriticalsModule extends Module {

    @EventHandler
    public void onPacketSend(PacketEvent.Send event) {
        if (mc.player == null || mc.world == null) return;

        if (event.getPacket() instanceof PlayerInteractEntityC2SPacket packet) {

            if (getInteractType(packet) == InteractType.ATTACK) {
                Entity target = getEntity(packet);

                if (target != null && !(target instanceof EndCrystalEntity) && !mc.player.isOnGround() && mc.player.fallDistance == 0 && !mc.player.isGliding() && !mc.player.isSubmergedInWater()) {

                    performPacketCrit();
                }
            }
        }
    }

    private void performPacketCrit() {
        if (mc.player == null) return;

        double x = mc.player.getX();
        double y = mc.player.getY();
        double z = mc.player.getZ();

        mc.player.fallDistance = 0.001f;
        PacketUtility.sendPositionAndRotation(x, y - 1e-6, z, mc.player.getYaw(), mc.player.getPitch(), false, mc.player.horizontalCollision);
    }

    private Entity getEntity(PlayerInteractEntityC2SPacket packet) {
        PacketByteBuf packetBuf = new PacketByteBuf(Unpooled.buffer());
        packet.write(packetBuf);
        if (mc.world != null) {
            return mc.world.getEntityById(packetBuf.readVarInt());
        }
        return null;
    }

    private InteractType getInteractType(PlayerInteractEntityC2SPacket packet) {
        PacketByteBuf packetBuf = new PacketByteBuf(Unpooled.buffer());
        packet.write(packetBuf);
        packetBuf.readVarInt();
        return packetBuf.readEnumConstant(InteractType.class);
    }

    private enum InteractType {
        INTERACT,
        ATTACK,
        INTERACT_AT
    }
}