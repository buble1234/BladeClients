package win.blade.core.module.storage.move;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import win.blade.common.gui.impl.gui.setting.implement.BooleanSetting;
import win.blade.common.utils.math.TimerUtil;
import win.blade.common.utils.player.MovementUtility;
import win.blade.core.event.controllers.EventHandler;
import win.blade.core.event.impl.minecraft.UpdateEvents;
import win.blade.core.event.impl.player.PlayerActionEvents;
import win.blade.core.module.api.Category;
import win.blade.core.module.api.Module;
import win.blade.core.module.api.ModuleInfo;

@ModuleInfo(
        name = "ElytraRecast",
        category = Category.MOVE,
        desc = "Буст от элитр"
)
public class ElytraRecastModule extends Module {
    TimerUtil jumpWindow = new TimerUtil();
    BooleanSetting runwayAssistSetting = new BooleanSetting("Помощь при взлёте", "Помогает взлететь при движении")
            .setValue(true);
    BooleanSetting wearToleranceSetting = new BooleanSetting("Полёт с износом", "Позволяет взлетать с изношенными элитрами")
            .setValue(false);

    public ElytraRecastModule() {
        addSettings(runwayAssistSetting, wearToleranceSetting);
    }

    @EventHandler
    public void onJump(PlayerActionEvents.Jump e) {
        jumpWindow.reset();
    }

    @EventHandler
    public void onPlayerTick(UpdateEvents.PlayerUpdate e) {
        if (!isElytraUsable()) return;
        if (!MovementUtility.isMoving()) return;

        if (mc.player.isOnGround()) {
            if (runwayAssistSetting.getValue() && jumpWindow.finished(25)) {
                mc.player.jump();
            }
        } else if (!mc.player.isGliding()) {
            mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
            mc.player.startGliding();
        }
    }

    private boolean isElytraUsable() {
        ItemStack chest = mc.player.getEquippedStack(EquipmentSlot.CHEST);
        if (!chest.isOf(Items.ELYTRA)) return false;
        if (wearToleranceSetting.getValue()) return true;
        if (!chest.isDamageable()) return true;
        return chest.getMaxDamage() - chest.getDamage() > 1;
    }
}
