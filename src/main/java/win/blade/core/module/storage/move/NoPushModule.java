package win.blade.core.module.storage.move;

import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import win.blade.common.gui.impl.gui.setting.implement.BooleanSetting;
import win.blade.common.gui.impl.gui.setting.implement.GroupSetting;
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

    public final GroupSetting options = new GroupSetting("Отключать коллизию для", "").setToggleable().settings(
            new BooleanSetting("Сущностей", "").setValue(true),
            new BooleanSetting("Блоков", "").setValue(true),
            new BooleanSetting("Жидкостей", "").setValue(true),
            new BooleanSetting("Удочек", "").setValue(true)
    );

    public NoPushModule() {
        addSettings(options);
    }
    private BooleanSetting getBooleanSetting(GroupSetting group, String name) {
        return (BooleanSetting) group.getSubSetting(name);
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive e) {
        if (mc.player == null || mc.world == null) return;

        if (!getBooleanSetting(options, "Удочек").getValue()) return;

        if (e.getPacket() instanceof EntityStatusS2CPacket pac) {
            if (pac.getStatus() == 31 && pac.getEntity(mc.world) instanceof FishingBobberEntity fishingBobber) {
                if (fishingBobber.getHookedEntity() == mc.player) {
                    e.cancel();
                }
            }
        }
    }
}