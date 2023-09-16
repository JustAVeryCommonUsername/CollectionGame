package org.tenmillionapples.collectiongame.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.tenmillionapples.collectiongame.CollectionGame;
import org.tenmillionapples.collectiongame.Game;
import org.tenmillionapples.collectiongame.event.GUIOpenEvent;

import java.util.List;
import java.util.stream.Collectors;

import static org.bukkit.ChatColor.RED;

public class Collected implements TabExecutor {
    /*
    /collected [game]
     */

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (CollectionGame.games.isEmpty()) {
            sender.sendMessage(RED + "There are no games currently active.");
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(RED + "Only players can use this command.");
            return true;
        }

        boolean oneGame = CollectionGame.games.size() == 1;

        if (args.length < (oneGame ? 0 : 1)) {
            sender.sendMessage(String.format(RED + "Usage: /%s [game]", label));
            return true;
        }

        Game game = CollectionGame.getGameByName(args[0]);
        if (game == null) {
            sender.sendMessage(String.format(RED + "Unknown game: %s", args[0]));
            return true;
        }

        Player player = (Player) sender;
        CollectionGame.openGUI(player, player, game, GUIOpenEvent.GUIType.COLLECTED);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return CollectionGame.games.stream().map(Game::getName).collect(Collectors.toList());
    }
}
