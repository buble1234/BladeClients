package win.blade.core.module.storage.misc;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.EntityHitResult;
import win.blade.common.gui.impl.gui.setting.implement.BooleanSetting;
import win.blade.common.gui.impl.gui.setting.implement.GroupSetting;
import win.blade.common.utils.aim.base.ViewTracer;
import win.blade.common.utils.aim.core.ViewDirection;
import win.blade.common.utils.friends.FriendManager;
import win.blade.common.utils.minecraft.ChatUtility;
import win.blade.common.utils.other.Result;
import win.blade.core.event.controllers.EventHandler;
import win.blade.core.event.impl.input.InputEvents;
import win.blade.core.event.impl.minecraft.UpdateEvents;
import win.blade.core.module.api.Category;
import win.blade.core.module.api.Module;
import win.blade.core.module.api.ModuleInfo;

/**
 * Автор: NoCap
 * Дата создания: 21.09.2025
 */
@ModuleInfo(name = "ClickActions", category = Category.MISC)
public class ClickActionsModule extends Module {

    private final GroupSetting options = new GroupSetting("Действие", "Дейсвие при клике.").settings(
            new BooleanSetting("Эндер-жемчуг", "Использует эндер жемчуг при клике.").setValue(true),
            new BooleanSetting("Друзья", "Добавляет игрока в друзья при клике.").setValue(false)
    );

    public ClickActionsModule() {
        addSettings(options);
    }

    @EventHandler
    public void onMouseClick(InputEvents.Mouse event) {
        if (event.getButton() != 2 || event.getAction() != 1) return;

        BooleanSetting friendSetting = (BooleanSetting) options.getSubSetting("Друзья");
        if (friendSetting == null || !friendSetting.getValue()) return;

        friend();
    }

    @EventHandler
    public void onUpdate(UpdateEvents.Update e) {

    }

    public void enderPearl() {

    }

    public void friend() {
        if (mc.player == null || mc.world == null) return;

        ViewDirection viewDirection = new ViewDirection(
                mc.player.getYaw(),
                mc.player.getPitch()
        );

        EntityHitResult hitResult = ViewTracer.traceEntity(6.0, viewDirection, entity -> entity instanceof PlayerEntity && entity != mc.player);

        if (hitResult == null || !(hitResult.getEntity() instanceof PlayerEntity)) {
            ChatUtility.print("Игрок не найден под курсором");
            return;
        }

        PlayerEntity targetPlayer = (PlayerEntity) hitResult.getEntity();
        String playerName = targetPlayer.getName().getString();


        if (FriendManager.instance.hasFriend(playerName)) {
            Result<Boolean, String> result = FriendManager.instance.removeFriend(playerName);

            if (result.isSuccess()) {
                ChatUtility.print("§cИгрок: %s был успешно удалён из списка друзей!".formatted(playerName));
            } else {
                ChatUtility.print("§c" + result.error());
            }
        } else {
            Result<Boolean, String> result = FriendManager.instance.add(playerName);
            ChatUtility.printResult(result, "Друг: %s успешно сохранён".formatted(playerName));
        }
    }
}
