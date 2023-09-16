package org.tenmillionapples.collectiongame.command.executors;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.tenmillionapples.collectiongame.Config;
import org.tenmillionapples.collectiongame.Game;
import org.tenmillionapples.collectiongame.command.GameCommand;

import java.util.List;

import static org.bukkit.ChatColor.*;
import static org.tenmillionapples.collectiongame.command.GameCommand.ArgumentType.GAME;
import static org.tenmillionapples.collectiongame.command.GameCommand.ArgumentType.PLAYER;
import static org.tenmillionapples.collectiongame.command.GameCommand.ReferenceType.NONE;

public class AddToGame extends GameCommand {
    public AddToGame() {
        super("addtogame", false, NONE, PLAYER, GAME);
    }

    @Override
    protected void onCommandInternal(CommandSender sender, List<Object> objects) {
        OfflinePlayer player = (OfflinePlayer) objects.get(0);
        Game game = (Game) objects.get(1);

        if (game.getParticipants().contains(player)) {
            sender.sendMessage(String.format(RED + "%s is already participating in game %s.", player.getName(), game.getName()));
            return;
        }

        game.addToGame(player);
        sender.sendMessage(String.format(GREEN + "Successfully added %s to game %s.", player.getName(), game.getName()));

        if (player.isOnline()) {
            Player online = player.getPlayer();
            String message = Config.getAddPlayerMessage(online, new ItemStack(Material.APPLE),
                    Config.getPrize(game.getName()), 0, game);
            online.sendMessage(message);
        }
    }
}
