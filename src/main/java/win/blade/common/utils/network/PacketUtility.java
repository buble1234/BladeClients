package win.blade.common.utils.network;

import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.message.LastSeenMessageList;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.item.ItemStack;

import java.time.Instant;
import java.util.BitSet;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.List;
import java.util.ArrayList;

import static win.blade.common.utils.minecraft.MinecraftInstance.mc;

/**
 * Автор: NoCap
 * Дата создания: 17.06.2025
 */

public class PacketUtility {
    
    private static final AtomicInteger sentPackets = new AtomicInteger(0);
    private static final AtomicInteger receivedPackets = new AtomicInteger(0);
    private static final AtomicLong lastPacketTime = new AtomicLong(0);
    
    private static final ConcurrentLinkedQueue<PacketData> packetQueue = new ConcurrentLinkedQueue<>();
    
    private static final List<Consumer<Packet<?>>> packetListeners = new ArrayList<>();
    

    /**
     * Отправляет пакет на сервер
     */
    public static void sendPacket(Packet<?> packet) {
        if (mc.getNetworkHandler() == null) return;
        
        try {
            mc.getNetworkHandler().sendPacket(packet);
            sentPackets.incrementAndGet();
            lastPacketTime.set(System.currentTimeMillis());
        } catch (Exception e) {
            System.err.println("Ошибка отправки пакета: " + e.getMessage());
        }
    }

    /**
     * Отправляет пакет с задержкой
     */
    public static void sendPacketDelayed(Packet<?> packet, long delayMs) {
        CompletableFuture.delayedExecutor(delayMs, java.util.concurrent.TimeUnit.MILLISECONDS)
                .execute(() -> sendPacket(packet));
    }
    
    /**
     * Отправляет пакет в очередь
     */
    public static void queuePacket(Packet<?> packet, int priority) {
        packetQueue.offer(new PacketData(packet, priority, System.currentTimeMillis()));
    }
    
    /**
     * Обрабатывает очередь пакетов
     */
    public static void processPacketQueue() {
        while (!packetQueue.isEmpty()) {
            PacketData data = packetQueue.poll();
            if (data != null) {
                sendPacket(data.packet);
            }
        }
    }

    /**
     * Отправляет пакет позиции игрока
     */
    public static void sendPosition(double x, double y, double z, boolean onGround, boolean horizontalCollision) {
        if (mc.player == null) return;
        sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y, z, onGround, horizontalCollision));
    }
    
    /**
     * Отправляет пакет поворота игрока
     */
    public static void sendRotation(float yaw, float pitch, boolean onGround, boolean horizontalCollision) {
        if (mc.player == null) return;
        sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(yaw, pitch, onGround, horizontalCollision));
    }
    
    /**
     * Отправляет пакет позиции и поворота
     */
    public static void sendPositionAndRotation(double x, double y, double z, float yaw, float pitch, boolean onGround, boolean horizontalCollision) {
        if (mc.player == null) return;
        sendPacket(new PlayerMoveC2SPacket.Full(x, y, z, yaw, pitch, onGround, horizontalCollision));
    }
    
    /**
     * Отправляет пакет onGround
     */
    public static void sendOnGroundOnly(boolean onGround, boolean horizontalCollision) {
        if (mc.player == null) return;
        sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(onGround, horizontalCollision));
    }
    
    /**
     * Телепортирует игрока (только клиентская сторона)
     */
    public static void teleportPlayer(double x, double y, double z, boolean horizontalCollision) {
        if (mc.player == null) return;
        mc.player.setPosition(x, y, z);
        sendPosition(x, y, z, mc.player.isOnGround(), horizontalCollision);
    }

    /**
     * Отправляет пакет использования предмета
     */
    public static void sendUseItem(Hand hand, int sequence, float yaw, float pitch) {
        sendPacket(new PlayerInteractItemC2SPacket(hand, sequence, yaw, pitch));
    }
    
    /**
     * Отправляет пакет взаимодействия с блоком
     */
    public static void sendInteractBlock(BlockPos pos, Direction direction, Hand hand, int sequence) {
        Vec3d hitVec = new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        BlockHitResult hitResult = new BlockHitResult(hitVec, direction, pos, false);
        sendPacket(new PlayerInteractBlockC2SPacket(hand, hitResult, sequence));
    }
    
    /**
     * Отправляет пакет атаки сущности
     */
    public static void sendAttackEntity(Entity entity, boolean sneaking) {
        sendPacket(PlayerInteractEntityC2SPacket.attack(entity, sneaking));
    }
    
    /**
     * Отправляет пакет взаимодействия с сущностью
     */
    public static void sendInteractEntity(Entity entity, Hand hand, boolean sneaking) {
        sendPacket(PlayerInteractEntityC2SPacket.interact(entity, sneaking, hand));
    }

    /**
     * Отправляет пакет действия игрока
     */
    public static void sendPlayerAction(PlayerActionC2SPacket.Action action, BlockPos pos, Direction direction) {
        sendPacket(new PlayerActionC2SPacket(action, pos, direction));
    }
    
    /**
     * Начинает копание блока
     */
    public static void startDigging(BlockPos pos, Direction direction) {
        sendPlayerAction(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, pos, direction);
    }
    
    /**
     * Заканчивает копание блока
     */
    public static void finishDigging(BlockPos pos, Direction direction) {
        sendPlayerAction(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, pos, direction);
    }
    
    /**
     * Отменяет копание блока
     */
    public static void cancelDigging(BlockPos pos, Direction direction) {
        sendPlayerAction(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, pos, direction);
    }
    
    /**
     * Начинает использование предмета
     */
    public static void startUsingItem() {
        sendPlayerAction(PlayerActionC2SPacket.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, Direction.DOWN);
    }
    
    /**
     * Меняет руку
     */
    public static void swapHands() {
        sendPlayerAction(PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ORIGIN, Direction.DOWN);
    }

    /**
     * Отправляет клик по слоту
     */
    public static void sendSlotClick(int syncId, int slot, int button, SlotActionType actionType) {
        if (mc.player == null) return;
        Int2ObjectMap<ItemStack> modifiedStacks = new Int2ObjectOpenHashMap<>();
        sendPacket(new ClickSlotC2SPacket(syncId, mc.player.currentScreenHandler.getRevision(), slot, button, actionType, mc.player.currentScreenHandler.getCursorStack(), modifiedStacks));
    }

    /**
     * Закрывает контейнер
     */
    public static void closeContainer(int syncId) {
        sendPacket(new CloseHandledScreenC2SPacket(syncId));
    }
    
    /**
     * Отправляет обновление творческого инвентаря
     */
    public static void sendCreativeInventoryAction(int slot, ItemStack stack) {
        sendPacket(new CreativeInventoryActionC2SPacket(slot, stack));
    }

    /**
     * Отправляет сообщение в чат
     */
    public static void sendChatMessage(String message) {
        sendPacket(new ChatMessageC2SPacket(message, Instant.now(), 0L, null, new LastSeenMessageList.Acknowledgment(0, new BitSet())));
    }
    
    /**
     * Отправляет команду
     */
    public static void sendCommand(String command) {
        if (!command.startsWith("/")) {
            command = "/" + command;
        }
        sendChatMessage(command);
    }

    /**
     * Отправляет пакет анимации руки
     */
    public static void sendHandSwing(Hand hand) {
        sendPacket(new HandSwingC2SPacket(hand));
    }
    
    /**
     * Отправляет пакет приседания
     */
    public static void sendSneaking(boolean sneaking) {
        sendPacket(new ClientCommandC2SPacket(mc.player, 
                sneaking ? ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY : ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
    }
    
    /**
     * Отправляет пакет спринта
     */
    public static void sendSprinting(boolean sprinting) {
        sendPacket(new ClientCommandC2SPacket(mc.player, 
                sprinting ? ClientCommandC2SPacket.Mode.START_SPRINTING : ClientCommandC2SPacket.Mode.STOP_SPRINTING));
    }
    
    /**
     * Отправляет пакет обновления способностей
     */
    public static void sendAbilities() {
        if (mc.player == null) return;
        sendPacket(new UpdatePlayerAbilitiesC2SPacket(mc.player.getAbilities()));
    }

    /**
     * Создает кастомный пакет
     */
    public static PacketByteBuf createCustomPacket() {
        return new PacketByteBuf(Unpooled.buffer());
    }
    
    /**
     * Отправляет кастомный пакет
     */
    public static void sendCustomPacket(String channel, PacketByteBuf data) {
        // Todo: реализовать
        System.out.println("Отправка кастомного пакета на канал: " + channel);
    }

    /**
     * Получает количество отправленных пакетов
     */
    public static int getSentPacketCount() {
        return sentPackets.get();
    }
    
    /**
     * Получает количество полученных пакетов
     */
    public static int getReceivedPacketCount() {
        return receivedPackets.get();
    }
    
    /**
     * Получает время последнего пакета
     */
    public static long getLastPacketTime() {
        return lastPacketTime.get();
    }
    
    /**
     * Сбрасывает статистику
     */
    public static void resetStats() {
        sentPackets.set(0);
        receivedPackets.set(0);
        lastPacketTime.set(0);
    }
    
    /**
     * Получает пинг до сервера
     */
    public static int getPing() {
        if (mc.getNetworkHandler() == null) return -1;
        try {
            return mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid()).getLatency();
        } catch (Exception e) {
            return -1;
        }
    }
    
    /**
     * Проверяет подключение к серверу
     */
    public static boolean isConnectedToServer() {
        return mc.getNetworkHandler() != null && mc.getNetworkHandler().getConnection().isOpen();
    }

    /**
     * Добавляет слушатель пакетов
     */
    public static void addPacketListener(Consumer<Packet<?>> listener) {
        packetListeners.add(listener);
    }
    
    /**
     * Удаляет слушатель пакетов
     */
    public static void removePacketListener(Consumer<Packet<?>> listener) {
        packetListeners.remove(listener);
    }
    
    /**
     * Вызывается при получении пакета
     */
    public static void onPacketReceived(Packet<?> packet) {
        receivedPackets.incrementAndGet();
        lastPacketTime.set(System.currentTimeMillis());
        
        for (Consumer<Packet<?>> listener : packetListeners) {
            try {
                listener.accept(packet);
            } catch (Exception e) {
                System.err.println("Ошибка в слушателе пакетов: " + e.getMessage());
            }
        }
    }

    public static class PacketData {
        public final Packet<?> packet;
        public final int priority;
        public final long timestamp;
        
        public PacketData(Packet<?> packet, int priority, long timestamp) {
            this.packet = packet;
            this.priority = priority;
            this.timestamp = timestamp;
        }
    }
    
    /**
     * Статистика сети
     */
    public static class NetworkStats {
        public final int sentPackets;
        public final int receivedPackets;
        public final long lastPacketTime;
        public final int ping;
        public final boolean connected;
        
        public NetworkStats() {
            this.sentPackets = getSentPacketCount();
            this.receivedPackets = getReceivedPacketCount();
            this.lastPacketTime = getLastPacketTime();
            this.ping = getPing();
            this.connected = isConnectedToServer();
        }
        
        @Override
        public String toString() {
            return String.format("NetworkStats{sent=%d, received=%d, ping=%d, connected=%s}", 
                    sentPackets, receivedPackets, ping, connected);
        }
    }
    
    /**
     * Получает полную статистику сети
     */
    public static NetworkStats getNetworkStats() {
        return new NetworkStats();
    }
}