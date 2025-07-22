package win.blade.core.commands.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import win.blade.core.Manager;
import win.blade.core.commands.Command;
import win.blade.core.commands.CommandInfo;
import win.blade.core.module.api.ModuleManager;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

/**
 * Автор: NoCap
 * Дата создания: 22.07.2025
 */
@CommandInfo(name = "toggle", description = "Включает или выключает модуль", alias = {"тогл"})
public class ToggleCommand extends Command {

    @Override
    public void executeBuild(@NotNull LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(RequiredArgumentBuilder.<CommandSource, String>argument("module", StringArgumentType.word())
                .suggests((context, suggestionsBuilder) -> {
                    Manager.getModuleManagement().all().forEach(module -> suggestionsBuilder.suggest(module.name()));
                    return suggestionsBuilder.buildFuture();
                })
                .executes(context -> {
                    String moduleName = StringArgumentType.getString(context, "module");
                    ModuleManager moduleManager = Manager.getModuleManagement();

                    return moduleManager.find(moduleName)
                            .map(module -> {
                                module.toggle();
                                String status = module.isEnabled() ? "включен" : "выключен";
                                Text message = Text.literal("Модуль ").append(Text.literal(module.name()).formatted(Formatting.AQUA)).append(Text.literal(" был " + status + ".").formatted(Formatting.WHITE));
                                mc.player.sendMessage(message, false);
                                return SINGLE_SUCCESS;
                            })
                            .orElseGet(() -> {
                                mc.player.sendMessage(Text.literal("Модуль '").append(Text.literal(moduleName).formatted(Formatting.RED)).append(Text.literal("' не найден.")), false);
                                return 0;
                            });
                }));
    }
}