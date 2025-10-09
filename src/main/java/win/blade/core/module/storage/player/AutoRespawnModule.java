package win.blade.core.module.storage.player;

import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.DeathMessageS2CPacket;
import win.blade.common.gui.impl.gui.setting.implement.SelectSetting;
import win.blade.common.utils.network.ServerUtility;
import win.blade.core.event.controllers.EventHandler;
import win.blade.core.event.impl.network.PacketEvent;
import win.blade.core.event.impl.player.DeathScreenEvent;
import win.blade.core.module.api.Category;
import win.blade.core.module.api.Module;
import win.blade.core.module.api.ModuleInfo;

@ModuleInfo(name = "AutoRespawn", category = Category.PLAYER, desc = "Автоматический")
public class AutoRespawnModule extends Module {

    SelectSetting modeSetting = new SelectSetting("Режим", "Режим авто-возраждения").value("FunTime Back", "Default");


    public AutoRespawnModule() {
        addSettings(modeSetting);
    }

    @EventHandler
    public void onPacket(PacketEvent.Receive e) {
        switch (e.getPacket()) {
            case DeathMessageS2CPacket message when ServerUtility.getWorldType().equals("lobby") && modeSetting.isSelected("FunTime Back") -> {
                mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(1448, 1337, 228, false, false));
                mc.player.requestRespawn();
                mc.player.closeScreen();
            }
            default -> {
            }
        }
    }

    @EventHandler
    public void onDeathScreen(DeathScreenEvent e) {
        if (modeSetting.isSelected("Default")) {
            mc.player.requestRespawn();
            mc.setScreen(null);
        }
    }

}
