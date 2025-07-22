package win.blade.core.commands.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.client.util.InputUtil;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import win.blade.core.Manager;
import win.blade.core.commands.Command;
import win.blade.core.commands.CommandInfo;

import java.util.Locale;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

/**
 * Автор: NoCap
 * Дата создания: 22.07.2025
 */
@CommandInfo(name = "bind", description = "Привязывает модуль к клавише", alias = {"бинд"})
public class BindCommand extends Command {

    @Override
    public void executeBuild(@NotNull LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(RequiredArgumentBuilder.<CommandSource, String>argument("module", StringArgumentType.word())
                .suggests((context, suggestionsBuilder) -> {
                    Manager.getModuleManagement().all().forEach(module -> suggestionsBuilder.suggest(module.name()));
                    return suggestionsBuilder.buildFuture();
                })
                .then(RequiredArgumentBuilder.<CommandSource, String>argument("key", StringArgumentType.word())
                        .executes(context -> {
                            String moduleName = StringArgumentType.getString(context, "module");
                            String keyName = StringArgumentType.getString(context, "key").toUpperCase();

                            return Manager.getModuleManagement().find(moduleName)
                                    .map(module -> {
                                        if (keyName.equalsIgnoreCase("NONE")) {
                                            module.setKeybind(0);
                                            mc.player.sendMessage(Text.literal("Модуль ").append(Text.literal(module.name()).formatted(Formatting.AQUA)).append(Text.literal(" был отвязан.").formatted(Formatting.WHITE)), false);
                                            return SINGLE_SUCCESS;
                                        }

                                        InputUtil.Key key = InputUtil.fromTranslationKey("key.keyboard." + keyName.toLowerCase(Locale.ROOT));

                                        if (key.getCode() == -1) {
                                            mc.player.sendMessage(Text.literal("Клавиша '").append(Text.literal(keyName).formatted(Formatting.RED)).append(Text.literal("' не найдена.")), false);
                                            return 0;
                                        }

                                        module.setKeybind(key.getCode());
                                        mc.player.sendMessage(Text.literal("Модуль ").append(Text.literal(module.name()).formatted(Formatting.AQUA)).append(Text.literal(" привязан к клавише ")).append(Text.literal(keyName).formatted(Formatting.GOLD)).append(Text.literal(".")), false);

                                        return SINGLE_SUCCESS;
                                    })
                                    .orElseGet(() -> {
                                        mc.player.sendMessage(Text.literal("Модуль '").append(Text.literal(moduleName).formatted(Formatting.RED)).append(Text.literal("' не найден.")), false);
                                        return 0;
                                    });
                        }))
                .executes(context -> {
                    String moduleName = StringArgumentType.getString(context, "module");
                    return Manager.getModuleManagement().find(moduleName)
                            .map(module -> {
                                int keyCode = module.keybind();
                                String keyName = InputUtil.fromKeyCode(keyCode, -1).getLocalizedText().getString().toUpperCase();

                                if (keyCode == 0) {
                                    keyName = "NONE";
                                }

                                mc.player.sendMessage(Text.literal("Модуль ").append(Text.literal(module.name()).formatted(Formatting.AQUA)).append(Text.literal(" привязан к клавише ")).append(Text.literal(keyName).formatted(Formatting.GOLD)).append(Text.literal(".")), false);

                                return SINGLE_SUCCESS;
                            })
                            .orElseGet(() -> {
                                mc.player.sendMessage(Text.literal("Модуль '").append(Text.literal(moduleName).formatted(Formatting.RED)).append(Text.literal("' не найден.")), false);
                                return 0;
                            });
                })
        );
    }
}