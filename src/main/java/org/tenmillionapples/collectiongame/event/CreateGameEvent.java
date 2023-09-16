package org.tenmillionapples.collectiongame.event;

import org.tenmillionapples.collectiongame.Game;

public class StartGameEvent extends GameEvent {
    public StartGameEvent(Game game) {
        this.game = game;
    }
}
