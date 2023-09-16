package org.tenmillionapples.collectiongame.command.executors;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.tenmillionapples.collectiongame.CollectionGame;
import org.tenmillionapples.collectiongame.Config;
import org.tenmillionapples.collectiongame.Game;
import org.tenmillionapples.collectiongame.command.GameCommand;
import org.tenmillionapples.collectiongame.event.CreateGameEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.bukkit.ChatColor.*;
import static org.tenmillionapples.collectiongame.command.GameCommand.ArgumentType.DISPLAY_NAME;
import static org.tenmillionapples.collectiongame.command.GameCommand.ArgumentType.NAME;
import static org.tenmillionapples.collectiongame.command.GameCommand.ReferenceType.NONE;

public class CreateGame extends GameCommand {
    public CreateGame() {
        super("creategame", false, NONE, NAME, DISPLAY_NAME);
    }

    @Override
    protected void onCommandInternal(CommandSender sender, List<Object> objects) {
        String name = (String) objects.get(0);
        String display = (String) objects.get(1);
        Game game = new Game(name, display);

        CreateGameEvent event = new CreateGameEvent(game);
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
            CollectionGame.games.add(game);
            sender.sendMessage(String.format(GREEN + "Successfully created game %s " + GREEN + "(%s).",
                    game.getDisplayName(), game.getName()));
        }
    }
}
