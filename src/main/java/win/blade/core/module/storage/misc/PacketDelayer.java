//package win.blade.core.module.storage.misc;
//
//import net.minecraft.network.packet.Packet;
//import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
//import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
//import win.blade.common.gui.impl.menu.settings.impl.ModeSetting;
//import win.blade.common.gui.impl.menu.settings.impl.SliderSetting;
//import win.blade.common.utils.minecraft.ChatUtility;
//import win.blade.core.event.controllers.EventHandler;
//import win.blade.core.event.impl.minecraft.UpdateEvents;
//import win.blade.core.event.impl.network.PacketEvent;
//import win.blade.core.module.api.BindMode;
//import win.blade.core.module.api.Category;
//import win.blade.core.module.api.Module;
//import win.blade.core.module.api.ModuleInfo;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.concurrent.CopyOnWriteArrayList;
//
///**
// * Автор Ieo117
// * Дата создания: 20.06.2025, в 14:08:04
// */
//
//@ModuleInfo(name = "PacketDelayer", category = Category.MISC, mode = BindMode.УДЕРЖИВАТЬ)
//public class PacketDelayer extends Module {
//    public ModeSetting packetMode = new ModeSetting(this, "Задерживать пакет на", "Ходьбу", "Ходьбу", "Ротацию", "Удар", "Всё вместе");
//    public SliderSetting maxHold = new SliderSetting(this, "Максимальное время задержки (сек)", 2f, 0.25f, 10f, 0.05f);
//
//    public List<Packet<?>> delayedPackets = new CopyOnWriteArrayList<>();
//    public long startTime = System.currentTimeMillis();
//
//    @Override
//    public void onEnable(){
//        super.onEnable();
//
//        startTime = System.currentTimeMillis();
//    }
//
//    @Override
//    protected void onDisable() {
//        assert mc.getNetworkHandler() == null;
//        delayedPackets.forEach(mc.getNetworkHandler()::sendPacket);
//        ChatUtility.print("Отправил", delayedPackets.size(), "пакетов");
//        delayedPackets.clear();
//
//        super.onDisable();
//    }
//
//    @EventHandler
//    public void onUpdate(UpdateEvents.Update e){
//        if(System.currentTimeMillis() - startTime > maxHold.getValue() * 1000L){
//            setEnabled(false);
//        }
//
////        if(delayedPackets.size() >= 8){
////            for (Packet<?> delayedPacket : delayedPackets) {
////                mc.getNetworkHandler().sendPacket(delayedPacket);
////            }
////            delayedPackets.clear();
////        }
//
//    }
//
//
//    @EventHandler
//    public void onPacket(PacketEvent.Send e){
////        ChatUtility.print("fs");
//        var packet = e.getPacket();
//        Packet<?> toAdd = null;
//        if(packetMode.is(3)){
//            if(packet instanceof PlayerInteractEntityC2SPacket p) toAdd = p;
//            else if (packet instanceof PlayerMoveC2SPacket p) toAdd = p;
//        } else if (packetMode.is(2) && packet instanceof PlayerInteractEntityC2SPacket p) {
//            toAdd = p;
//        } else if (packetMode.is(1) && packet instanceof PlayerMoveC2SPacket.LookAndOnGround p) {
//            toAdd = p;
//        } else if (packetMode.is(0) && packet instanceof PlayerMoveC2SPacket.Full p) {
//            toAdd = p;
//        }
//
//        if(toAdd == null) return;
//
//        delayedPackets.add(toAdd);
//        e.cancel();
////        ChatUtility.print("Отменил " + toAdd.toString());
//    }
//
//
//
//}
