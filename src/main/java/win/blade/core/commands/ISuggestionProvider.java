package win.blade.core.commands;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.command.CommandSource;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static win.blade.common.utils.minecraft.MinecraftInstance.mc;

public class ISuggestionProvider {

    public static final SuggestionProvider<CommandSource> ONLINE_PLAYERS = (context, builder) -> suggest(getOnlinePlayerNames(), builder);

    public static CompletableFuture<Suggestions> suggest(Collection<String> suggestions, SuggestionsBuilder builder) {
        String remaining = builder.getRemaining().toLowerCase();

        suggestions.forEach(suggestion -> {
            if (suggestion.toLowerCase().startsWith(remaining)) {
                builder.suggest(suggestion);
            }
        });

        return builder.buildFuture();
    }

    public static CompletableFuture<Suggestions> suggest(String[] suggestions, SuggestionsBuilder builder) {
        String remaining = builder.getRemaining().toLowerCase();

        for (String suggestion : suggestions) {
            if (suggestion.toLowerCase().startsWith(remaining)) {
                builder.suggest(suggestion);
            }
        }

        return builder.buildFuture();
    }

    public static Collection<String> getOnlinePlayerNames() {

        if (mc == null || mc.getNetworkHandler() == null) {
            return Collections.emptyList();
        }

        return mc.getNetworkHandler()
                .getPlayerList()
                .stream()
                .map(PlayerListEntry::getProfile)
                .map(profile -> profile.getName())
                .collect(Collectors.toList());
    }
}