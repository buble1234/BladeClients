package win.blade.core.module.storage.player;

import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import win.blade.common.gui.impl.gui.setting.implement.BooleanSetting;
import win.blade.common.gui.impl.gui.setting.implement.GroupSetting;
import win.blade.common.utils.network.PacketUtility;
import win.blade.core.event.block.BlockBreakingEvent;
import win.blade.core.event.controllers.EventHandler;
import win.blade.core.event.impl.minecraft.UpdateEvents;
import win.blade.core.module.api.Category;
import win.blade.core.module.api.Module;
import win.blade.core.module.api.ModuleInfo;
import win.blade.mixin.accessor.LivingEntityAccessor;
import win.blade.mixin.accessor.MinecraftClientAccessor;

/**
 * Автор: NoCap
 * Дата создания: 29.06.2025
 * Рефакторинг под новый API: 14.07.2024
 */
@ModuleInfo(name = "NoDelay",
        category = Category.PLAYER,
        desc = "Убирает различные игровые задержки.")
public class NoDelayModule extends Module {

    private final GroupSetting delayOptions = new GroupSetting("Убирать задержку на", "Выбор отключаемых задержек.").setToggleable().settings(
            new BooleanSetting("Прыжок", "Убирает задержку между прыжками.").setValue(true),
            new BooleanSetting("Правый клик", "Убирает задержку на правый клик.").setValue(true),
            new BooleanSetting("Ломание блоков", "Ускоряет ломание блоков.").setValue(true)
    );

    public NoDelayModule() {
        addSettings(delayOptions);
    }

    private BooleanSetting getBooleanSetting(GroupSetting group, String name) {
        return (BooleanSetting) group.getSubSetting(name);
    }

    @EventHandler
    public void onUpdate(UpdateEvents.Update updateEvent) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        if (getBooleanSetting(delayOptions, "Прыжок").getValue()) {
            resetJumpCooldown();
        }

        if (getBooleanSetting(delayOptions, "Правый клик").getValue()) {
            resetItemUseCooldown();
        }

    }

    private void resetJumpCooldown() {
        if (mc.player == null) return;

        LivingEntityAccessor livingEntityAccessor = (LivingEntityAccessor) mc.player;
        if (livingEntityAccessor.getLastJumpCooldown() > 0) {
            livingEntityAccessor.setLastJumpCooldown(0);
        }
    }

    private void resetItemUseCooldown() {
        MinecraftClientAccessor minecraftClientAccessor = (MinecraftClientAccessor) mc;
        if (minecraftClientAccessor.getItemUseCooldown() > 0) {
            minecraftClientAccessor.setItemUseCooldown(0);
        }
    }

    @EventHandler
    public void onBlockBreaking(BlockBreakingEvent e) {
        if (getBooleanSetting(delayOptions, "Ломание блоков").getValue()) {
            BlockPos blockPos = e.getBlockPos();
            Direction direction = e.getDirection();
            if (mc.interactionManager.currentBreakingProgress >= 0.5) {
                mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, blockPos, direction));
                mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, blockPos, direction));
            }
        }
    }
}