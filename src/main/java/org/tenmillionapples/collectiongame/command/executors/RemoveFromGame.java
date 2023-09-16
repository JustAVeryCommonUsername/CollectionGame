package org.tenmillionapples.collectiongame.command.executors;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.tenmillionapples.collectiongame.Game;
import org.tenmillionapples.collectiongame.command.GameCommand;

import java.util.List;

import static org.bukkit.ChatColor.GREEN;
import static org.bukkit.ChatColor.RED;
import static org.tenmillionapples.collectiongame.command.GameCommand.ArgumentType.GAME;
import static org.tenmillionapples.collectiongame.command.GameCommand.ArgumentType.PLAYER;
import static org.tenmillionapples.collectiongame.command.GameCommand.ReferenceType.PLAYER_ARGUMENT;

public class RemoveFromGame extends GameCommand {
    public RemoveFromGame() {
        super("removefromgame", false, PLAYER_ARGUMENT, PLAYER, GAME);
    }

    @Override
    protected void onCommandInternal(CommandSender sender, List<Object> objects) {
        OfflinePlayer player = (Player) objects.get(0);
        Game game = (Game) objects.get(1);

        if (game.removeFromGame(player)) {
            sender.sendMessage(String.format(GREEN + "Successfully removed %s from game %s.", player.getName(), game.getName()));
        } else {
            sender.sendMessage(RED + "Player isn't participating in that game.");
        }
    }
}
