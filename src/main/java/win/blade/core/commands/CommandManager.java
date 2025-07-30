package win.blade.core.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientCommandSource;
import net.minecraft.command.CommandSource;
import org.jetbrains.annotations.NotNull;
import win.blade.common.utils.minecraft.MinecraftInstance;
import win.blade.common.utils.system.LayoutUtil;
import win.blade.core.commands.impl.*;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

import static win.blade.core.Manager.EVENT_BUS;

public class CommandManager implements MinecraftInstance {
    private String prefix = ".";
    private final CommandDispatcher<CommandSource> englishDispatcher = new CommandDispatcher<>();
    private final CommandDispatcher<CommandSource> russianDispatcher = new CommandDispatcher<>();
    private final CommandSource source = new ClientCommandSource(null, MinecraftClient.getInstance());
    private final List<Command> commands = new ArrayList<>();


    public CommandManager() {
        add(new HelpCommand());
        //add(new ConfigCommand());
        add(new FriendCommand());
        add(new BlockESPCommand());
        add(new ToggleCommand());
        add(new BindCommand());
    }

    private void add(@NotNull Command command) {
        command.register(englishDispatcher, russianDispatcher);
        EVENT_BUS.registerLambdaFactory("ru.blade",
                (lookupInMethod, klass) -> (MethodHandles.Lookup) lookupInMethod.invoke(null, klass, MethodHandles.lookup()));
        EVENT_BUS.subscribe(command);
        commands.add(command);
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String newPrefix) {
        this.prefix = newPrefix;
    }

    public Command get(Class<? extends Command> commandClass) {
        for (Command command : commands) {
            if (command.getClass().equals(commandClass)) return command;
        }
        return null;
    }

    public List<Command> getCommands() {
        return commands;
    }

    public CommandSource getSource() {
        return source;
    }

    public CommandDispatcher<CommandSource> getActiveDispatcher() {
        return LayoutUtil.isRussianLayout() ? russianDispatcher : englishDispatcher;
    }

    public void registerCommand(Command command) {
        if (command == null) return;
        command.register(englishDispatcher, russianDispatcher);
        this.commands.add(command);
    }



    public CommandDispatcher<CommandSource> getEnglish() {
        return englishDispatcher;
    }

    public CommandDispatcher<CommandSource> getRussian() {
        return russianDispatcher;
    }
}