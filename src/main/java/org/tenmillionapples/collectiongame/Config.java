package org.tenmillionapples.collectiongame;

import org.apache.commons.lang.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class Config {
    static FileConfiguration config;

    public Config(){}

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
        message = message.replace("{PRIZE}", prize + "");
        return message.replace("{NUMBER}", "" + num);
    }

    /**
     * @param game The name of the game, or null to get default whitelist
     * @return The whitelist material set
     */
    public static Set<Material> getWhitelist(@Nullable String game) {
        return getMatsFromNameList(getStringListOrDefault("whitelisted-items", game));
    }

    /**
     * @param game The name of the game, or null to get default blacklist
     * @return The blacklist material set
     */
    public static Set<Material> getBlacklist(@Nullable String game) {
        return getMatsFromNameList(getStringListOrDefault("blacklisted-items", game));
    }

    public static double getPrize(String game) {
        String path = "prize";
        if (!config.isSet(String.format("%s.%s", path, game)))
            return config.getDouble(String.format("%s.default", path));
        else
            return config.getDouble(String.format("%s.%s", path, game));
    }

    private static List<String> getStringListOrDefault(String path, String game) {
        if (!config.isSet(String.format("%s.%s", path, game)))
            return config.getStringList(String.format("%s.default", path));
        else
            return config.getStringList(String.format("%s.%s", path, game));
    }

    public static Sound getCollectSound() {
        return Sound.valueOf(config.getString("sound.collect").toUpperCase());
    }

    public static Sound getWinSound() {
        return Sound.valueOf(config.getString("sound.win").toUpperCase());
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
}
