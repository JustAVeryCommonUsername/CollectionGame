package org.tenmillionapples.collectiongame.command;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.tenmillionapples.collectiongame.CollectionGame;
import org.tenmillionapples.collectiongame.Config;
import org.tenmillionapples.collectiongame.Game;
import org.tenmillionapples.collectiongame.event.CreateGameEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.bukkit.ChatColor.*;

@CommandName(name = "creategame")
public class CreateGame implements TabExecutor {
    /*
    /creategame <name> <display>
     */

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(String.format(RED + "Usage: /%s <name> <display>", label));
            return true;
        }

        String name = args[0], display = args[1];

        Set<Material> blacklist = Config.getBlacklist(name), whitelist = Config.getWhitelist(name);

        Game.Mode mode = whitelist.isEmpty() ? Game.Mode.BLACKLIST : Game.Mode.WHITELIST;
        Game game = new Game(name, display, mode);
        game.getWhitelist().addAll(whitelist);
        game.getBlacklist().addAll(blacklist);

        CreateGameEvent event = new CreateGameEvent(game);
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
            CollectionGame.games.add(game);
            sender.sendMessage(String.format(GREEN + "Successfully created game %s (%s)."), game.getDisplayName(), game.getName());
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        return new ArrayList<>();
    }
}
