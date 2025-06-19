package win.blade.core.module.storage.move;

import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import win.blade.common.gui.impl.menu.settings.impl.BooleanSetting;
import win.blade.common.gui.impl.menu.settings.impl.MultiBooleanSetting;
import win.blade.core.event.controllers.EventHandler;
import win.blade.core.event.impl.network.PacketEvent;
import win.blade.core.module.api.Category;
import win.blade.core.module.api.Module;
import win.blade.core.module.api.ModuleInfo;

@ModuleInfo(
        name = "NoPush",
        category = Category.MOVE,
        desc = "Отключает коллизию"
)
public class NoPushModule extends Module {

    public final MultiBooleanSetting options = new MultiBooleanSetting(this, "Отключать коллизию для",
            BooleanSetting.of("Сущностей", true),
            BooleanSetting.of("Блоков", true),
            BooleanSetting.of("Жидкостей", true),
            BooleanSetting.of("Удочек", true)
    );

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive e) {
        if (e.getPacket() instanceof EntityStatusS2CPacket pac && pac.getStatus() == 31 && pac.getEntity(mc.world) instanceof FishingBobberEntity fishingBobber && options.getValue("Удочек"))
            if (fishingBobber.getHookedEntity() == mc.player) e.cancel();
    }
}
