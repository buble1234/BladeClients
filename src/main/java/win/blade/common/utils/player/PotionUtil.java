package win.blade.common.utils.player;

import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Hand;
import win.blade.common.utils.minecraft.MinecraftInstance;

public class PotionUtil implements MinecraftInstance {
    public boolean isChangingItem;
    private boolean isItemChangeRequested;
    private int previousSlot = -1;

    public void changeItemSlot(boolean resetAfter) {
        if (this.isItemChangeRequested && this.previousSlot != -1) {
            isChangingItem = true;
            if (mc.player != null) {
                mc.player.getInventory().selectedSlot = this.previousSlot;
            }
            if (resetAfter) {
                this.isItemChangeRequested = false;
                this.previousSlot = -1;
                isChangingItem = false;
            }
        }
    }

    public void setPreviousSlot(int slot) {
        this.isItemChangeRequested = true;
        this.previousSlot = slot;
    }

    public static void useItem(Hand hand) {
        if (mc.getNetworkHandler() != null && mc.player != null) {
            int sequence = mc.player.getInventory().getChangeCount();
            float yaw = mc.player.getYaw();
            float pitch = mc.player.getPitch();

            mc.getNetworkHandler().sendPacket(new PlayerInteractItemC2SPacket(hand, sequence, yaw, pitch));
        }

        if (mc.gameRenderer != null) {
            mc.gameRenderer.firstPersonRenderer.resetEquipProgress(hand);
        }
    }
}