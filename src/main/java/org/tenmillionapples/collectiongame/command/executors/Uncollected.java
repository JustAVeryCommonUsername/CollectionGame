package org.tenmillionapples.collectiongame.command.executors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.tenmillionapples.collectiongame.CollectionGame;
import org.tenmillionapples.collectiongame.Game;
import org.tenmillionapples.collectiongame.command.GameCommand;
import org.tenmillionapples.collectiongame.event.GUIOpenEvent;

import java.util.List;
import java.util.stream.Collectors;

import static org.bukkit.ChatColor.RED;
import static org.tenmillionapples.collectiongame.command.GameCommand.ArgumentType.GAME;
import static org.tenmillionapples.collectiongame.command.GameCommand.ReferenceType.COMMAND_SENDER;

public class Uncollected extends GameCommand {
    public Uncollected() {
        super("uncollected", true, COMMAND_SENDER, GAME);
    }

    @Override
    protected void onCommandInternal(CommandSender sender, List<Object> objects) {
        Player player = (Player) sender;
        Game game = (Game) objects.get(0);
        if (!game.getParticipants().contains(player)) {
            player.sendMessage(RED + "You are not part of this game.");
            return;
        }

        CollectionGame.openGUI(player, player, game, GUIOpenEvent.GUIType.UNCOLLECTED);
    }
}