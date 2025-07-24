//package win.blade.core.module.storage.misc;
//
//import net.minecraft.network.packet.c2s.common.ResourcePackStatusC2SPacket;
//import net.minecraft.network.packet.s2c.common.ResourcePackSendS2CPacket;
//import win.blade.common.utils.math.TimerUtil;
//import win.blade.core.event.controllers.EventHandler;
//import win.blade.core.event.impl.minecraft.UpdateEvents;
//import win.blade.core.event.impl.network.PacketEvent;
//import win.blade.core.module.api.Category;
//import win.blade.core.module.api.Module;
//import win.blade.core.module.api.ModuleInfo;
//
//import java.util.UUID;
//
///**
// * Автор: NoCap
// * Дата создания: 22.07.2025
// */
//@ModuleInfo(
//        name = "ServerRPSpoofer",
//        category = Category.MISC
//)
//public class ServerRPSpoofer extends Module {
//
//    ResourcePackAction currentAction = ResourcePackAction.WAIT;
//    TimerUtil timer = TimerUtil.create();
//
//    @EventHandler
//    public void onPacket(PacketEvent packetEvent) {
//        if (packetEvent.getPacket() instanceof ResourcePackSendS2CPacket) {
//            currentAction = ResourcePackAction.ACCEPT;
//            packetEvent.cancel();
//        }
//    }
//
//    @EventHandler
//    public void onUpdate(UpdateEvents.Update updateEvent) {
//        if (mc.getNetworkHandler() != null) {
//            UUID uniqueId = UUID.randomUUID();
//            if (currentAction == ResourcePackAction.ACCEPT) {
//                mc.getNetworkHandler().sendPacket(new ResourcePackStatusC2SPacket(uniqueId, ResourcePackStatusC2SPacket.Status.ACCEPTED));
//                currentAction = ResourcePackAction.SEND;
//                timer.resetTimer();
//            } else if (currentAction == ResourcePackAction.SEND && timer.isReached(300L)) {
//                mc.getNetworkHandler().sendPacket(new ResourcePackStatusC2SPacket(uniqueId, ResourcePackStatusC2SPacket.Status.SUCCESSFULLY_LOADED));
//                currentAction = ResourcePackAction.WAIT;
//            }
//        }
//    }
//
//    @Override
//    public void onDisable() {
//        currentAction = ResourcePackAction.WAIT;
//        super.onDisable();
//    }
//
//    public enum ResourcePackAction {
//        ACCEPT, SEND, WAIT;
//    }
//}
