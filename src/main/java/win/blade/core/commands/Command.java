package win.blade.core.commands;


import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.CommandSource;
import org.jetbrains.annotations.NotNull;
import win.blade.common.utils.minecraft.MinecraftInstance;
import win.blade.common.utils.system.LayoutUtil;

import java.util.ArrayList;
import java.util.List;

public abstract class Command implements MinecraftInstance {
    public abstract void executeBuild(LiteralArgumentBuilder<CommandSource> builder);

    protected static <T> @NotNull RequiredArgumentBuilder<CommandSource, T> arg(final String name, final ArgumentType<T> type) {
        return RequiredArgumentBuilder.argument(name, type);
    }

    protected static @NotNull LiteralArgumentBuilder<CommandSource> literal(final String name) {
        return LiteralArgumentBuilder.literal(name);
    }

    public void register(CommandDispatcher<CommandSource> englishDispatcher, CommandDispatcher<CommandSource> russianDispatcher) {
        CommandInfo info = this.getClass().getAnnotation(CommandInfo.class);
        if (info == null) {
            System.err.println("Команда " + this.getClass().getName() + " потеряла аннотацию @CommandInfo!");
            return;
        }

        List<String> englishNames = new ArrayList<>();
        List<String> russianNames = new ArrayList<>();

        englishNames.add(info.name());
        for (String alias : info.alias()) {
            if (LayoutUtil.isCyrillic(alias)) {
                russianNames.add(alias);
            } else {
                englishNames.add(alias);
            }
        }

        if (!englishNames.isEmpty()) {
            registerForDispatcher(englishDispatcher, englishNames);
        }

        if (!russianNames.isEmpty()) {
            registerForDispatcher(russianDispatcher, russianNames);
        }
    }

    private void registerForDispatcher(CommandDispatcher<CommandSource> dispatcher, List<String> names) {
        if (names.isEmpty()) return;

        LiteralArgumentBuilder<CommandSource> mainBuilder = literal(names.get(0));
        executeBuild(mainBuilder);
        LiteralCommandNode<CommandSource> mainNode = dispatcher.register(mainBuilder);

        for (int i = 1; i < names.size(); i++) {
            LiteralArgumentBuilder<CommandSource> aliasBuilder = literal(names.get(i));
            if (mainNode.getChildren().isEmpty() && mainNode.getCommand() != null) {
                aliasBuilder.executes(mainNode.getCommand());
                dispatcher.register(aliasBuilder);
            } else {
                aliasBuilder.redirect(mainNode);
                dispatcher.register(aliasBuilder);
            }
        }
    }


    public String getName() {
        CommandInfo info = this.getClass().getAnnotation(CommandInfo.class);
        return (info != null) ? info.name() : null;
    }

    public String getAliases() {
        CommandInfo info = this.getClass().getAnnotation(CommandInfo.class);
        return (info != null) ? String.join(", ", info.alias()) : "";
    }

    public String getDescription() {
        CommandInfo info = this.getClass().getAnnotation(CommandInfo.class);
        return (info != null) ? info.description() : "";
    }
}