package org.tenmillionapples.collectiongame.command;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.tenmillionapples.collectiongame.CollectionGame;
import org.tenmillionapples.collectiongame.Game;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.bukkit.ChatColor.*;

@CommandName(name = "removefromgame")
public class RemoveFromGame implements TabExecutor {
    /*
    /removefromgame <player> <game>
     */

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(String.format(RED + "/%s <player> <game>", label));
            return true;
        }

        OfflinePlayer offline = Bukkit.getOfflinePlayer(args[0]);
        if (!offline.hasPlayedBefore()) {
            sender.sendMessage(String.format(RED + "Unknown player: %s", args[1]));
            return true;
        }

        Game game = CollectionGame.getGameByName(args[1]);
        if (game == null) {
            sender.sendMessage(String.format(RED + "Unknown game: %s", args[0]));
            return true;
        }

        game.removeFromGame(offline);
        sender.sendMessage(String.format(GREEN + "Successfully removed %s from game %s.", offline.getName(), game.getDisplayName()));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
        } else if (args.length == 2) {
            if (sender instanceof Player) {
                return CollectionGame.getGames((Player) sender).stream().map(Game::getName).collect(Collectors.toList());
            } else {
                return CollectionGame.games.stream().map(Game::getName).collect(Collectors.toList());
            }
        }

        return new ArrayList<>();
    }
}
