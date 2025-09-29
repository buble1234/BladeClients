package win.blade.core.module.storage.misc;

import net.minecraft.text.Text;
import win.blade.common.gui.impl.gui.setting.implement.MultiSelectSetting;
import win.blade.common.gui.impl.gui.setting.implement.SelectSetting;
import win.blade.common.gui.impl.gui.setting.implement.ValueSetting;
import win.blade.common.ui.NotificationType;
import win.blade.common.utils.friends.FriendManager;
import win.blade.common.utils.network.ServerUtility;
import win.blade.core.Manager;
import win.blade.core.event.controllers.EventHandler;
import win.blade.core.event.impl.minecraft.UpdateEvents;
import win.blade.core.module.api.Category;
import win.blade.core.module.api.Module;
import win.blade.core.module.api.ModuleInfo;

import java.util.List;

@ModuleInfo(
        name = "AutoLeave",
        category = Category.MISC,
        desc = ""
)
public class AutoLeaveModule extends Module {

    private final List<String> staffPrefix = List.of("helper","moder","staff","admin","curator","стажёр", "staff", "сотрудник", "помощник", "админ", "модер");

    SelectSetting leaveType = new SelectSetting("Leave Type", "Allows you to select the leave type")
            .value("Hub", "Main Menu");

    MultiSelectSetting triggerSetting = new MultiSelectSetting("Triggers", "Select in which case you will exit")
            .value("Players", "Staff");

    ValueSetting distanceSetting = new ValueSetting("Max Distance", "Maximum distance for triggering auto leave")
            .setValue(10).range(5, 40).visible(() -> triggerSetting.isSelected("Players"));

    public AutoLeaveModule() {
        addSettings(leaveType, triggerSetting, distanceSetting);
    }

    @EventHandler
    public void onTick(UpdateEvents.Update e) {
        if (ServerUtility.isPvpActive()) return;

        if (triggerSetting.isSelected("Players"))
            mc.world.getPlayers().stream().filter(p -> mc.player.distanceTo(p) < distanceSetting.getValue() && mc.player != p && !FriendManager.instance.hasFriend(String.valueOf(p)))
                    .findFirst().ifPresent(p -> leave(p.getName().copy().append(" - Появился рядом " + mc.player.distanceTo(p) + "м")));
        if (triggerSetting.isSelected("Staff") && !staffPrefix.isEmpty())
            leave(Text.of("Стафф на сервере"));
    }


    public void leave(Text text) {
        switch (leaveType.getSelected()) {
            case "Hub" -> {

                Manager.notificationManager.add("Ливаю от игрока", NotificationType.SUCCESS, 10000);
                mc.getNetworkHandler().sendChatCommand("hub");
            }
            case "Main Menu" ->
                    mc.getNetworkHandler().getConnection().disconnect(Text.of("[Auto Leave] \n").copy().append(text));
        }
        setEnabled(false);
    }


}
