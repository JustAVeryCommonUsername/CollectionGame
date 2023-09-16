package org.tenmillionapples.collectiongame.command.executors;

import org.bukkit.command.CommandSender;
import org.tenmillionapples.collectiongame.Game;
import org.tenmillionapples.collectiongame.command.GameCommand;
import org.tenmillionapples.collectiongame.event.EndGameEvent;

import java.util.List;

import static org.bukkit.ChatColor.*;
import static org.tenmillionapples.collectiongame.command.GameCommand.ArgumentType.GAME;
import static org.tenmillionapples.collectiongame.command.GameCommand.ReferenceType.NONE;

public class EndGame extends GameCommand {
    public EndGame() {
        super("endgame", false, NONE, GAME);
    }

    @Override
    protected void onCommandInternal(CommandSender sender, List<Object> objects) {
        Game game = (Game) objects.get(0);
        game.endGame(EndGameEvent.EndCause.FORCE);
        sender.sendMessage(String.format(GREEN + "Successfully ended game: %s.", game.getName()));
    }
}
