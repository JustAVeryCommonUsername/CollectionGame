package org.tenmillionapples.collectiongame.command.executors;

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
import org.tenmillionapples.collectiongame.command.GameCommand;
import org.tenmillionapples.collectiongame.event.UncollectItemEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.bukkit.ChatColor.GREEN;
import static org.bukkit.ChatColor.RED;
import static org.tenmillionapples.collectiongame.command.GameCommand.ArgumentType.*;
import static org.tenmillionapples.collectiongame.command.GameCommand.ReferenceType.PLAYER_ARGUMENT;

public class UncollectItem extends GameCommand {
    public UncollectItem() {
        super("uncollectitem", false, PLAYER_ARGUMENT, PLAYER, MATERIAL, GAME);
    }

    @Override
    protected void onCommandInternal(CommandSender sender, List<Object> objects) {
        OfflinePlayer player = (OfflinePlayer) objects.get(0);
        Material mat = (Material) objects.get(1);
        Game game = (Game) objects.get(2);

        ItemStack item = new ItemStack(mat);

        if (!game.collectionContainsItem(player, item)) {
            sender.sendMessage(String.format(RED + "%s isn't collected by %s.",
                    WordUtils.capitalize(mat.name().toLowerCase()), player.getName()));
            return;
        }

        UncollectItemEvent event = new UncollectItemEvent(player, item, game);
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
            ItemStack newItem = event.getItem();
            sender.sendMessage(String.format(GREEN + "Removed %s from %s's collection.", Util.getItemName(newItem), player.getName()));
            game.uncollectItem(player.getUniqueId(), newItem);
        }
    }
}
