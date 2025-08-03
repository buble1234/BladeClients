package win.blade.core.commands.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.command.CommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import win.blade.common.utils.config.ConfigManager;
import win.blade.common.utils.minecraft.ChatUtility;
import win.blade.core.commands.Command;
import win.blade.core.commands.CommandInfo;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

/**
 * Автор Ieo117
 * Дата создания: 16.07.2025, в 00:32:02
 */

@CommandInfo(name = "config")
public class ConfigCommand extends Command {
    private static final ConfigManager configManager = ConfigManager.instance;

    private static List<String> getConfigNames() {
        return configManager.getAllConfigs();
    }

    private static final SuggestionProvider<CommandSource> CONFIG_SUGGESTION_PROVIDER = (context, builder) ->
            CommandSource.suggestMatching(getConfigNames(), builder);

    @Override
    public void executeBuild(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(LiteralArgumentBuilder.<CommandSource>literal("save")
                .then(RequiredArgumentBuilder.<CommandSource, String>argument("name", StringArgumentType.word())
                        .executes(context -> {
                            String configName = StringArgumentType.getString(context, "name");
                            CompletableFuture.runAsync(() -> {
                                configManager.saveConfig(configName);
                                ChatUtility.add(Text.literal("Конфигурация сохранена: " + configName)
                                        .formatted(Formatting.RED));
                            });
                            return SINGLE_SUCCESS;
                        })));

        builder.then(LiteralArgumentBuilder.<CommandSource>literal("load")
                .then(RequiredArgumentBuilder.<CommandSource, String>argument("name", StringArgumentType.word())
                        .suggests(CONFIG_SUGGESTION_PROVIDER)
                        .executes(context -> {
                            String configName = StringArgumentType.getString(context, "name");
                            ConfigManager.instance.loadConfig(configName);
                            return SINGLE_SUCCESS;
                        })));

        builder.then(LiteralArgumentBuilder.<CommandSource>literal("delete")
                .then(RequiredArgumentBuilder.<CommandSource, String>argument("name", StringArgumentType.word())
                        .suggests(CONFIG_SUGGESTION_PROVIDER)
                        .executes(context -> {
                            String configName = StringArgumentType.getString(context, "name");
                            if (configManager.deleteConfig(configName)) {
                                ChatUtility.add(Text.literal("Конфигурация удалена: " + configName).formatted(Formatting.RED));
                            } else {
                                ChatUtility.add(Text.literal("Конфигурация не найдена: " + configName).formatted(Formatting.RED));
                            }
                            return SINGLE_SUCCESS;
                        })));

        builder.then(LiteralArgumentBuilder.<CommandSource>literal("list")
                .executes(context -> {
//                    CONFIG_MANAGER.update();
                    ChatUtility.add(Text.literal("Доступные конфигурации:")
                            .formatted(Formatting.RED));
                    configManager.getAllConfigs().forEach(name -> {
                        Text configText = Text.literal("> " + name)
                                .setStyle(Style.EMPTY
                                        .withColor(Formatting.AQUA)
                                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, ".cfg load " + name))
                                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Конфиг: " + name).formatted(Formatting.RED))));
                        ChatUtility.add(configText);
                    });

                    return SINGLE_SUCCESS;
                }));

        builder.then(LiteralArgumentBuilder.<CommandSource>literal("dir")
                .executes(context -> {
                    try {
                        Util.getOperatingSystem().open(ConfigManager.instance.configDirectory);
                        ChatUtility.add(Text.literal("Папка с конфигурациями открыта")
                                .formatted(Formatting.RED));
                    } catch (Exception e) {
                        ChatUtility.add(Text.literal("Не удалось открыть папку: " + e.getMessage())
                                .formatted(Formatting.RED));
                        // e.printStackTrace(); // Or log
                    }
                    return SINGLE_SUCCESS;
                }));
    }
}
