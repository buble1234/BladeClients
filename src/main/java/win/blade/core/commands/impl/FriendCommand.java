package win.blade.core.commands.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;
import win.blade.common.utils.friends.FriendManager;
import win.blade.common.utils.minecraft.ChatUtility;
import win.blade.common.utils.other.Result;
import win.blade.core.commands.Command;
import win.blade.core.commands.CommandInfo;

/**
 * Автор Ieo117
 * Дата создания: 16.07.2025, в 17:29:14
 */
@CommandInfo(name = "friends")
public class FriendCommand extends Command {

    @Override
    public void executeBuild(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(literal("add").then(arg("friend_name", StringArgumentType.string()).executes(context -> {
            String name = StringArgumentType.getString(context, "friend_name");

            if(name.length() < 3){
               ChatUtility.print("Не корректный ник. В нике должно быть хотя бы 3 символа");
               return 1;
            }

            var result = FriendManager.instance.add(name);

            ChatUtility.printResult(result, "Друг: %s успешно сохранён".formatted(name));

            return 1;
        })));


        builder.then(literal("remove").then(arg("friend_name", StringArgumentType.string()).executes(context -> {
            String friendName = StringArgumentType.getString(context, "friend_name");

            Result<Boolean, String> result = FriendManager.instance.removeFriend(friendName);

            ChatUtility.printResult(result, "Игрок: %s был успешно удалён из списка друзей!".formatted(friendName));

            return 1;
        })));

        builder.then(literal("list").executes(context -> {
            var friendList = FriendManager.instance.friendList;

            if(friendList.isEmpty()){
                ChatUtility.print("Список друзуй пуст");
                return 1;
            }

            StringBuilder message = new StringBuilder();

            message.append("Список друзей: \n");

            friendList.forEach(friend -> message.append(friend.name()).append("\n"));

            ChatUtility.print(message.toString());

            return 1;
        }));
    }
}
