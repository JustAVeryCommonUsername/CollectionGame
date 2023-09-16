package org.tenmillionapples.collectiongame.listener;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.tenmillionapples.collectiongame.CollectionGame;
import org.tenmillionapples.collectiongame.Game;

import java.util.Set;

public class CollectionListener implements Listener {
    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.isCancelled())
            return;

        ItemStack item = e.getCurrentItem();
        Player player = (Player) e.getWhoClicked();
        if (item != null)
            collectItem(player, item);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onItemPickup(EntityPickupItemEvent e) {
        if (e.isCancelled() || e.getEntity().getType() != EntityType.PLAYER)
            return;

        Player player = (Player) e.getEntity();
        ItemStack item = e.getItem().getItemStack();
        collectItem(player, item);
    }

    private void collectItem(Player player, ItemStack item) {
        Set<Game> games = CollectionGame.getGames(player);
        games.forEach(game -> game.collectItem(player, item));
    }
}
