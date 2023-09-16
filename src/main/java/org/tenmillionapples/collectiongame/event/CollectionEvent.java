package org.tenmillionapples.collectiongame.event;

import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;

public abstract class CollectionEvent extends GameEvent {
    protected ItemStack item;

    protected OfflinePlayer player;

    /**
     * Gets the item stack associated with the collection event
     * @return The item stack
     */
    public ItemStack getItem() {
        return item;
    }

    /**
     * Sets the item associated with the collection event
     * @param item The item stack
     */
    public void setItem(ItemStack item){
        this.item = item;
    };

    /**
     * Gets the offline player that triggered the event
     * @return The offline player
     */
    public OfflinePlayer getPlayer() {
        return player;
    }
}
