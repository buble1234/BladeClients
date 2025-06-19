package win.blade.core.module.storage.misc;

import com.jagrosh.discordipc.IPCClient;
import com.jagrosh.discordipc.IPCListener;
import com.jagrosh.discordipc.entities.RichPresence;
import com.jagrosh.discordipc.entities.User;
import com.jagrosh.discordipc.entities.ActivityType;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.jagrosh.discordipc.exceptions.NoDiscordClientException;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;
import win.blade.common.utils.resource.InformationUtility;
import win.blade.core.module.api.Category;
import win.blade.core.module.api.Module;
import win.blade.core.module.api.ModuleInfo;

/**
 * Автор: NoCap
 * Дата создания: 18.06.2025
 */

@ModuleInfo(
        name = "DiscordRPC",
        category = Category.MISC,
        desc = "Показывает статус игры в Discord"
)
public class DiscordRPCModule extends Module {

    private IPCClient client;
    private final long timeStamp = System.currentTimeMillis();

    private static final long APPLICATION_ID = 1384850624776245399L;

    public DiscordRPCModule() {
        setEnabled(true);
    }

    @Override
    public void onEnable() {
        try {
            client = new IPCClient(APPLICATION_ID);
            client.setListener(new IPCListener() {

                @Override
                public void onReady(IPCClient client) {
                    updatePresence();
                    System.out.println("Discord RPC is ready.");
                }

                @Override
                public void onClose(IPCClient client, com.google.gson.JsonObject json) {
                    System.out.println("Discord RPC is closed.");
                }

                @Override
                public void onDisconnect(IPCClient client, Throwable t) {
                    System.out.println("Discord RPC is disconnected.");
                    if (t != null) {
                        t.printStackTrace();
                    }
                }

                @Override
                public void onPacketSent(IPCClient client, com.jagrosh.discordipc.entities.Packet packet) {
                }

                @Override
                public void onPacketReceived(IPCClient client, com.jagrosh.discordipc.entities.Packet packet) {
                }

                @Override
                public void onActivityJoin(IPCClient client, String secret) {
                    System.out.println("Discord RPC activity join: " + secret);
                }

                @Override
                public void onActivitySpectate(IPCClient client, String secret) {
                    System.out.println("Discord RPC activity spectate: " + secret);
                }

                @Override
                public void onActivityJoinRequest(IPCClient client, String secret, User user) {
                    System.out.println("Discord RPC join request from: " + user.getName());
                }
            });

            client.connect();
            System.out.println("Discord RPC enabled.");

        } catch (NoDiscordClientException e) {
            System.err.println("Discord client not found: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Failed to enable Discord RPC: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        if (client == null) {
            return;
        }

        try {
            client.close();
            client = null;
            System.out.println("Discord RPC closed.");
        } catch (Exception e) {
            System.err.println("Failed to close Discord RPC: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updatePresence() {
        if (client == null) {
            return;
        }

        MinecraftClient mc = MinecraftClient.getInstance();
        String details = InformationUtility.CLIENT + " v" + InformationUtility.VERSION;
        String state = "Enhancing Minecraft gameplay";

        if (mc.world != null) {
            if (mc.getCurrentServerEntry() != null) {
                state = "Playing on " + mc.getCurrentServerEntry().address;
            } else {
                state = "Playing Singleplayer";
            }
        } else {
            state = "In Main Menu";
        }

        JsonArray buttons = new JsonArray();

        JsonObject discordButton = new JsonObject();
        discordButton.addProperty("label", "Discord");
        discordButton.addProperty("url", InformationUtility.DISCORD_INVITE);
        buttons.add(discordButton);

        JsonObject telegramButton = new JsonObject();
        telegramButton.addProperty("label", "Telegram");
        telegramButton.addProperty("url", InformationUtility.TELEGRAM_LINK);
        buttons.add(telegramButton);

        RichPresence presence = new RichPresence.Builder()
                .setState(state)
                .setDetails(details)
                .setStartTimestamp(timeStamp)
                .setActivityType(ActivityType.Playing)
                .setLargeImage("https://s7.gifyu.com/images/SXMNX.gif", InformationUtility.CLIENT + " v" + InformationUtility.VERSION + " by " + InformationUtility.AUTHOR)
                .setSmallImage("minecraft", "Minecraft")
                .setButtons(buttons)
                .build();

        try {
            client.sendRichPresence(presence);
        } catch (Exception e) {
            System.err.println("Failed to update Discord presence: " + e.getMessage());
        }
    }

    public void refresh() {
        if (isEnabled()) {
            updatePresence();
        }
    }
}