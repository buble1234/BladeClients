package win.blade.core.module.storage.player;

import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import win.blade.common.gui.impl.gui.setting.implement.BooleanSetting;
import win.blade.common.gui.impl.gui.setting.implement.ValueSetting;
import win.blade.common.utils.math.TimerUtil;
import win.blade.core.event.controllers.EventHandler;
import win.blade.core.event.impl.minecraft.UpdateEvents;
import win.blade.core.module.api.Category;
import win.blade.core.module.api.Module;
import win.blade.core.module.api.ModuleInfo;

/**
 * Автор: NoCap
 * Дата создания: 26.09.2025
 */
@ModuleInfo(name = "AutoTool", category = Category.PLAYER, desc = "Автоматически берет лучший инструмент")
public class AutoToolModule extends Module {

    private final TimerUtil switchDelay = new TimerUtil();

    private static final BooleanSetting ignoreDurability = new BooleanSetting("Игнорировать прочность", "Игнорировать прочность инструмента при выборе")
            .setValue(false);

    private final ValueSetting swapDelay = new ValueSetting("Задержка переключения", "Задержка между переключениями инструментов (тики)")
            .setValue(20).range(1, 100);

    private int previousSlot = -1;
    private BlockPos currentMiningPos = null;
    private boolean isMining = false;

    public AutoToolModule() {
        addSettings(ignoreDurability, swapDelay);
    }

    @Override
    public void onEnable() {
        previousSlot = -1;
        currentMiningPos = null;
        isMining = false;
        switchDelay.updateLast();
        super.onEnable();
    }

    @Override
    protected void onDisable() {
        restorePreviousSlot();
        super.onDisable();
    }

    @EventHandler
    public void onUpdate(UpdateEvents.Update event) {
        if (mc.player == null || mc.world == null) {
            return;
        }

        updateMiningState();

        if (currentMiningPos != null && isMining) {
            switchToOptimalTool(currentMiningPos);
        }
    }

    private void updateMiningState() {
        if (mc.crosshairTarget != null && mc.crosshairTarget.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHit = (BlockHitResult) mc.crosshairTarget;
            BlockPos targetPos = blockHit.getBlockPos();

            if (mc.options.attackKey.isPressed()) {
                if (!targetPos.equals(currentMiningPos)) {
                    currentMiningPos = targetPos;
                }
                isMining = true;
            } else {
                isMining = false;
                currentMiningPos = null;
            }
        } else {
            isMining = false;
            currentMiningPos = null;
        }
    }

    private void switchToOptimalTool(BlockPos pos) {
        if (!switchDelay.timeElapsed((int) swapDelay.getValue())) {
            return;
        }

        BlockState blockState = mc.world.getBlockState(pos);
        if (blockState.isAir()) {
            return;
        }

        int bestSlot = findBestTool(blockState);
        if (bestSlot != -1 && bestSlot != mc.player.getInventory().selectedSlot) {
            if (previousSlot == -1) {
                previousSlot = mc.player.getInventory().selectedSlot;
            }

            mc.player.getInventory().selectedSlot = bestSlot;
            switchDelay.updateLast();
        }
    }

    public static int findBestTool(BlockState blockState) {
        int bestSlot = -1;
        float bestSpeed = 1.0f;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty()) {
                continue;
            }

            if (!ignoreDurability.getValue() && stack.isDamaged() && stack.getDamage() >= stack.getMaxDamage() - 2) {
                continue;
            }

            float miningSpeed = stack.getMiningSpeedMultiplier(blockState);
            int effLevel = EnchantmentHelper.getLevel(mc.world.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT).getEntry(Enchantments.EFFICIENCY.getValue()).get(), stack);
            if (miningSpeed > 1.0f && effLevel > 0) {
                miningSpeed += (float)(effLevel * effLevel + 1);
            }

            if (miningSpeed > bestSpeed) {
                bestSpeed = miningSpeed;
                bestSlot = i;
            }
        }

        return bestSlot;
    }

    private void restorePreviousSlot() {
        if (previousSlot != -1 && mc.player != null) {
            mc.player.getInventory().selectedSlot = previousSlot;
            previousSlot = -1;
        }
    }
}