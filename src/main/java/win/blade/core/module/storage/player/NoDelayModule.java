package win.blade.core.module.storage.player;

import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import win.blade.common.utils.network.PacketUtility;
import win.blade.common.gui.impl.menu.settings.impl.BooleanSetting;
import win.blade.common.gui.impl.menu.settings.impl.MultiBooleanSetting;
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
 */

@ModuleInfo(name = "NoDelay",
        category = Category.PLAYER,
        desc = "Убирает задержку на выбранные вещи")
public class NoDelayModule extends Module {

    private final MultiBooleanSetting delayOptions = new MultiBooleanSetting(this, "Убирать задержку на",
            BooleanSetting.of("Прыжок", true),
            BooleanSetting.of("Правый клик", true),
            BooleanSetting.of("Ломание блоков", true)
    );

    @EventHandler
    public void onEnable(UpdateEvents.Update updateEvent) {
        if (delayOptions.getValue("Прыжок")) {
            resetJumpCooldown();
        }

        if (delayOptions.getValue("Правый клик")) {
            resetItemUseCooldown();
        }

        if (delayOptions.getValue("Ломание блоков")) {
            resetBlockCooldown();
        }
    }

    private void resetJumpCooldown() {
        LivingEntityAccessor livingEntityAccessor = (LivingEntityAccessor) mc.player;
        if (livingEntityAccessor != null && livingEntityAccessor.getLastJumpCooldown() > 0) {
            livingEntityAccessor.setLastJumpCooldown(0);
        }
    }

    private void resetItemUseCooldown() {
        MinecraftClientAccessor minecraftClientAccessor = (MinecraftClientAccessor) mc;
        if (minecraftClientAccessor != null && minecraftClientAccessor.getItemUseCooldown() > 0) {
            minecraftClientAccessor.setItemUseCooldown(0);
        }
    }

    private void resetBlockCooldown() {
        if (mc.crosshairTarget != null && mc.crosshairTarget.getType() == HitResult.Type.BLOCK && mc.crosshairTarget instanceof BlockHitResult hitResult) {
            float destroyProgress = mc.interactionManager.getBlockBreakingProgress();
            if (destroyProgress > 0.5f) {
                BlockPos pos = hitResult.getBlockPos();
                Direction face = hitResult.getSide();

                PacketUtility.finishDigging(pos, face);
                PacketUtility.cancelDigging(pos, face);
            }
        }
    }
}