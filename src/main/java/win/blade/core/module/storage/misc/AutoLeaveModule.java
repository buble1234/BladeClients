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
        desc = "Автоматически выходит с сервера."
)
public class AutoLeaveModule extends Module {

    private final List<String> staffPrefix = List.of("helper","moder","staff","admin","curator","стажёр", "staff", "сотрудник", "помощник", "админ", "модер");

    SelectSetting leaveType = new SelectSetting("Тип выхода", "Позволяет выбрать тип выхода")
            .value("Хаб", "Главное меню");

    MultiSelectSetting triggerSetting = new MultiSelectSetting("Триггеры", "Выберите в каком случае произойдет выход")
            .value("Игроки", "Стафф");

    ValueSetting distanceSetting = new ValueSetting("Макс. дистанция", "Максимальная дистанция для срабатывания авто-выхода")
            .setValue(10).range(5, 40).visible(() -> triggerSetting.isSelected("Игроки"));

    public AutoLeaveModule() {
        addSettings(leaveType, triggerSetting, distanceSetting);
    }

    @EventHandler
    public void onTick(UpdateEvents.Update e) {
        if (ServerUtility.isPvpActive()) return;

        if (triggerSetting.isSelected("Игроки"))
            mc.world.getPlayers().stream().filter(p -> mc.player.distanceTo(p) < distanceSetting.getValue() && mc.player != p && !FriendManager.instance.hasFriend(String.valueOf(p)))
                    .findFirst().ifPresent(p -> leave(p.getName().copy().append(" - Появился рядом " + mc.player.distanceTo(p) + "м")));
        if (triggerSetting.isSelected("Стафф") && !staffPrefix.isEmpty())
            leave(Text.of("Стафф на сервере"));
    }


    public void leave(Text text) {
        switch (leaveType.getSelected()) {
            case "Хаб" -> {

                Manager.notificationManager.add("Ливаю от игрока", NotificationType.SUCCESS, 10000);
                mc.getNetworkHandler().sendChatCommand("hub");
            }
            case "Главное меню" ->
                    mc.getNetworkHandler().getConnection().disconnect(Text.of("[Авто-выход] \n").copy().append(text));
        }
        setEnabled(false);
    }


}