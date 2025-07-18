package win.blade.core.module.storage.misc;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import win.blade.common.gui.impl.menu.settings.impl.ModeSetting;
import win.blade.common.gui.impl.menu.settings.impl.SliderSetting;
import win.blade.common.utils.minecraft.ChatUtility;
import win.blade.core.event.controllers.EventHandler;
import win.blade.core.event.impl.minecraft.UpdateEvents;
import win.blade.core.event.impl.network.PacketEvent;
import win.blade.core.module.api.Category;
import win.blade.core.module.api.Module;
import win.blade.core.module.api.ModuleInfo;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Автор: NoCap
 * Дата создания: 17.07.2025
 */
@ModuleInfo(
        name = "ChatBot",
        category = Category.MISC,
        desc = "Чат бот для заработка валюты"
)
public class ChatBotModule extends Module {
    private long lastMessageTime = 0;
    private final Random randomGenerator = new Random();

    private final ModeSetting chatMode = new ModeSetting(this, "Режим бота", "Казино", "Кто первый");
    private final SliderSetting cooldown = new SliderSetting(this, "Задержка в секундах", 30, 20, 60, 1f).setVisible(() -> chatMode.is("Казино"));
    private final SliderSetting chance = new SliderSetting(this, "Шанс на выигрыш", 50, 10, 90, 1f).setVisible(() -> chatMode.is("Казино"));
    private final SliderSetting time = new SliderSetting(this, "Время в секундах", 60, 30, 300, 1f).setVisible(() -> !chatMode.is("Казино"));

    private final Map<String, Integer> bids = new HashMap<>();
    private String currentLeader;
    private int currentMin;
    private long auctionEndTime = 0;
    private long nextSignalTime = 0;
    private boolean gameStarted = false;
    private int signalCount = 0;

    private String lastSentPublicMessage = null;
    private long lastSendTime = 0;
    private String pendingChatMessage = null;
    private long retryTime = 0;

    @EventHandler
    public void onUpdate(UpdateEvents.Update e) {
        if (mc.player == null) return;

        long currentTime = System.currentTimeMillis();

        if (lastSentPublicMessage != null && currentTime - lastSendTime > 1000) {
            lastSentPublicMessage = null;
        }

        if (pendingChatMessage != null && currentTime >= retryTime) {
            lastSentPublicMessage = pendingChatMessage;
            lastSendTime = currentTime;
            ChatUtility.sendGlobalChatMessage(pendingChatMessage);
            if (chatMode.is("Казино")) {
                lastMessageTime = currentTime;
            }
            pendingChatMessage = null;
        }

        if (chatMode.is("Казино")) {
            onCasinoUpdate();
        } else if (chatMode.is("Кто первый")) {
            onWhoFirstUpdate();
        }
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive event) {
        if (mc.player == null) return;

        if (event.getPacket() instanceof GameMessageS2CPacket) {
            final GameMessageS2CPacket packet = event.getPacket();
            String message = packet.content().getString().toLowerCase(Locale.ROOT);

            if (message.contains("получено от игрока ")) {
                if (chatMode.is("Казино")) {
                    handleCasino(message);
                } else if (chatMode.is("Кто первый")) {
                    handleWhoFirst(message);
                }
            }

            if (message.contains("подождите перед отправкой сообщения")) {
                long currentTime = System.currentTimeMillis();
                if (lastSentPublicMessage != null) {
                    pendingChatMessage = lastSentPublicMessage;
                    retryTime = currentTime + 5000;
                    lastSentPublicMessage = null;
                }
            }
        }
    }

    private void onCasinoUpdate() {
        long currentTime = System.currentTimeMillis();
        PlayerEntity player = mc.player;

        if (currentTime - lastMessageTime >= cooldown.getValue() * 1000) {
            String playerName = player.getName().getString();
            String msg = "Привет! Я - ваш личный казино бот. Попробуйте свою удачу и отправьте мне деньги /pay " + playerName + " сумма. Взамен, я отправлю вам удвоенную сумму если повезёт. Казино бот работает от суммы " + 5000 + ".";
            lastSentPublicMessage = msg;
            lastSendTime = currentTime;
            ChatUtility.sendGlobalChatMessage(msg);
            lastMessageTime = currentTime;
        }
    }

    private void handleCasino(String message) {
        String[] parts = message.split(" ");
        String senderName = parts[parts.length - 1];

        Pattern pattern = Pattern.compile("\\$(\\d{1,3}(,\\d{3})*)");
        Matcher matcher = pattern.matcher(message);
        int amount = 0;
        if (matcher.find()) {
            String amountStr = matcher.group(1).replace(",", "");
            amount = Integer.parseInt(amountStr);
        }

        if (amount < 5000) {
            mc.player.networkHandler.sendChatCommand("m " + senderName + " Минимальная сумма для игры - " + 5000 + " монет.");
            mc.player.networkHandler.sendChatCommand("pay " + senderName + " " + amount);
            return;
        }

        boolean win = randomGenerator.nextDouble() < chance.getValue() / 100;

        if (win) {
            int winnings = amount * 2;
            mc.player.networkHandler.sendChatCommand("m " + senderName + " Поздравляем! Вы выиграли!");
            mc.player.networkHandler.sendChatCommand("pay " + senderName + " " + winnings);
        } else {
            mc.player.networkHandler.sendChatCommand("m " + senderName + " К сожалению, вы проиграли.");
        }
    }

    private void onWhoFirstUpdate() {
        long currentTime = System.currentTimeMillis();
        String playerName = mc.player.getName().getString();

        if (gameStarted) {
            if (currentTime > auctionEndTime) {
                if (currentLeader != null) {
                    String msg = "Игра завершена! Победитель: " + currentLeader + " с " + currentMin + " монет! Удвоение отправлено.";
                    lastSentPublicMessage = msg;
                    lastSendTime = currentTime;
                    ChatUtility.sendGlobalChatMessage(msg);
                    for (Map.Entry<String, Integer> entry : bids.entrySet()) {
                        if (!entry.getKey().equals(currentLeader)) {
                            mc.player.networkHandler.sendChatCommand("pay " + entry.getKey() + " " + entry.getValue());
                        }
                    }
                    mc.player.networkHandler.sendChatCommand("pay " + currentLeader + " " + (currentMin * 2));
                } else {
                    String msg = "Игра завершена без ставок.";
                    lastSentPublicMessage = msg;
                    lastSendTime = currentTime;
                    ChatUtility.sendGlobalChatMessage(msg);
                }
                bids.clear();
                currentLeader = null;
                currentMin = Integer.MAX_VALUE;
                gameStarted = false;
                nextSignalTime = currentTime + randomGenerator.nextInt(5000) + 5000;
            }
        } else {
            if (nextSignalTime == 0) {
                nextSignalTime = currentTime + randomGenerator.nextInt(10000) + 5000;
                String msg = "Подготовьтесь к игре 'Кто первый' на x2 деньги! Победит тот, кто после 'старт' первым скинет деньги за " + time.getValue() + " сек. Отправляйте /pay " + playerName + " сумма";
                lastSentPublicMessage = msg;
                lastSendTime = currentTime;
                ChatUtility.sendGlobalChatMessage(msg);
            }
            if (currentTime > nextSignalTime) {
                if (signalCount % 3 == 0 && signalCount != 0) {
                    String msg = "Подготовьтесь к игре 'Кто первый' на x2 деньги! Победит тот, кто после 'старт' первым скинет деньги за " + time.getValue() + " сек. Отправляйте /pay " + playerName + " сумма";
                    lastSentPublicMessage = msg;
                    lastSendTime = currentTime;
                    ChatUtility.sendGlobalChatMessage(msg);
                }
                if (randomGenerator.nextDouble() < 0.2) {
                    String msg = "старт";
                    lastSentPublicMessage = msg;
                    lastSendTime = currentTime;
                    ChatUtility.sendGlobalChatMessage(msg);
                    gameStarted = true;
                    auctionEndTime = (long) (currentTime + time.getValue() * 1000);
                    bids.clear();
                    currentLeader = null;
                    currentMin = Integer.MAX_VALUE;
                } else {
                    String[] fakes = {"$тарт", "start", "старt"};
                    String fake = fakes[randomGenerator.nextInt(fakes.length)];
                    lastSentPublicMessage = fake;
                    lastSendTime = currentTime;
                    ChatUtility.sendGlobalChatMessage(fake);
                }
                signalCount++;
                nextSignalTime = currentTime + randomGenerator.nextInt(15000) + 5000;
            }
        }
    }

    private void handleWhoFirst(String message) {
        String[] parts = message.split(" ");
        String senderName = parts[parts.length - 1];

        Pattern pattern = Pattern.compile("\\$(\\d{1,3}(,\\d{3})*)");
        Matcher matcher = pattern.matcher(message);
        int amount = 0;
        if (matcher.find()) {
            String amountStr = matcher.group(1).replace(",", "");
            amount = Integer.parseInt(amountStr);
        }

        if (amount <= 0) return;

        if (!gameStarted) {
            mc.player.networkHandler.sendChatCommand("m " + senderName + " Это был фальшстарт! Платеж не засчитан.");
            mc.player.networkHandler.sendChatCommand("pay " + senderName + " " + amount);
            return;
        }

        bids.put(senderName, bids.getOrDefault(senderName, 0) + amount);
        int total = bids.get(senderName);

        if (total < currentMin) {
            currentMin = total;
            currentLeader = senderName;
            long currentTime = System.currentTimeMillis();
            String msg = "Новый лидер (самый быстрый): " + senderName + " с " + total + "!";
            lastSentPublicMessage = msg;
            lastSendTime = currentTime;
            ChatUtility.sendGlobalChatMessage(msg);
        }
    }
}