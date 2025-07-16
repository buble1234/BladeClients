package win.blade.core.commands.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.command.CommandSource;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import win.blade.common.utils.minecraft.ChatUtility;
import win.blade.core.Manager;
import win.blade.core.commands.Command;
import win.blade.core.commands.CommandInfo;
import win.blade.core.module.storage.render.BlockESPModule;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

/**
 * Автор: NoCap
 * Дата создания: 16.07.2025
 */
@CommandInfo(name = "blockesp", alias = {"блокесп"})
public class BlockESPCommand extends Command {

    private static BlockESPModule getModule() {
        return Manager.getModuleManagement().get(BlockESPModule.class);
    }

    private static final SuggestionProvider<CommandSource> ALL_BLOCKS_PROVIDER = (context, builder) ->
            CommandSource.suggestMatching(
                    Registries.BLOCK.getIds().stream().map(Identifier::toString),
                    builder
            );

    private static final SuggestionProvider<CommandSource> ADDED_BLOCKS_PROVIDER = (context, builder) -> {
        BlockESPModule module = getModule();
        if (module == null) return builder.buildFuture();
        return CommandSource.suggestMatching(
                module.blockList.stream().map(block -> Registries.BLOCK.getId(block).toString()),
                builder
        );
    };

    @Override
    public void executeBuild(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(LiteralArgumentBuilder.<CommandSource>literal("add")
                        .then(RequiredArgumentBuilder.<CommandSource, String>argument("block_id", StringArgumentType.greedyString())
                                .suggests(ALL_BLOCKS_PROVIDER)
                                .executes(context -> {
                                    String blockName = StringArgumentType.getString(context, "block_id");
                                    return modifyBlockList(blockName, true);
                                })))

                .then(LiteralArgumentBuilder.<CommandSource>literal("remove")
                        .then(RequiredArgumentBuilder.<CommandSource, String>argument("block_id", StringArgumentType.greedyString())
                                .suggests(ADDED_BLOCKS_PROVIDER)
                                .executes(context -> {
                                    String blockName = StringArgumentType.getString(context, "block_id");
                                    return modifyBlockList(blockName, false);
                                })))

                .then(LiteralArgumentBuilder.<CommandSource>literal("list")
                        .executes(context -> {
                            BlockESPModule module = getModule();
                            if (module == null) return SINGLE_SUCCESS;

                            if (module.blockList.isEmpty()) {
                                ChatUtility.add(Text.literal("Список блоков для ESP пуст.").formatted(Formatting.YELLOW));
                            } else {
                                ChatUtility.add(Text.literal("Список отслеживаемых блоков:").formatted(Formatting.AQUA));
                                module.blockList.forEach(block -> ChatUtility.add(Text.literal(" - ").append(block.getName()).formatted(Formatting.GRAY)));
                            }
                            return SINGLE_SUCCESS;
                        }))

                .then(LiteralArgumentBuilder.<CommandSource>literal("clear")
                        .executes(context -> {
                            BlockESPModule module = getModule();
                            if (module != null && !module.blockList.isEmpty()) {
                                module.blockList.clear();
                                module.updateBlocks();
                                ChatUtility.add(Text.literal("Список блоков очищен.").formatted(Formatting.GREEN));
                            } else {
                                ChatUtility.add(Text.literal("Список уже пуст.").formatted(Formatting.YELLOW));
                            }
                            return SINGLE_SUCCESS;
                        }));
    }

    private int modifyBlockList(String blockName, boolean isAdding) {
        BlockESPModule module = getModule();
        if (module == null) {
            ChatUtility.add(Text.literal("Модуль BlockESP не загружен.").formatted(Formatting.RED));
            return SINGLE_SUCCESS;
        }

        Identifier blockId = Identifier.tryParse(blockName);
        if (blockId == null || !Registries.BLOCK.containsId(blockId)) {
            ChatUtility.add(Text.literal("Блок '" + blockName + "' не найден.").formatted(Formatting.RED));
            return SINGLE_SUCCESS;
        }

        Block block = Registries.BLOCK.get(blockId);
        if (block == Blocks.AIR) {
            ChatUtility.add(Text.literal("Нельзя добавить воздух.").formatted(Formatting.RED));
            return SINGLE_SUCCESS;
        }

        if (isAdding) {
            if (module.blockList.contains(block)) {
                ChatUtility.add(Text.literal("Блок '").append(block.getName()).append("' уже в списке.").formatted(Formatting.YELLOW));
            } else {
                module.blockList.add(block);
                ChatUtility.add(Text.literal("Блок '").append(block.getName()).append("' добавлен.").formatted(Formatting.GREEN));
            }
        } else {
            if (module.blockList.remove(block)) {
                ChatUtility.add(Text.literal("Блок '").append(block.getName()).append("' удален.").formatted(Formatting.GREEN));
            } else {
                ChatUtility.add(Text.literal("Блок '").append(block.getName()).append("' не найден в списке.").formatted(Formatting.RED));
            }
        }

        if (module.isEnabled()) {
            module.updateBlocks();
        }

        return SINGLE_SUCCESS;
    }
}