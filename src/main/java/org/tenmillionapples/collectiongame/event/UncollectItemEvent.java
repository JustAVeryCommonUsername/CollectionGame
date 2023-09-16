package org.tenmillionapples.collectiongame.event;

import org.bukkit.OfflinePlayer;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.tenmillionapples.collectiongame.Game;

public class UncollectItemEvent extends CollectionEvent {
    private static final HandlerList HANDLERS = new HandlerList();

    public UncollectItemEvent(OfflinePlayer player, ItemStack mat, Game game) {
        this.player = player;
        this.item = mat;
        this.game = game;
    }
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
