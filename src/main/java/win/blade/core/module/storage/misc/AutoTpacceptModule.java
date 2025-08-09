package win.blade.core.module.storage.misc;

import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import win.blade.common.gui.impl.gui.setting.implement.BooleanSetting;
import win.blade.common.utils.friends.FriendManager;
import win.blade.common.utils.network.ServerUtility;
import win.blade.core.event.controllers.EventHandler;
import win.blade.core.event.impl.network.PacketEvent;
import win.blade.core.module.api.Category;
import win.blade.core.module.api.Module;
import win.blade.core.module.api.ModuleInfo;

/**
 * Автор: NoCap
 * Дата создания: 16.07.2025
 */
@ModuleInfo(
        name = "AutoTpaccept",
        category = Category.MISC,
        desc = "Автоматически принимает запросы на телепортацию."
)
public class AutoTpacceptModule extends Module {

    private final BooleanSetting onlyFriends = new BooleanSetting("Только друзья", "Принимать телепорт только от друзей.").setValue(false);

    public AutoTpacceptModule() {
        addSettings(onlyFriends);
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive event) {
        if (mc.player == null || mc.world == null) return;
        if (event.getPacket() instanceof GameMessageS2CPacket) {
            final GameMessageS2CPacket packet = event.getPacket();
            String message = packet.content().getString();

            if (message.contains("телепортироваться") || message.contains("телепортацию") || message.contains("tpaccept") || message.contains("tpyes")) {
                String playerName = ServerUtility.isName(message);

                if (onlyFriends.getValue()) {
                    if (FriendManager.instance.hasFriend(playerName)) {
                        mc.getNetworkHandler().sendChatCommand("tpaccept " + playerName);
                    }
                } else {
                    mc.getNetworkHandler().sendChatCommand("tpaccept " + playerName);
                }
            }
        }
    }
}