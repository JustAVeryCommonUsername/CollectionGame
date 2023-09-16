package org.tenmillionapples.collectiongame.event;

import org.bukkit.event.HandlerList;
import org.tenmillionapples.collectiongame.Game;
import org.tenmillionapples.collectiongame.gui.CollectionGUI;

/**
 * This event is called right before a collection GUI is opened.
 */
public class GUIOpenEvent extends GameEvent {
    private static final HandlerList HANDLERS = new HandlerList();

    protected final CollectionGUI gui;

    protected final GUIType type;

    public GUIOpenEvent(Game game, CollectionGUI gui, GUIType type) {
        this.game = game;
        this.gui = gui;
        this.type = type;
    }

    public CollectionGUI getGui() {
        return gui;
    }

    public GUIType getType() {
        return type;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public enum GUIType {
        UNCOLLECTED,
        COLLECTED
    }
}
