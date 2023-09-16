package org.tenmillionapples.collectiongame.event;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.tenmillionapples.collectiongame.Game;

public abstract class GameEvent extends Event implements Cancellable {
    private boolean isCancelled = false;

    protected Game game;

    public Game getGame() {
        return game;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        isCancelled = cancelled;
    }
}
