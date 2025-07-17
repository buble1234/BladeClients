package win.blade.core.module.storage.misc;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import win.blade.common.gui.impl.menu.settings.impl.SliderSetting;
import win.blade.common.utils.minecraft.ChatUtility;
import win.blade.core.event.controllers.EventHandler;
import win.blade.core.event.impl.minecraft.UpdateEvents;
import win.blade.core.event.impl.network.PacketEvent;
import win.blade.core.module.api.Category;
import win.blade.core.module.api.Module;
import win.blade.core.module.api.ModuleInfo;

import java.util.Locale;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Автор: NoCap
 * Дата создания: 17.07.2025
 */
@ModuleInfo(
        name = "CasinoBot",
        category = Category.PLAYER
)
public class CasinoBotModule extends Module {
    private long lastMessageTime = 0;
    private final Random randomGenerator = new Random();

    private final SliderSetting cooldown = new SliderSetting(this, "Задержка в секундах", 30, 20, 60, 1f);
    private final SliderSetting chance = new SliderSetting(this, "Шанс на выигрыш", 50, 10, 90, 1f);

    @EventHandler
    public void onUpdate(UpdateEvents.Update e) {
        if (mc.player == null) return;

        long currentTime = System.currentTimeMillis();
        PlayerEntity player = mc.player;

        if (currentTime - lastMessageTime >= cooldown.getValue() * 1000) {
            String playerName = player.getName().getString();
            ChatUtility.sendGlobalChatMessage("Привет! Я - ваш личный казино бот. Попробуйте свою удачу и отправьте мне деньги /pay " + playerName + " сумма. Взамен, я отправлю вам удвоенную сумму если повезёт. Казино бот работает от суммы " + 5000 + ".");
            lastMessageTime = currentTime;
        }
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive event) {
        if (mc.player == null) return;

        if (event.getPacket() instanceof GameMessageS2CPacket) {
            final GameMessageS2CPacket packet = event.getPacket();
            String message = packet.content().getString().toLowerCase(Locale.ROOT);

            if (message.contains("получено от игрока ")) {
                handleReceivedMessage(message);
            }
        }
    }

    private void handleReceivedMessage(String message) {
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
            return;
        }

        boolean win = randomGenerator.nextDouble() < chance.getValue() / 100;

        if (win) {
            int winnings = amount * 2;
            mc.player.networkHandler.sendChatCommand("m " + senderName + " Поздравляем! Вы выиграли!");
            mc.player.networkHandler.sendChatCommand("pay " + senderName + " " + winnings);
            mc.player.networkHandler.sendChatCommand("pay " + senderName + " " + winnings);
        } else {
            mc.player.networkHandler.sendChatCommand("m " + senderName + " К сожалению, вы проиграли.");
        }
    }
}
