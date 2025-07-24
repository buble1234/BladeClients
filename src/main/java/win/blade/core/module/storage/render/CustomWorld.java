package win.blade.core.module.storage.render;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;
import win.blade.common.gui.impl.menu.settings.impl.BooleanSetting;
import win.blade.common.gui.impl.menu.settings.impl.SliderSetting;
import win.blade.core.event.controllers.EventHandler;
import win.blade.core.event.impl.minecraft.UpdateEvents;
import win.blade.core.event.impl.network.PacketEvent;
import win.blade.core.module.api.Category;
import win.blade.core.module.api.Module;
import win.blade.core.module.api.ModuleInfo;


@ModuleInfo(name = "CustomWorld", category = Category.RENDER, desc = "Визуально меняет мир (только время)")
public class CustomWorld extends Module {
    public SliderSetting time = new SliderSetting(this,"Время", 16000, 0, 24000, 500);
    @EventHandler
    public void onUpdate(UpdateEvents.Update e) {
        if (mc.world != null) { (mc.world.getLevelProperties()).setTimeOfDay((time.getValue().intValue())); }
    }
    @EventHandler
    public void onPacketReceive(PacketEvent.Receive event) {
            if (event.getPacket() instanceof WorldTimeUpdateS2CPacket) { event.cancel(); }
    }
}