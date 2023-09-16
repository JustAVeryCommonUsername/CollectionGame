package org.tenmillionapples.collectiongame;

import org.apache.commons.lang.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import javax.xml.crypto.Data;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class Config {
    static FileConfiguration config;

    private static Database database = null;

    public Config(){};

    public static String getCollectMessage(OfflinePlayer player, ItemStack mat, double prize, int num, Game game) {
        String message = ChatColor.translateAlternateColorCodes('&',
                Objects.requireNonNull(config.getString("collect-message")));
        message = replacePlaceholders(message, player, mat, prize, num, game);
        return message;
    }

    public static String getWinMessage(OfflinePlayer player, double prize, int num, Game game) {
        String message = ChatColor.translateAlternateColorCodes('&',
                Objects.requireNonNull(config.getString("win-message")));
        // item isn't applicable and this is more fun than null values
        message = replacePlaceholders(message, player, new ItemStack(Material.APPLE), prize,num, game);
        return message;
    }

    public static String getAddPlayerMessage(Player player, ItemStack item, double prize, int num, Game game) {
        String message = ChatColor.translateAlternateColorCodes('&',
                Objects.requireNonNull(config.getString("add-player-message")));
        message = replacePlaceholders(message, player, item, prize, num, game);
        return message;
    }

    private static String replacePlaceholders(String message, OfflinePlayer player, ItemStack item, double prize, int num, Game game) {
        String displayName = player.isOnline() ? player.getPlayer().getDisplayName() : player.getName();
        message = message.replace("{PLAYERNAME}", player.getName());
        message = message.replace("{DISPLAYNAME}", displayName);
        message = message.replace("{ITEM}", WordUtils.capitalizeFully(Util.getItemName(item).replace('_', ' ')));
        message = message.replace("{GAME}", game.getDisplayName());
        message = message.replace("{PRIZE}", getPrize(game.getName()) + "");
        return message.replace("{NUMBER}", "" + num);
    }

    /**
     * @param game The name of the game, or null to get default whitelist
     * @return The whitelist material set
     */
    public static Set<Material> getWhitelist(@Nullable String game) {
        List<String> list = config.getStringList("whitelisted-items." + (game == null ? "default" : game));
        return getMatsFromNameList(list);
    }

    /**
     * @param game The name of the game, or null to get default blacklist
     * @return The blacklist material set
     */
    public static Set<Material> getBlacklist(@Nullable String game) {
        List<String> list = config.getStringList("blacklisted-items." + (game == null ? "default" : game));
        return getMatsFromNameList(list);
    }

    public static double getPrize(@Nullable String game) {
        return config.getDouble("prize." + (game == null ? "default" : game));
    }

    public static boolean isRecurrent(@Nullable String game) {
        return config.getBoolean("recurrent." + (game == null ? "default" : game));
    }

    public static Sound getCollectSound() {
        return Sound.valueOf(config.getString("sound.collect").toUpperCase());
        // TODO add sound in config
    }

    public static Sound getWinSound() {
        return Sound.valueOf(config.getString("sound.win").toUpperCase());
        // TODO add sound in config
    }

    public static Database getDatabase() {
        if (database == null) {
            ConfigurationSection section = config.getConfigurationSection("database");
            database = new Database(section.getString("host"), section.getInt("port"), section.getString("database"),
                    section.getString("user"), section.getString("password"));
        }
        return database;
    }

    public static int getGuiRows() {
        return config.getInt("gui-rows");
    }

    private static Set<Material> getMatsFromNameList(List<String> list) {
        Set<Material> mats = new HashSet<>();
        list.forEach(s -> {
            Material mat = Material.getMaterial(s.toUpperCase());
            if (mat != null)
                mats.add(mat);
        });
        return mats;
    }

    public static final class Database {
        public final String host;
        public final int port;
        public final String databaseName;
        public final String user;
        public final String password;

        private Database(String host, int port, String databaseName, String user, String password) {
            this.host = host;
            this.port = port;
            this.databaseName = databaseName;
            this.user = user;
            this.password = password;
        }
    }
}
