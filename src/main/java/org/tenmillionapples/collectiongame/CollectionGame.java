package org.tenmillionapples.collectiongame;

import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.tenmillionapples.collectiongame.command.*;
import org.tenmillionapples.collectiongame.command.executors.*;
import org.tenmillionapples.collectiongame.database.DatabaseManager;
import org.tenmillionapples.collectiongame.event.GUIOpenEvent;
import org.tenmillionapples.collectiongame.gui.CollectionGUI;
import org.tenmillionapples.collectiongame.listener.CollectionListener;
import org.tenmillionapples.collectiongame.listener.InventoryListener;
import org.tenmillionapples.collectiongame.plugins.PAPIHook;
import org.tenmillionapples.collectiongame.plugins.VaultHook;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

import static org.bukkit.Material.*;

public class CollectionGame extends JavaPlugin {
    public static final Set<Game> games = new HashSet<>();
    public static final Set<Material> illegalMats = new HashSet<>();
    public static final Set<CollectionGUI> guis = new HashSet<>();
    public static VaultHook vault;
    public static PAPIHook papi;
    public static DatabaseManager databaseManager = null;

    /**
     * @return games that the specified player is participating in
     */
    public static Set<Game> getGames(OfflinePlayer player) {
        return games.stream().filter(g -> g.getCollections().containsKey(player.getUniqueId())).collect(Collectors.toSet());
    }

    @Nullable
    public static Game getGameByName(String name) {
        return games.stream().filter(g -> g.getName().equalsIgnoreCase(name)).findAny().orElse(null);
    }

    /**
     * Finds an open collection GUI with the specified player as the viewer
     * @param player The player viewing the GUI
     * @return The collection gui if it is found, or null if not found
     */
    @Nullable
    public static CollectionGUI getGui(OfflinePlayer player) {
        return guis.stream().filter(gui -> gui.getViewer() == player).findAny().orElse(null);
    }

    @Override
    public void onEnable() {
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new CollectionListener(), this);
        pm.registerEvents(new InventoryListener(), this);
        registerCommands();

        saveDefaultConfig();
        Config.config = getConfig();

        for (Material mat : Material.values()) {
            String name = mat.name();
            if (mat.isLegacy() || mat.isAir() || name.startsWith("INFESTED_") || name.endsWith("_SPAWN_EGG")
            || name.endsWith("_WALL_SIGN") || name.endsWith("_WALL_HANGING_SIGN") || name.endsWith("_CAULDRON")
            || name.startsWith("POTTED_") || name.endsWith("_WALL_HEAD") || name.endsWith("_WALL_BANNER")
            || name.endsWith("CANDLE_CAKE")) {
                illegalMats.add(mat);
            }
        }
        Set<Material> mats = Arrays.stream(new Material[]{
                SUSPICIOUS_SAND, SUSPICIOUS_GRAVEL, BUDDING_AMETHYST, PETRIFIED_OAK_SLAB, SPAWNER, FARMLAND, REINFORCED_DEEPSLATE,
                END_PORTAL_FRAME, END_PORTAL, BARRIER, LIGHT, DIRT_PATH, COMMAND_BLOCK, REPEATING_COMMAND_BLOCK, CHAIN_COMMAND_BLOCK,
                COMMAND_BLOCK_MINECART, STRUCTURE_VOID, STRUCTURE_BLOCK, FILLED_MAP, PLAYER_HEAD, KNOWLEDGE_BOOK, DEBUG_STICK, WATER,
                LAVA, TALL_SEAGRASS, PISTON_HEAD, MOVING_PISTON, WALL_TORCH, FIRE, SOUL_FIRE, REDSTONE_WIRE, REDSTONE_WALL_TORCH,
                SOUL_WALL_TORCH, ATTACHED_MELON_STEM, ATTACHED_PUMPKIN_STEM, PUMPKIN_STEM, MELON_STEM, COCOA, TRIPWIRE, CARROT,
                POTATOES, SKELETON_WALL_SKULL, WITHER_SKELETON_WALL_SKULL, TORCHFLOWER_CROP, PITCHER_CROP, END_GATEWAY, BEETROOTS,
                FROSTED_ICE, KELP_PLANT, BAMBOO_SAPLING, BUBBLE_COLUMN, SWEET_BERRY_BUSH, WEEPING_VINES_PLANT, TWISTING_VINES_PLANT,
                POWDER_SNOW, CAVE_VINES, CAVE_VINES_PLANT, BIG_DRIPLEAF_STEM, FROGSPAWN, POTION, LINGERING_POTION, SPLASH_POTION,
                WRITTEN_BOOK, BUNDLE, JIGSAW
        }).collect(Collectors.toSet());
        illegalMats.addAll(mats);

        // Hook plugins
        if (getServer().getPluginManager().getPlugin("Vault") != null) {
            vault = new VaultHook(this);
            if (!vault.setupEconomy()) {
                vault = null;
            }
        }
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            papi = new PAPIHook();
            papi.register();
        }

        databaseManager = new DatabaseManager(this);
        databaseManager.loadData();

        new Metrics(this, 19771);
    }

    @Override
    public void onDisable() {
        for (CollectionGUI gui : guis) {
            gui.getViewer().closeInventory();
        }

        databaseManager.saveData();

        Config.config = null;
        databaseManager = null;
        vault = null;
    }

    private void registerCommands() {
        Set<GameCommand> executors = new HashSet<>(Arrays.asList(new AddToGame(), new RemoveFromGame(), new CollectItem(), new CreateGame(),
                new EndGame(), new UncollectItem(), new ViewCollected(), new ViewUncollected(), new Collected(), new Uncollected()));
        executors.forEach(gameCommand -> {
            try {
                PluginCommand command = getServer().getPluginCommand(gameCommand.getCommandName());
                command.setExecutor(gameCommand);
                command.setTabCompleter(gameCommand);
                command.setPermissionMessage(ChatColor.RED + "No permission. (/" + command.getName() + ")");
            } catch(Exception e) {
                getPluginLoader().disablePlugin(this);
                e.printStackTrace();
            }
        });
    }

    /**
     * Opens a new collection gui
     * @param viewer The player to open the collection GUI inventory
     * @param reference The player to get the collection items from
     * @param game The game the GUI is opened for
     * @param type The type of GUI that is opened
     */
    public static void openGUI(Player viewer, OfflinePlayer reference, Game game, @Nonnull GUIOpenEvent.GUIType type) {
        boolean selfCollection = viewer == reference;
        String title = game.getDisplayName() + ChatColor.RESET + ": ";
        List<ItemStack> items;

        if (type == GUIOpenEvent.GUIType.COLLECTED) {
            title += selfCollection ? "Collected" : (reference.getName() + "'s collected");
            items = game.getCollections().getOrDefault(reference.getUniqueId(),
                    new HashSet<>()).stream().sorted(game.getComparator()).collect(Collectors.toList());
        } else {
            title += selfCollection ? "Uncollected" : (reference.getName() + "'s uncollected");
            Set<ItemStack> collected = new HashSet<>(game.getCollections().getOrDefault(reference.getUniqueId(), new HashSet<>()));
            Set<ItemStack> required = new HashSet<>(game.getRequiredItems());
            required.removeIf(i -> collected.stream().anyMatch(j -> j.isSimilar(i))); // TODO: optimize (maybe keep a cache of the uncollected items?)
            items = required.stream().sorted(game.getComparator()).collect(Collectors.toList());
        }

        CollectionGUI gui = new CollectionGUI(viewer, title, items);
        GUIOpenEvent event = new GUIOpenEvent(game, gui, type);
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
            gui.openGUI();
            guis.add(gui);
        }
    }

    /*
    TODO: reload config command
    FIXME: clicking on crafting table result without picking up item still collects it
     */
}
