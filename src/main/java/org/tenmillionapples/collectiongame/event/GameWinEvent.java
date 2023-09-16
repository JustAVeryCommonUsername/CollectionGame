package org.tenmillionapples.collectiongame.event;

import org.bukkit.OfflinePlayer;
import org.bukkit.event.HandlerList;
import org.tenmillionapples.collectiongame.Game;

public class GameWinEvent extends GameEvent {
    private static final HandlerList HANDLERS = new HandlerList();

    protected OfflinePlayer winner;
    protected double prize;

    public GameWinEvent(Game game, double prize, OfflinePlayer winner) {
        this.game = game;
        this.prize = prize;
        this.winner = winner;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public OfflinePlayer getWinner() {
        return winner;
    }

    public void setWinner(OfflinePlayer winner) {
        this.winner = winner;
    }

    public double getPrize() {
        return prize;
    }

    public void setPrize(double prize) {
        this.prize = prize;
    }
}
