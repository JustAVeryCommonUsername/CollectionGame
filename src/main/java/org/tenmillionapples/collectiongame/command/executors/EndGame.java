package org.tenmillionapples.collectiongame.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.tenmillionapples.collectiongame.CollectionGame;
import org.tenmillionapples.collectiongame.Game;
import org.tenmillionapples.collectiongame.event.EndGameEvent;

import java.util.List;
import java.util.stream.Collectors;

import static org.bukkit.ChatColor.RED;

@CommandName(name = "endgame")
public class EndGame implements TabExecutor {
    /*
    /endgame <name>
     */

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(String.format(RED + "Usage: /%s <game>", label));
            return true;
        }

        Game game = CollectionGame.getGameByName(args[0]);
        if (game == null) {
            sender.sendMessage(String.format(RED + "Unknown game: %s", args[0]));
            return true;
        }

        game.endGame(EndGameEvent.EndCause.FORCE);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return CollectionGame.games.stream().map(Game::getName).collect(Collectors.toList());
    }
}
