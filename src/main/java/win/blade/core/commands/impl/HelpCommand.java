package win.blade.core.commands.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import win.blade.core.Manager;
import win.blade.core.commands.Command;
import win.blade.core.commands.CommandInfo;


import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

@CommandInfo(name = "help", description = "Показывает список команд", alias = {"хелп"})
public class HelpCommand extends Command {

    @Override
    public void executeBuild(@NotNull LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            Manager.getCommandManager().getCommands().forEach(command -> {
                CommandInfo info = command.getClass().getAnnotation(CommandInfo.class);

                if (info != null) {
                    String name = info.name();
                    String aliases = String.join(", ", info.alias());
                    String description = info.description();

                    MutableText fullMessage = Text.literal("[" + name + "]").formatted(Formatting.WHITE)
                            .append(Text.literal(aliases.isEmpty() ? "" : " §7(" + aliases + ")").formatted(Formatting.GRAY))
                            .append(Text.literal(" ").formatted(Formatting.RESET))
                            .append(Text.literal("➜").formatted(Formatting.WHITE))
                            .append(Text.literal(" " + description).formatted(Formatting.WHITE));

                    mc.player.sendMessage(fullMessage,false);
                }
            });

            return SINGLE_SUCCESS;
        });
    }
}