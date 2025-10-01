package win.blade.core.module.storage.misc;

import net.fabricmc.fabric.api.event.player.PlayerPickItemEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.EnderPearlItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.BundleItemSelectedC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
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
        boolean state = false;
        if (friendSetting != null && friendSetting.getValue()) {
            state = friend();
        }
        if(!state){
            enderPearl();
        }
    }

    @EventHandler
    public void onUpdate(UpdateEvents.Update e) {

    }

    public void enderPearl() {
        if(mc.player.getInventory().getMainHandStack().getItem() instanceof EnderPearlItem){
            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
            mc.player.swingHand(Hand.MAIN_HAND);
        } else{
            int slot = useItem(Items.ENDER_PEARL);
//            if(slot > 8){
////                mc.interactionManager.clickSlot(0, slot, mc.player.getInventory().selectedSlot, SlotActionType.SWAP, mc.player);
//                // anouther one!
//            }
        }
    }

    public static int useItem(Item item){
        int hotBar = getItemOnNeededSlot(true, item);

        if (hotBar != - 1) {
            int wrapSlot = mc.player.getInventory().selectedSlot;
            mc.player.getInventory().selectedSlot = hotBar;

            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
            mc.player.swingHand(Hand.MAIN_HAND);

            if (hotBar != mc.player.getInventory().selectedSlot) {
                mc.player.networkHandler.sendPacket(new BundleItemSelectedC2SPacket(mc.player.getInventory().selectedSlot, mc.player.getInventory().selectedSlot));
            }

            mc.player.getInventory().selectedSlot = wrapSlot;
            return hotBar;
        }

        int inventory = getItemOnNeededSlot(false, item);

        if (inventory != - 1) {
            int wrapSlot = mc.player.getInventory().selectedSlot;
            pickItem(inventory);
            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
            mc.player.swingHand(Hand.MAIN_HAND);
            mc.player.getInventory().selectedSlot = wrapSlot;
//            pickItem(inventory);

            return inventory;
        }
        return -1;
    }

    public static void pickItem(int slotId){
        mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(slotId));
    }

    public static int getItemOnNeededSlot(boolean hotBar, Item item){
        int hot = hotBar ? 0 : 9;
        int inv = hotBar ? 9 : 36;
        int finalSlot = -1;
        for(int i = hot; i < inv; i++){
            ItemStack stack = mc.player.getInventory().getStack(i);

            if(stack.getItem() == item){
                finalSlot = i;
                break;
            }
        }
        return finalSlot;
    }

    public boolean friend() {
        if (mc.player == null || mc.world == null) return false;

        ViewDirection viewDirection = new ViewDirection(
                mc.player.getYaw(),
                mc.player.getPitch()
        );

        EntityHitResult hitResult = ViewTracer.traceEntity(6.0, viewDirection, entity -> entity instanceof PlayerEntity && entity != mc.player);

        if (hitResult == null || !(hitResult.getEntity() instanceof PlayerEntity)) {
            ChatUtility.print("Игрок не найден под курсором");
            return false;
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

        return true;
    }
}
