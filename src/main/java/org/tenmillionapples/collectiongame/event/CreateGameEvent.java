package org.tenmillionapples.collectiongame.event;

import org.bukkit.event.HandlerList;
import org.tenmillionapples.collectiongame.Game;

/**
 * This event is called whenever a game is created
 */
public class CreateGameEvent extends GameEvent {
    private static final HandlerList HANDLERS = new HandlerList();

    public CreateGameEvent(Game game) {
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
