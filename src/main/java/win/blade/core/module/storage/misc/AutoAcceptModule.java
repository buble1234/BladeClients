package win.blade.core.module.storage.misc;

import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import win.blade.common.gui.impl.gui.setting.implement.BooleanSetting;
import win.blade.common.gui.impl.gui.setting.implement.GroupSetting;
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
        desc = "Автоматически принимает запросы на телепортацию и приглашения в клан."
)
public class AutoAcceptModule extends Module {

    private final GroupSetting action = new GroupSetting("Действия", "Какие действия принимать.").setToggleable().settings(
            new BooleanSetting("Телепорт", "Принимать телепорт.").setValue(true),
            new BooleanSetting("Клан", "Принимать приглашения в клан.").setValue(true)
    );

    private final BooleanSetting onlyFriends = new BooleanSetting("Только друзья", "Принимать запросы только от друзей.").setValue(false);

    public AutoAcceptModule() {
        addSettings(action, onlyFriends);
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive event) {
        if (mc.player == null || mc.world == null) return;
        if (event.getPacket() instanceof GameMessageS2CPacket) {
            final GameMessageS2CPacket packet = event.getPacket();
            String message = packet.content().getString();

            if (((BooleanSetting) action.getSubSetting("Телепорт")).getValue() &&
                    (message.contains("телепортироваться") || message.contains("телепортацию") ||
                            message.contains("tpaccept") || message.contains("tpyes"))) {

                String playerName = ServerUtility.isName(message);

                if (shouldAcceptFromPlayer(playerName)) {
                    mc.getNetworkHandler().sendChatCommand("tpaccept " + playerName);
                }
            }

            if (((BooleanSetting) action.getSubSetting("Клан")).getValue() &&
                    ((message.contains("приглашает") && message.contains("клан")) ||
                            message.contains("clan invite") || message.contains("приглашение в клан") ||
                            message.contains("вступить в клан"))) {

                String playerName = ServerUtility.isName(message);

                if (shouldAcceptFromPlayer(playerName)) {
                    mc.getNetworkHandler().sendChatCommand("clan accept");
                }
            }
        }
    }

    private boolean shouldAcceptFromPlayer(String playerName) {
        if (playerName == null || playerName.isEmpty()) {
            return false;
        }

        if (onlyFriends.getValue()) {
            return FriendManager.instance.hasFriend(playerName);
        }

        return true;
    }
}