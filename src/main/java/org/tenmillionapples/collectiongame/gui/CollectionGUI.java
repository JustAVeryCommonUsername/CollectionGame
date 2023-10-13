package org.tenmillionapples.collectiongame.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.tenmillionapples.collectiongame.Config;
import org.tenmillionapples.collectiongame.Util;

import java.util.List;

public class CollectionGUI {
    private final Player viewer;
    private final List<ItemStack> items;
    private final String title;
    private int page = 0;

    private static final Material PAGE_SELECTION_MAT = Material.ARROW;

    public CollectionGUI(Player viewer, String title, List<ItemStack> items) {
        this.viewer = viewer;
        this.title = title;
        this.items = items;
    }

    /**
     * Opens the gui to the current page for the player
     */
    public void openGUI() {
        openGUIInternal();
    }

    /**
     * Opens the gui to the next page for the player, also incrementing the page counter
     */
    public void nextPage() {
        if (page == getMaxPage()) {
            throw new IllegalStateException("Page count must be less than the maximum page of" + getMaxPage());
        }
        page++;
        openGUIInternal();
    }

    /**
     * Opens the gui to the next page for the player, also incrementing the page counter
     */
    public void previousPage() {
        if (page < 1) {
            throw new IllegalStateException("Page count must be 0 or greater");
        }
        page--;
        openGUIInternal();
    }

    private void openGUIInternal() {
        int slotCount = getSlotCount();
        String title = String.format("%s (%s/%s)", this.title, page + 1, getMaxPage() + 1);
        Inventory inv = Bukkit.createInventory(null, slotCount + 9, title);

        // Add items
        List<ItemStack> subList = items.subList(page * slotCount, Math.min(items.size(), ((page + 1) * slotCount)));
        subList.forEach(inv::addItem);

        // Add glass
        ItemStack glass = Util.createItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for(int i = slotCount; i < slotCount + 9; i++)
            inv.setItem(i, glass.clone());

        // Add arrows
        if (page > 0) {
            inv.setItem(slotCount, Util.createItem(PAGE_SELECTION_MAT, ChatColor.RESET + "Previous page"));
        }
        if (page < getMaxPage()) {
            inv.setItem(slotCount + 8, Util.createItem(PAGE_SELECTION_MAT, ChatColor.RESET + "Next page"));
        }

        viewer.openInventory(inv);
    }

    public int getMaxPage() {
        return (items.size() - 1) / getSlotCount();
    }

    private int getSlotCount() {
        return Config.getGuiRows() * 9;
    }

    /**
     * Gets the title for the inventory of the gui, not including the page count
     */
    public String getTitle() {
        return title;
    }

    /**
     * Gets the material list for the collection gui<br>
     * This list can be modified
     * @return A mutable list of materials
     */
    public List<ItemStack> getItems() {
        return items;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public Player getViewer() {
        return viewer;
    }
}
