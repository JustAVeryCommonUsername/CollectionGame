package org.tenmillionapples.collectiongame.command;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.tenmillionapples.collectiongame.CollectionGame;
import org.tenmillionapples.collectiongame.Config;
import org.tenmillionapples.collectiongame.Game;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.bukkit.ChatColor.*;
import static org.tenmillionapples.collectiongame.command.GameCommand.ArgumentType.*;

@CommandName(name = "addtogame")
public class AddToGame extends GameCommand {
    public AddToGame() {
        super(false, PLAYER, GAME);
    }

    @Override
    protected void onCommandInternal(CommandSender sender, List<Object> objects) {
        OfflinePlayer player = (OfflinePlayer) objects.get(0);
        Game game = (Game) objects.get(1);
        
        game.addToGame(player);
        sender.sendMessage(String.format(GREEN + "Successfully added %s to game %s.", player.getName(), game.getDisplayName()));

        if (player.isOnline()) {
            Player online = player.getPlayer();
            String message = Config.getAddPlayerMessage(online, new ItemStack(Material.APPLE),
                    Config.getPrize(game.getName()), 0, game);
            online.sendMessage(message);
        }
    }
}
