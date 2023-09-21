package org.tenmillionapples.collectiongame.database;

import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.tenmillionapples.collectiongame.CollectionGame;
import org.tenmillionapples.collectiongame.Game;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class DatabaseManager {
    private final CollectionGame game;

    public DatabaseManager(CollectionGame game) {
        this.game = game;
    }

    public void loadData() {
        File file = new File(game.getDataFolder(), "data.yml");
        YamlConfiguration data;
        try {
            file.getParentFile().mkdirs();
            file.createNewFile();

            data = new YamlConfiguration();
            data.load(file);
        } catch (Exception e) {
            game.getLogger().log(Level.SEVERE, "There was a problem loading the data. Disabling plugin.", e);
            game.getServer().getPluginManager().disablePlugin(game);
            throw new RuntimeException(e);
        }

        for(String gameKey : data.getKeys(false)) {
            Game game = new Game(gameKey, data.getString(gameKey + ".display"));
            CollectionGame.games.add(game);
            ConfigurationSection section = getSection(data, gameKey + ".collections");
            for(String uuidKey : section.getKeys(false)) {
                UUID uuid = UUID.fromString(uuidKey);
                List<ItemStack> items = (List<ItemStack>) section.getList(uuidKey);
                game.getCollections().put(uuid, new HashSet<>(items));
            }
        }
    }

    public void saveData() {
        YamlConfiguration data = new YamlConfiguration();

        for (Game game : CollectionGame.games) {
            ConfigurationSection mainSection = getSection(data, game.getName());
            mainSection.set("display", game.getDisplayName());
            ConfigurationSection collectionsSection = getSection(mainSection, "collections");
            for (OfflinePlayer player : game.getParticipants()) {
                List<ItemStack> items = new ArrayList<>(game.getCollections().get(player.getUniqueId()));
                collectionsSection.set(player.getUniqueId().toString(), items);
            }
        }

        try {
            data.save(new File(game.getDataFolder(), "data.yml"));
        } catch (IOException e) {
            game.getLogger().log(Level.SEVERE, "There was a problem saving the data.", e);
        }
    }

    private ConfigurationSection getSection(ConfigurationSection section, String key) {
        if (section.isSet(key)) {
            return section.getConfigurationSection(key);
        } else {
            return section.createSection(key);
        }
    }
}