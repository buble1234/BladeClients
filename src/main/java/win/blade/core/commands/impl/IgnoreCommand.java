package win.blade.core.commands.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import win.blade.common.utils.ignore.IgnoreManager;
import win.blade.common.utils.minecraft.ChatUtility;
import win.blade.common.utils.other.Result;
import win.blade.core.commands.Command;
import win.blade.core.commands.CommandInfo;
import win.blade.core.commands.ISuggestionProvider;

@CommandInfo(name = "ignore")
public class IgnoreCommand extends Command {

    @Override
    public void executeBuild(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(literal("add")
                .then(arg("player_name", StringArgumentType.string())
                        .suggests(ISuggestionProvider.ONLINE_PLAYERS)
                        .executes(context -> {
                            String name = StringArgumentType.getString(context, "player_name");

                            if(name.length() < 3){
                                ChatUtility.print("Некорректный ник. В нике должно быть хотя бы 3 символа");
                                return 1;
                            }

                            var result = IgnoreManager.instance.add(name);
                            ChatUtility.printResult(result, "Игрок: %s успешно добавлен в игнор".formatted(name));
                            return 1;
                        })));

        builder.then(literal("remove")
                .then(arg("player_name", StringArgumentType.string())
                        .executes(context -> {
                            String playerName = StringArgumentType.getString(context, "player_name");
                            Result<Boolean, String> result = IgnoreManager.instance.removeIgnored(playerName);
                            ChatUtility.printResult(result, "Игрок: %s был успешно удалён из игнора!".formatted(playerName));
                            return 1;
                        })));

        builder.then(literal("list")
                .executes(context -> {
                    var ignoreList = IgnoreManager.instance.ignoreList;

                    if(ignoreList.isEmpty()){
                        ChatUtility.print("Список игнорируемых пуст");
                        return 1;
                    }

                    StringBuilder message = new StringBuilder();
                    message.append("Список игнорируемых игроков: \n");
                    ignoreList.forEach(ignore -> message.append(ignore.name()).append("\n"));
                    ChatUtility.print(message.toString());
                    return 1;
                }));
    }
}