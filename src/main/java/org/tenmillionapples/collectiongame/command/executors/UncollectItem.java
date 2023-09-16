package org.tenmillionapples.collectiongame.command;

import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.tenmillionapples.collectiongame.CollectionGame;
import org.tenmillionapples.collectiongame.Game;
import org.tenmillionapples.collectiongame.Util;
import org.tenmillionapples.collectiongame.event.UncollectItemEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.bukkit.ChatColor.GREEN;
import static org.bukkit.ChatColor.RED;

@CommandName(name = "uncollectitem")
public class UncollectItem implements TabExecutor {
     /*
     /uncollectitem <player> <item> [game]
     */

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (CollectionGame.games.isEmpty()) {
            sender.sendMessage(RED + "There are no games currently active.");
            return true;
        }
        boolean oneGame = CollectionGame.games.size() == 1;

        if (args.length < (oneGame ? 2 : 3)) {
            sender.sendMessage(String.format(RED + "Usage: /%s <player> <item> [game]", label));
            return true;
        }

        OfflinePlayer player = Bukkit.getOfflinePlayer(args[0]);
        if (!player.hasPlayedBefore()) {
            sender.sendMessage(String.format(RED + "Invalid player: %s", args[0]));
            return true;
        }

        Material mat = Material.getMaterial(args[1].toUpperCase());
        if (mat == null) {
            sender.sendMessage(String.format(RED + "Unknown item: %s", args[2]));
            return true;
        }

        Game game = CollectionGame.getGameByName(args[2]);
        if (game == null) {
            sender.sendMessage(String.format(RED + "Unknown game: %s", args[1]));
            return true;
        }


        ItemStack item = new ItemStack(mat);

        if (!game.collectionContainsItem(player, item)) {
            sender.sendMessage(String.format(RED + "%s isn't collected by %s.", WordUtils.capitalize(mat.name().toLowerCase()), player.getName()));
            return true;
        }
        UncollectItemEvent event = new UncollectItemEvent(player, item, game);
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
            ItemStack newItem = event.getItem();
            sender.sendMessage(String.format(GREEN + "Removed %s from %s's collection.", Util.getItemName(newItem), player.getName()));
            game.uncollectItem(player.getUniqueId(), newItem);
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
        } else if (args.length == 2){
            return Util.getValidMatNames(Util.allValidMats(), args[2]);
        } else if (args.length == 3) {
            String string = args[0];
            Player player = Bukkit.getPlayer(string);
            if (player != null) {
                Set<Game> games = CollectionGame.getGames(player);
                return games.stream().map(Game::getName).collect(Collectors.toList());
            }
        }

        return new ArrayList<>();
    }
}
