package org.tenmillionapples.collectiongame;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.tenmillionapples.collectiongame.event.CollectItemEvent;
import org.tenmillionapples.collectiongame.event.EndGameEvent;
import org.tenmillionapples.collectiongame.event.GameWinEvent;

import java.util.*;
import java.util.stream.Collectors;

public class Game {
    private final Map<UUID, Set<ItemStack>> collections = new HashMap<>();

    private TreeSet<ItemStack> required;

    private final String name;
    private String display;
    private final Mode mode;
    private Comparator<ItemStack> comparator;

    public Game(String name, String display) {
        this.name = name;
        this.display = ChatColor.translateAlternateColorCodes('&', display);
        this.mode = Config.getWhitelist(name).isEmpty() ? Mode.BLACKLIST : Mode.WHITELIST;

        comparator = Comparator.comparing(m -> m.getType().ordinal());
        required = new TreeSet<>(comparator);
    }

    public void addToGame(OfflinePlayer player) {
        collections.put(player.getUniqueId(), new HashSet<>());
    }

    /**
     * @return true if the player was already participating, and false if the player wasn't
     */
    public boolean removeFromGame(OfflinePlayer player) {
        return collections.remove(player.getUniqueId()) != null;
    }

    /**
     * @param player the plauer
     * @param item Material they collected
     * @return If the item was already collected or not
     */
    public boolean collectItem(OfflinePlayer player, ItemStack item) {
        UUID uuid = player.getUniqueId();
        item = item.clone();
        item.setAmount(1);

        Optional<ItemStack> optional = getRequiredEquivalent(item);
        if (collectionContainsItem(player, item) || !optional.isPresent())
            return false;

        CollectItemEvent event = new CollectItemEvent(player, optional.get(), this);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled())
            return false;

        item = event.getItem();
        if (!collections.containsKey(uuid))
            collections.put(uuid, new HashSet<>());
        collections.get(uuid).add(item);

        if (player.isOnline()) {
            Player online = player.getPlayer();
            online.playSound(online.getLocation(), Config.getCollectSound(), 1f, 1f);
        }

        String message = Config.getCollectMessage(player.getPlayer(),
                item, Config.getPrize(name), collections.get(uuid).size(), this);
        getParticipants().forEach(p -> {
            if (p.isOnline() && !message.isEmpty())
                p.getPlayer().sendMessage(message);
        });

        updateGame();
        return true;
    }

    /**
     * Gets the set of required materials for this game
     * @return a mutable tree set of the required items
     */
    public TreeSet<ItemStack> getRequiredItems() {
        if (required.isEmpty()) {
            if (mode == Mode.WHITELIST) {
                required.addAll(Config.getWhitelist(name).stream().map(ItemStack::new).collect(Collectors.toSet()));
            } else {
                Set<Material> validMats = Util.allValidMats();
                validMats.removeAll(Config.getBlacklist(name));
                required.addAll(validMats.stream().map(ItemStack::new).collect(Collectors.toSet()));
            }
        }

        return required;
    }

    /**
     * Updates the game when something changes, such as new items were added or removed from the required items list<br>
     * This ensures that game wins are always up-to-date
     */
    public void updateGame() {
        Set<OfflinePlayer> winners = new HashSet<>();
        getParticipants().forEach(p -> {
            if (collections.get(p.getUniqueId()).size() >= getRequiredItems().size()) {
                winners.add(p);
            }
            // This way calculates the winners exactly, but is much slower
            // The above code fails if items were removed from required and more items were added but the count stayed the same
            //    This will most likely not happen, and it is worth saving performance over
            /*if (required.stream().allMatch(i -> collectionContainsItem(p, i))) {
                winners.add(p);
            }*/
        });
        if (winners.isEmpty())
            return;

        if (Config.isRecurrent(name)) {
            winners.forEach(this::onWin);
        } else {
            endGame(EndGameEvent.EndCause.GOAL);
        }
    }

    /**
     * @param uuid UUId of the player
     * @param mat Material to uncollect
     * @return If the material was already collected or not
     */
    public boolean uncollectItem(UUID uuid, ItemStack mat) {
        try {
            return collections.get(uuid).remove(mat);
        } finally {
            updateGame();
        }
    }

    public Map<UUID, Set<ItemStack>> getCollections() {
        return collections;
    }

    /**
     * Gets the players participating in this game
     * @return A set of offline players
     */
    public Set<OfflinePlayer> getParticipants() {
        return collections.keySet().stream().map(Bukkit::getOfflinePlayer).collect(Collectors.toSet());
    }

    public void endGame(EndGameEvent.EndCause cause) {
        OfflinePlayer winner = computeWinner();

        EndGameEvent endGameEvent = new EndGameEvent(winner, cause, this);
        Bukkit.getPluginManager().callEvent(endGameEvent);
        if (endGameEvent.isCancelled())
            return;

        if (winner != null)
            onWin(winner);

        CollectionGame.games.remove(this);
    }

    private void onWin(OfflinePlayer winner) {
        GameWinEvent winEvent = new GameWinEvent(this, Config.getPrize(name), winner);
        Bukkit.getPluginManager().callEvent(winEvent);
        if (winEvent.isCancelled())
            return;

        winner = winEvent.getWinner();
        if (winner.isOnline()) {
            Player online = winner.getPlayer();
            online.playSound(online.getLocation(), Config.getWinSound(), 1f, 1f);
        }

        if (CollectionGame.vault != null) {
            CollectionGame.vault.addMoney(winner, winEvent.getPrize());
        }

        String message = Config.getWinMessage(winner, Config.getPrize(name), collections.get(winner.getUniqueId()).size(), this);
        getParticipants().forEach(p -> {
            if (p.isOnline())
                p.getPlayer().sendMessage(message);
        });
    }

    /**
     * Computes the winner for the game, which will be whoever has the highest material count.
     * If tied (which only happens when its ended forcefully), it will be random
     * @return The winner of the game, or null if there's nobody playing the game
     */
    public OfflinePlayer computeWinner() {
        return getPlayerForPlace(1);
    }

    /**
     * Gets the offline player with the highest collection amount, in that place on the leaderboard for this game<br>
     * For example, if the place was set to 1, it will return the player that is winning<br>
     * With a place of 3, it will return the player with the 3rd highest collection
     * @param place The place to select
     * @return The player in that place
     */
    public OfflinePlayer getPlayerForPlace(int place) {
        List<Map.Entry<UUID, Set<ItemStack>>> places = collections.entrySet().stream()
                .sorted(Comparator.comparingInt(entry -> entry.getValue().size())).collect(Collectors.toList());
        return places.isEmpty() ? null : Bukkit.getOfflinePlayer(places.get(place - 1).getKey());
    }

    /**
     * Gets whether the player's collection contains a similar item
     * @param player The player to get the collection for
     * @param item The item
     * @return true if the collection contains the item, false otherwise
     */
    public boolean collectionContainsItem(OfflinePlayer player, ItemStack item) {
        Optional<ItemStack> out = collections.getOrDefault(player.getUniqueId(), new HashSet<>()).stream()
                .filter(i -> Util.hasCustomData(i) ? i.isSimilar(item) : i.getType() == item.getType()).findAny();
        return out.isPresent();
    }

    /**
     * Gets the equivalent item that was found in the required set, or nothing or no item was matched.
     * @param item The item to match
     * @return The item that was found in the required set, if present
     */
    public Optional<ItemStack> getRequiredEquivalent(ItemStack item) {
        return getRequiredItems().stream()
                .filter(i -> Util.hasCustomData(i) ? i.isSimilar(item) : i.getType() == item.getType()).findFirst();
    }

    public Mode getMode() {
        return mode;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return display;
    }

    public void setDisplayName(String display) {
        this.display = display;
    }

    /**
     * Gets the comparator that sorts that items in the collection GUIs
     */
    public Comparator<ItemStack> getComparator() {
        return comparator;
    }

    /**
     * Gets the comparator that sorts that items in the collection GUIs
     * The comparator is transient and is not persistently saved
     */
    public void setComparator(Comparator<ItemStack> comparator) {
        this.comparator = comparator;
        TreeSet<ItemStack> newRequired = new TreeSet<>(comparator);
        newRequired.addAll(required);
        required = newRequired;
        updateGame();
    }

    public enum Mode {
        BLACKLIST,
        WHITELIST
    }
}