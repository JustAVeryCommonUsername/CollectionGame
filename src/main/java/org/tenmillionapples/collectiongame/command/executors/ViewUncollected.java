package org.tenmillionapples.collectiongame.command.executors;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.tenmillionapples.collectiongame.CollectionGame;
import org.tenmillionapples.collectiongame.Game;
import org.tenmillionapples.collectiongame.command.GameCommand;
import org.tenmillionapples.collectiongame.event.GUIOpenEvent;

import java.util.List;

import static org.tenmillionapples.collectiongame.command.GameCommand.ArgumentType.GAME;
import static org.tenmillionapples.collectiongame.command.GameCommand.ArgumentType.PLAYER;
import static org.tenmillionapples.collectiongame.command.GameCommand.ReferenceType.PLAYER_ARGUMENT;

public class ViewUncollected extends GameCommand {
    public ViewUncollected() {
        super("viewuncollected", true, PLAYER_ARGUMENT, PLAYER, GAME);
    }

    @Override
    protected void onCommandInternal(CommandSender sender, List<Object> objects) {
        Player viewer = (Player) sender;
        OfflinePlayer player = (OfflinePlayer) objects.get(0);
        Game game = (Game) objects.get(1);

        CollectionGame.openGUI(viewer, player, game, GUIOpenEvent.GUIType.UNCOLLECTED);
    }
}
