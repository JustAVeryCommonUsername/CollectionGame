package org.tenmillionapples.collectiongame.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.tenmillionapples.collectiongame.CollectionGame;
import org.tenmillionapples.collectiongame.Config;
import org.tenmillionapples.collectiongame.gui.CollectionGUI;

import java.util.HashSet;
import java.util.Set;

public class InventoryListener implements Listener {
    private static final Set<Player> forcedClosed = new HashSet<>();

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        Player player = (Player) e.getPlayer();
        CollectionGUI gui = CollectionGame.getGui(player);
        if (gui == null)
            return;

        if (forcedClosed.remove(player))
            return;

        CollectionGame.guis.remove(gui);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        CollectionGUI gui = CollectionGame.getGui(player);
        if (gui == null)
            return;

        int lowerSlot = Config.getGuiRows() * 9;
        if (e.getSlot() == lowerSlot && gui.getPage() > 0) { // Previous page
            forcedClosed.add(player);
            gui.previousPage();
        } else if (e.getSlot() == lowerSlot + 8 && gui.getPage() < gui.getMaxPage()) { // Next page
            forcedClosed.add(player);
            gui.nextPage();
        }

        e.setCancelled(true);
    }
}
