package win.blade.core.module.storage.combat;

import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import win.blade.common.gui.impl.gui.setting.implement.ValueSetting;
import win.blade.common.utils.network.PacketUtility;
import win.blade.core.event.controllers.EventHandler;
import win.blade.core.event.impl.minecraft.UpdateEvents;
import win.blade.core.module.api.Category;
import win.blade.core.module.api.Module;
import win.blade.core.module.api.ModuleInfo;

/**
 * Автор: NoCap
 * Дата создания: 22.07.2025
 */
@ModuleInfo(
        name = "FastBow",
        category = Category.COMBAT,
        desc = "Ускоряет стрельбу из лука."
)
public class FastBowModule extends Module {

    private final ValueSetting speed = new ValueSetting("Скорость", "Время натяжения перед выстрелом.")
            .setValue(10f).range(1f, 10f);

    public FastBowModule() {
        addSettings(speed);
    }

    @EventHandler
    public void onUpdate(UpdateEvents.PlayerUpdate event) {
        if (mc.player == null || mc.world == null) {
            return;
        }

        boolean isHoldingBow = mc.player.getMainHandStack().getItem() == Items.BOW || mc.player.getOffHandStack().getItem() == Items.BOW;

        if (isHoldingBow && mc.player.isUsingItem()) {
            if (mc.player.getItemUseTime() >= speed.getValue()) {
                PacketUtility.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, mc.player.getHorizontalFacing()));

                Hand hand = mc.player.getOffHandStack().getItem() == Items.BOW ? Hand.OFF_HAND : Hand.MAIN_HAND;
                PacketUtility.sendSequentialPacket(id -> new PlayerInteractItemC2SPacket(hand, id, mc.player.getYaw(), mc.player.getPitch()));

                mc.player.stopUsingItem();
            }
        }
    }
}