package win.blade.core.module.storage.render;

import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;
import win.blade.common.gui.impl.gui.setting.implement.ValueSetting;
import win.blade.core.event.controllers.EventHandler;
import win.blade.core.event.impl.minecraft.UpdateEvents;
import win.blade.core.event.impl.network.PacketEvent;
import win.blade.core.module.api.Category;
import win.blade.core.module.api.Module;
import win.blade.core.module.api.ModuleInfo;


@ModuleInfo(name = "CustomWorld", category = Category.RENDER, desc = "Визуально меняет мир (только время)")
public class CustomWorld extends Module {
    public ValueSetting time = new ValueSetting("Время", "Устанавливает выбранное время в мире").range(0, 24000).setValue(16000);

    @EventHandler
    public void onUpdate(UpdateEvents.Update e) {
        if (mc.world != null) { (mc.world.getLevelProperties()).setTimeOfDay((long) time.getValue()); }
    }
    @EventHandler
    public void onPacketReceive(PacketEvent.Receive event) {
        if (event.getPacket() instanceof WorldTimeUpdateS2CPacket) {
            event.cancel();
        }
    }
}