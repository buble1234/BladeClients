package win.blade.core.module.storage.player;

import win.blade.common.gui.impl.gui.setting.implement.ValueSetting;
import win.blade.core.event.controllers.EventHandler;
import win.blade.core.event.impl.minecraft.UpdateEvents;
import win.blade.core.module.api.Category;
import win.blade.core.module.api.Module;
import win.blade.core.module.api.ModuleInfo;

import java.util.Random;
import java.util.Arrays;
import java.util.List;

/**
 * Автор: NoCap
 * Дата создания: 17.07.2025
 */
@ModuleInfo(
        name = "AntiAFK",
        category = Category.PLAYER,
        desc = "Предотвращает кик за бездействие"
)
public class AntiAFKModule extends Module {

    private final ValueSetting cooldown = new ValueSetting("Задержка в минутах", "").setValue(2f).range(1f, 10f);

    private long lastActionTime = 0;

    private final List<String> randomMessages = Arrays.asList(
            "привет",
            "как дела",
            "лол",
            "ого",
            "круто",
            "я тут",
            "ок",
            "ыыыыы",
            "вфызщхвхызфвзыфвыф",
            "норм",
            "ага",
            "точно",
            "понял",
            "отлично",
            "да",
            "нет",
            "спасибо"
    );

    private final Random random = new Random();

    @EventHandler
    public void onUpdate(UpdateEvents.Update event) {
        if (mc.player == null || mc.world == null) {
            return;
        }

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastActionTime >= cooldown.getValue() * 60 * 1000) {
            performAction();
            lastActionTime = currentTime;
        }
    }

    private void performAction() {
        String messageToSend = randomMessages.get(random.nextInt(randomMessages.size()));
        mc.player.networkHandler.sendChatCommand("say " + messageToSend);
    }
}