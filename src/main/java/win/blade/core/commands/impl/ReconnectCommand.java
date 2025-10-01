package win.blade.core.commands.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.realms.gui.screen.RealmsMainScreen;
import net.minecraft.command.CommandSource;
import win.blade.common.gui.impl.gui.MenuScreen;
import win.blade.common.gui.impl.screen.multiplayer.MultiplayerScreen;
import win.blade.common.gui.impl.screen.options.PauseScreen;
import win.blade.core.Manager;
import win.blade.core.commands.Command;
import win.blade.core.commands.CommandInfo;

import java.util.concurrent.TimeUnit;

/**
 * Автор Ieo117
 * Дата создания: 01.10.2025, в 16:39:50
 */
@CommandInfo(name = "reconnect")
public class ReconnectCommand extends Command {

//    public ReconnectCommand(){
//        super("");
//    }


    @Override
    public void executeBuild(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(s -> {
//            boolean bl = mc.isInSingleplayer();
//            mc.execute(() -> {
//                mc.disconnect();
//                var screen = new MultiplayerScreen();
//                mc.setScreen(screen);
//                Manager.executorService.schedule(() -> {
//                    ConnectScreen.connect(screen, mc, ServerAddress.parse(entry.address), entry, false, null);
//                }, 500, TimeUnit.MILLISECONDS);
//            });
            ServerInfo entry = mc.getCurrentServerEntry();
            PauseScreen.disconnect(mc);
            ConnectScreen.connect(new MenuScreen(), mc, ServerAddress.parse(entry.address), entry, false, null);

            return 1;
        });
    }
}
