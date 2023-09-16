package org.tenmillionapples.collectiongame.event;

import org.bukkit.OfflinePlayer;
import org.bukkit.event.HandlerList;
import org.tenmillionapples.collectiongame.Game;

public class EndGameEvent extends GameEvent {
    private static final HandlerList HANDLERS = new HandlerList();

    protected OfflinePlayer winner;

    protected final EndCause cause;

    public EndGameEvent(OfflinePlayer winner, EndCause cause, Game game) {
        this.winner = winner;
        this.cause = cause;
        this.game = game;
    }

    public void setWinner(OfflinePlayer player) {
        winner = player;
    }

    public OfflinePlayer getWinner() {
        return winner;
    }

    public EndCause getCause() {
        return cause;
    }

    public enum EndCause {
        GOAL,
        FORCE
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
