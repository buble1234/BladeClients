package win.blade.common.utils.attack;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import win.blade.common.utils.minecraft.MinecraftInstance;
import win.blade.common.utils.player.PlayerUtility;

/**
 * Автор: NoCap
 * Дата создания: 15.07.2025
 */
public class ShieldBreaker implements MinecraftInstance {

    private static AttackState state = new AttackState();

    public static boolean breakShield(LivingEntity target, AttackSettings settings) {
        if (!(target instanceof PlayerEntity playerTarget)) {
            return false;
        }

        boolean isShielding = playerTarget.isBlocking() && (playerTarget.getActiveItem().getItem() == Items.SHIELD || playerTarget.getOffHandStack().getItem() == Items.SHIELD);

        if (!isShielding) {
            return false;
        }

        int axeSlot = findAxeSlot();

        if (axeSlot == -1) {
            return false;
        }

        if (axeSlot < 9) {
            int prevSlot = mc.player.getInventory().selectedSlot;
            mc.player.getInventory().selectedSlot = axeSlot;
            if (mc.getNetworkHandler() != null) {
                mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(axeSlot));
            }

            performShieldBreakAttack(target, settings);

            mc.player.getInventory().selectedSlot = prevSlot;
            if (mc.getNetworkHandler() != null) {
                mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(prevSlot));
            }
        } else {
            int currentSlot = mc.player.getInventory().selectedSlot;
            mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, axeSlot, currentSlot, SlotActionType.SWAP, mc.player);
            if (mc.getNetworkHandler() != null) {
                mc.getNetworkHandler().sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
            }

            performShieldBreakAttack(target, settings);

            mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, axeSlot, currentSlot, SlotActionType.SWAP, mc.player);
            if (mc.getNetworkHandler() != null) {
                mc.getNetworkHandler().sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
            }
        }

        state.setLastAttackTime(System.currentTimeMillis());

        return true;
    }

    private static void performShieldBreakAttack(LivingEntity target, AttackSettings settings) {

        if (mc.player.isBlocking() && settings.unpressShield()) {
            mc.interactionManager.stopUsingItem(mc.player);
        }

        boolean wasSprint = mc.player.isSprinting();
        settings.attackMode().handleSprintBeforeAttack(settings, state);

        if (mc.getNetworkHandler() != null) {
            mc.interactionManager.attackEntity(mc.player, target);
        }
        mc.player.swingHand(PlayerUtility.getAttackHand());

        if (wasSprint != mc.player.isSprinting()) {
            settings.attackMode().handleSprintAfterAttack(settings, state);
        }
    }

    private static int findAxeSlot() {
        for (int i = 0; i < mc.player.getInventory().size(); ++i) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() instanceof AxeItem) {
                return i;
            }
        }
        return -1;
    }
}