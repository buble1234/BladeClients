package win.blade.common.utils.network;

import net.minecraft.client.gui.hud.BossBarHud;
import net.minecraft.client.gui.hud.ClientBossBar;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.text.Text;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

import static win.blade.common.utils.minecraft.MinecraftInstance.mc;

/**
 * Автор: NoCap
 * Дата создания: 17.06.2025
 */

public class ServerUtility {

    private static final Map<String, Object> cache = new ConcurrentHashMap<>();
    private static long lastUpdate = 0;
    private static final long CACHE_DURATION = 1000;

    private static final Pattern ANARCHY_PATTERN = Pattern.compile("режим:\\s*анархия-(.+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern GRIEF_PATTERN = Pattern.compile("режим:\\s*гриферский-(.+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern PVP_PATTERN = Pattern.compile("пвп|pvp", Pattern.CASE_INSENSITIVE);

    private static final Map<String, ServerInfo> KNOWN_SERVERS = new HashMap<>();

    static {
        KNOWN_SERVERS.put("funtime", new ServerInfo("FunTime", "Анархия", true));
        KNOWN_SERVERS.put("spookytime", new ServerInfo("SpookyTime", "Анархия", true));
        KNOWN_SERVERS.put("2b2t", new ServerInfo("2b2t", "Анархия", true));
        KNOWN_SERVERS.put("9b9t", new ServerInfo("9b9t", "Анархия", true));
        KNOWN_SERVERS.put("constantiam", new ServerInfo("Constantiam", "Анархия", true));
        KNOWN_SERVERS.put("reallyworld", new ServerInfo("ReallyWorld", "Выживание", false));
        KNOWN_SERVERS.put("holyworld", new ServerInfo("HolyWorld", "Выживание", false));
        KNOWN_SERVERS.put("aresmine", new ServerInfo("AresMine", "Выживание", false));
        KNOWN_SERVERS.put("mineblaze", new ServerInfo("MineBlaze", "Выживание", false));
        KNOWN_SERVERS.put("hitech", new ServerInfo("HiTech", "Выживание", false));
        KNOWN_SERVERS.put("cristalix", new ServerInfo("Cristalix", "Мини-игры", false));
        KNOWN_SERVERS.put("hypixel", new ServerInfo("Hypixel", "Мини-игры", false));
        KNOWN_SERVERS.put("mineplex", new ServerInfo("Mineplex", "Мини-игры", false));
        KNOWN_SERVERS.put("vimeworld", new ServerInfo("VimeWorld", "Мини-игры", false));
        KNOWN_SERVERS.put("mcpvp", new ServerInfo("MCPVP", "PvP", false));
        KNOWN_SERVERS.put("pvpwars", new ServerInfo("PvPWars", "PvP", false));
        KNOWN_SERVERS.put("kohi", new ServerInfo("Kohi", "PvP", false));
        KNOWN_SERVERS.put("badlion", new ServerInfo("Badlion", "PvP", false));
        KNOWN_SERVERS.put("cosmicpvp", new ServerInfo("CosmicPvP", "Factions", false));
        KNOWN_SERVERS.put("archonhq", new ServerInfo("ArchonHQ", "Factions", false));
        KNOWN_SERVERS.put("saicopvp", new ServerInfo("SaicoPvP", "Factions", false));
        KNOWN_SERVERS.put("manacube", new ServerInfo("ManaCube", "Skyblock", false));
        KNOWN_SERVERS.put("cubecraft", new ServerInfo("CubeCraft", "Skyblock", false));
        KNOWN_SERVERS.put("plotme", new ServerInfo("PlotMe", "Творчество", false));
        KNOWN_SERVERS.put("builduhc", new ServerInfo("BuildUHC", "Творчество", false));
        KNOWN_SERVERS.put("technic", new ServerInfo("Technic", "Моды", false));
        KNOWN_SERVERS.put("ftb", new ServerInfo("Feed The Beast", "Моды", false));
        KNOWN_SERVERS.put("pixelmon", new ServerInfo("Pixelmon", "Моды", false));
        KNOWN_SERVERS.put("minecraftonly", new ServerInfo("MinecraftOnly", "Выживание", false));
        KNOWN_SERVERS.put("mcskill", new ServerInfo("MCSkill", "Мини-игры", false));
        KNOWN_SERVERS.put("mc-hunger", new ServerInfo("MC-Hunger", "Голодные игры", false));
        KNOWN_SERVERS.put("darkcity", new ServerInfo("DarkCity", "РП", false));
        KNOWN_SERVERS.put("litemc", new ServerInfo("LiteMC", "Выживание", false));
        KNOWN_SERVERS.put("uacraft", new ServerInfo("UACraft", "Выживание", false));
        KNOWN_SERVERS.put("mcukraine", new ServerInfo("MCUkraine", "Выживание", false));
        KNOWN_SERVERS.put("lordofthecraft", new ServerInfo("Lord of the Craft", "РП", false));
        KNOWN_SERVERS.put("massivecraft", new ServerInfo("MassiveCraft", "РП", false));
        KNOWN_SERVERS.put("wynncraft", new ServerInfo("Wynncraft", "MMORPG", false));
        KNOWN_SERVERS.put("hardcore", new ServerInfo("Hardcore", "Хардкор", false));
        KNOWN_SERVERS.put("uhc", new ServerInfo("UHC", "Хардкор", false));
        KNOWN_SERVERS.put("localhost", new ServerInfo("Локальный сервер", "Разработка", false));
        KNOWN_SERVERS.put("127.0.0.1", new ServerInfo("Локальный сервер", "Разработка", false));
        KNOWN_SERVERS.put("192.168", new ServerInfo("Локальная сеть", "LAN", false));
        KNOWN_SERVERS.put("10.0", new ServerInfo("Локальная сеть", "LAN", false));
    }

    public static ServerInfo getCurrentServer() {
        return getCachedValue("server_info", () -> {
            if (mc.getNetworkHandler() == null) return null;

            String address = mc.getNetworkHandler().getConnection().getAddress().toString().toLowerCase();

            for (Map.Entry<String, ServerInfo> entry : KNOWN_SERVERS.entrySet()) {
                if (address.contains(entry.getKey())) {
                    return entry.getValue();
                }
            }

            if (address.matches(".*\\d+\\.\\d+\\.\\d+\\.\\d+.*")) {
                if (address.contains("192.168") || address.contains("10.0") || address.contains("172.16")) {
                    return new ServerInfo("Локальная сеть", "LAN", false);
                } else if (address.contains("127.0.0.1") || address.contains("localhost")) {
                    return new ServerInfo("Локальный сервер", "Разработка", false);
                } else {
                    return new ServerInfo("Неизвестный сервер", "Публичный", false);
                }
            }

            if (address.contains(".ru")) {
                return new ServerInfo("Российский сервер", "Unknown", false);
            } else if (address.contains(".ua")) {
                return new ServerInfo("Украинский сервер", "Unknown", false);
            } else if (address.contains(".com") || address.contains(".net") || address.contains(".org")) {
                return new ServerInfo("Международный сервер", "Unknown", false);
            }

            return new ServerInfo("Неизвестный сервер", "Unknown", false);
        });
    }

    public static boolean isPvpActive() {
        return getCachedValue("pvp_active", () -> {
            if (checkPvpInBossBar()) return true;

            if (checkPvpInTab()) return true;

            if (checkPvpInScoreboard()) return true;

            return false;
        });
    }

    public static String getAnarchyMode() {
        return getCachedValue("anarchy_mode", () -> {
            if (!isAnarchyServer()) return "none";

            String mode = extractModeFromTab(ANARCHY_PATTERN);
            if (mode != null) return mode;

            mode = extractModeFromScoreboard();
            if (mode != null) return mode;

            return "unknown";
        });
    }

    public static String getGriefMode() {
        return getCachedValue("grief_mode", () -> {
            if (!isAnarchyServer()) return "none";

            String mode = extractModeFromTab(GRIEF_PATTERN);
            return mode != null ? mode : "none";
        });
    }

    public static boolean isOnSpawn() {
        return getCachedValue("on_spawn", () -> {
            if (mc.world == null) return false;

            String worldId = mc.world.getRegistryKey().getValue().toString();
            return !worldId.equals("minecraft:overworld");
        });
    }

    public static GameState getCurrentGameState() {
        ServerInfo server = getCurrentServer();
        if (server == null) {
            return new GameState("Одиночная игра", "Локальный мир", false, false);
        }

        String location = isOnSpawn() ? "Спавн" : "Игровой мир";
        String mode = server.isAnarchy() ? getAnarchyMode() : "Выживание";
        boolean pvp = isPvpActive();

        return new GameState(server.getName(), location, pvp, server.isAnarchy());
    }

    private static boolean isAnarchyServer() {
        ServerInfo server = getCurrentServer();
        return server != null && server.isAnarchy();
    }

    private static boolean checkPvpInBossBar() {
        try {
            BossBarHud hud = mc.inGameHud.getBossBarHud();
            Field field = BossBarHud.class.getDeclaredField("bossBars");
            field.setAccessible(true);

            Map<UUID, ClientBossBar> bossBars = (Map<UUID, ClientBossBar>) field.get(hud);

            return bossBars.values().stream()
                    .anyMatch(bar -> PVP_PATTERN.matcher(bar.getName().getString()).find());

        } catch (Exception e) {
            return false;
        }
    }

    private static boolean checkPvpInTab() {
        try {
            String tabText = getTabHeaderText();
            return tabText != null && PVP_PATTERN.matcher(tabText).find();
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean checkPvpInScoreboard() {
        try {
            if (mc.player == null || mc.player.getScoreboard() == null) return false;

            return mc.player.getScoreboard().getObjectives().stream()
                    .anyMatch(obj -> PVP_PATTERN.matcher(obj.getDisplayName().getString()).find());

        } catch (Exception e) {
            return false;
        }
    }

    private static String extractModeFromTab(Pattern pattern) {
        try {
            String tabText = getTabHeaderText();
            if (tabText == null) return null;

            return Arrays.stream(tabText.split("\n"))
                    .map(line -> line.replaceAll("§.", "").trim())
                    .map(pattern::matcher)
                    .filter(matcher -> matcher.find())
                    .map(matcher -> matcher.group(1).trim())
                    .findFirst()
                    .orElse(null);

        } catch (Exception e) {
            return null;
        }
    }

    private static String extractModeFromScoreboard() {
        try {
            if (mc.player == null || mc.player.getScoreboard() == null) return null;

            Object[] objectives = mc.player.getScoreboard().getObjectives().toArray();
            if (objectives.length == 0) return null;

            ScoreboardObjective obj = (ScoreboardObjective) objectives[0];
            String displayName = obj.getDisplayName().getString();

            if (displayName.length() > 10) {
                return displayName.substring(10);
            }

        } catch (Exception e) {
        }

        return null;
    }

    private static String getTabHeaderText() throws Exception {
        PlayerListHud playerListHud = mc.inGameHud.getPlayerListHud();
        Field headerField = PlayerListHud.class.getDeclaredField("header");
        headerField.setAccessible(true);

        Text headerText = (Text) headerField.get(playerListHud);
        return headerText != null ? headerText.getString() : null;
    }

    @SuppressWarnings("unchecked")
    private static <T> T getCachedValue(String key, java.util.function.Supplier<T> supplier) {
        long currentTime = System.currentTimeMillis();

        if (currentTime - lastUpdate > CACHE_DURATION) {
            cache.clear();
            lastUpdate = currentTime;
        }

        return (T) cache.computeIfAbsent(key, k -> supplier.get());
    }

    public static class ServerInfo {
        private final String name;
        private final String type;
        private final boolean anarchy;

        public ServerInfo(String name, String type, boolean anarchy) {
            this.name = name;
            this.type = type;
            this.anarchy = anarchy;
        }

        public String getName() { return name; }
        public String getType() { return type; }
        public boolean isAnarchy() { return anarchy; }

        @Override
        public String toString() {
            return String.format("ServerInfo{name='%s', type='%s', anarchy=%s}", name, type, anarchy);
        }
    }

    public static class GameState {
        private final String serverName;
        private final String location;
        private final boolean pvpActive;
        private final boolean anarchyServer;

        public GameState(String serverName, String location, boolean pvpActive, boolean anarchyServer) {
            this.serverName = serverName;
            this.location = location;
            this.pvpActive = pvpActive;
            this.anarchyServer = anarchyServer;
        }

        public String getServerName() { return serverName; }
        public String getLocation() { return location; }
        public boolean isPvpActive() { return pvpActive; }
        public boolean isAnarchyServer() { return anarchyServer; }

        public String getFormattedStatus() {
            StringBuilder status = new StringBuilder();
            status.append(serverName);

            if (anarchyServer) {
                status.append(" | ").append(location);
            }

            if (pvpActive) {
                status.append(" | PvP активен");
            }

            return status.toString();
        }

        @Override
        public String toString() {
            return String.format("GameState{server='%s', location='%s', pvp=%s, anarchy=%s}",
                    serverName, location, pvpActive, anarchyServer);
        }
    }

    @Deprecated
    public static boolean isOnFuntime() {
        ServerInfo server = getCurrentServer();
        return server != null && server.getName().equals("FunTime");
    }

    @Deprecated
    public static boolean isOnSpookyTime() {
        ServerInfo server = getCurrentServer();
        return server != null && server.getName().equals("SpookyTime");
    }

    @Deprecated
    public static boolean isOnReallyWorld() {
        ServerInfo server = getCurrentServer();
        return server != null && server.getName().equals("ReallyWorld");
    }

    @Deprecated
    public static boolean isOnHolyWorld() {
        ServerInfo server = getCurrentServer();
        return server != null && server.getName().equals("HolyWorld");
    }

    @Deprecated
    public static boolean isOnAresMine() {
        ServerInfo server = getCurrentServer();
        return server != null && server.getName().equals("AresMine");
    }

    @Deprecated
    public static boolean isOnAnarchy() {
        return !getAnarchyMode().equals("none");
    }

    @Deprecated
    public static boolean isOnGrief() {
        return !getGriefMode().equals("none");
    }

    public static boolean isOnHypixel() {
        ServerInfo server = getCurrentServer();
        return server != null && server.getName().equals("Hypixel");
    }

    public static boolean isOnCristalix() {
        ServerInfo server = getCurrentServer();
        return server != null && server.getName().equals("Cristalix");
    }

    public static boolean isOnVimeWorld() {
        ServerInfo server = getCurrentServer();
        return server != null && server.getName().equals("VimeWorld");
    }

    public static boolean isOn2b2t() {
        ServerInfo server = getCurrentServer();
        return server != null && server.getName().equals("2b2t");
    }

    public static boolean isOnMinigameServer() {
        ServerInfo server = getCurrentServer();
        return server != null && server.getType().equals("Мини-игры");
    }

    public static boolean isOnPvPServer() {
        ServerInfo server = getCurrentServer();
        return server != null && (server.getType().equals("PvP") || isPvpActive());
    }

    public static boolean isOnFactionsServer() {
        ServerInfo server = getCurrentServer();
        return server != null && server.getType().equals("Factions");
    }

    public static boolean isOnSkyblockServer() {
        ServerInfo server = getCurrentServer();
        return server != null && server.getType().equals("Skyblock");
    }

    public static boolean isOnRolePlayServer() {
        ServerInfo server = getCurrentServer();
        return server != null && (server.getType().equals("РП") || server.getType().equals("MMORPG"));
    }

    public static boolean isOnModdedServer() {
        ServerInfo server = getCurrentServer();
        return server != null && server.getType().equals("Моды");
    }

    public static boolean isOnLocalServer() {
        ServerInfo server = getCurrentServer();
        return server != null && (server.getType().equals("LAN") || server.getType().equals("Разработка"));
    }

    public static boolean isOnRussianServer() {
        ServerInfo server = getCurrentServer();
        if (server == null) return false;

        String name = server.getName().toLowerCase();
        return name.contains("russian") || name.contains("российский") ||
                Arrays.asList("FunTime", "SpookyTime", "ReallyWorld", "HolyWorld",
                        "AresMine", "MineBlaze", "HiTech", "MinecraftOnly",
                        "MCSkill", "MC-Hunger", "DarkCity", "LiteMC").contains(server.getName());
    }

    public static Set<String> getAllKnownServers() {
        return KNOWN_SERVERS.values().stream()
                .map(ServerInfo::getName)
                .collect(java.util.stream.Collectors.toSet());
    }

    public static List<ServerInfo> getServersByType(String type) {
        return KNOWN_SERVERS.values().stream()
                .filter(server -> server.getType().equals(type))
                .collect(java.util.stream.Collectors.toList());
    }

    public static void addCustomServer(String address, ServerInfo serverInfo) {
        KNOWN_SERVERS.put(address.toLowerCase(), serverInfo);
    }

    public static String isName(String notSolved) {
        AtomicReference<String> mb = new AtomicReference<>("FATAL ERROR");
        Objects.requireNonNull(mc.getNetworkHandler()).getListedPlayerListEntries().forEach(player -> {
            if (notSolved.contains(player.getProfile().getName())) {
                mb.set(player.getProfile().getName());
            }
        });

        return mb.get();
    }
}