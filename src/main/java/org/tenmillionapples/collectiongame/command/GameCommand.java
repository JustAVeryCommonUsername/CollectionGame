package org.tenmillionapples.collectiongame.command;

import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.tenmillionapples.collectiongame.CollectionGame;
import org.tenmillionapples.collectiongame.Game;
import org.tenmillionapples.collectiongame.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.bukkit.ChatColor.RED;

/**
 * A class all game commands extend for this plugin<br>
 * This class is very strict in how the commands should work, and should not be used for any other plugin<br>
 * For instance, if the last argument is a game, it is always assumed to be optional if there's only 1 game.
 */
public abstract class GameCommand implements TabExecutor {
    private final ArgumentType[] argumentTypes;
    private final boolean mustBePlayer;
    private final String commandName;
    private final ReferenceType referenceType;

    public GameCommand(String commandName, boolean mustBePlayer, ReferenceType referenceType, ArgumentType... argumentTypes) {
        this.commandName = commandName;
        this.mustBePlayer = mustBePlayer;
        this.referenceType = referenceType;
        this.argumentTypes = argumentTypes;
    }

    protected abstract void onCommandInternal(CommandSender sender, List<Object> objects);

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player) && mustBePlayer) {
            sender.sendMessage(RED + "Only players can use this command.");
            return true;
        }

        // Test if all arguments are there
        boolean oneGame = ArrayUtils.contains(argumentTypes, ArgumentType.GAME) && CollectionGame.games.size() == 1;
        if (args.length < argumentTypes.length - (oneGame ? 1 : 0)) {
            sender.sendMessage(String.format(RED + "Usage: %s.", getUsage(label)));
            return true;
        }

        List<Object> objects = new ArrayList<>();
        int i = 0;
        for(ArgumentType type : argumentTypes) {
            String arg;
            if (oneGame && type == ArgumentType.GAME) {
                arg = CollectionGame.games.stream().findAny().get().getName();
            } else {
                arg = args[i];
            }

            switch (type) {
                case PLAYER: {
                    OfflinePlayer player = Bukkit.getOfflinePlayer(arg);
                    if (!player.hasPlayedBefore()) {
                        sender.sendMessage(String.format(RED + "Unknown player: %s.", arg));
                        return true;
                    }
                    objects.add(player);
                    break;
                }
                case GAME: {
                    Game game = CollectionGame.getGameByName(arg);
                    if (game == null) {
                        sender.sendMessage(String.format(RED + "Unknown game: %s.", arg));
                        return true;
                    }
                    objects.add(game);
                    break;
                }
                case MATERIAL: {
                    Material mat = Material.getMaterial(arg.toUpperCase());
                    if (mat == null) {
                        sender.sendMessage(String.format(RED + "Unknown item: %s.", arg));
                        return true;
                    }
                    objects.add(mat);
                    break;
                }
                case NAME: {
                    if (CollectionGame.games.stream().anyMatch(g -> g.getName().equals(arg))) {
                        sender.sendMessage(String.format(RED + "Already a game with name: %s.", arg));
                        return true;
                    }
                    objects.add(arg);
                    break;
                }
                case DISPLAY_NAME: {
                    objects.add(arg);
                    break;
                }
            }
            i++;
        }

        onCommandInternal(sender, objects);
        return true;
    }

    private String getUsage(String label) {
        StringBuilder builder = new StringBuilder("/" + label);

        for (ArgumentType type : argumentTypes) {
            builder.append(" ").append(type.getUsagedDisplay());
        }

        return builder.toString();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > argumentTypes.length) {
            return new ArrayList<>();
        }

        ArgumentType type = argumentTypes[args.length - 1];
        String currentArg = args[args.length - 1];
        switch (type) {
            case PLAYER:
                return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
            case GAME: {
                OfflinePlayer player = null;
                if (referenceType == ReferenceType.NONE) {
                    return CollectionGame.games.stream().map(Game::getName).collect(Collectors.toList());
                } else if (referenceType == ReferenceType.PLAYER_ARGUMENT) {
                    int playerIndex = ArrayUtils.indexOf(argumentTypes, ArgumentType.PLAYER);

                    player = Bukkit.getOfflinePlayer(args[playerIndex]);
                    if (!player.hasPlayedBefore())
                        break;
                } else if (referenceType == ReferenceType.COMMAND_SENDER) {
                    if (sender instanceof Player) {
                        player = (Player) sender;
                    } else {
                        return CollectionGame.games.stream().map(Game::getName).collect(Collectors.toList());
                    }
                }

                // Return games the player is in
                return CollectionGame.getGames(player).stream().map(Game::getName)
                        .filter(s -> currentArg.isEmpty() || s.contains(currentArg))
                        .collect(Collectors.toList());
            }
            case MATERIAL:
                return Util.getValidMatNames(Util.allValidMats(), currentArg);
            default:
                break;
        }

        return new ArrayList<>();
    }

    public String getCommandName() {
        return commandName;
    }

    public enum ArgumentType {
        PLAYER("<player>"),
        GAME("[game]"),
        NAME("<name>"),
        DISPLAY_NAME("<display name>"),
        MATERIAL("<item>");

        private final String usageDisplay;

        ArgumentType(String usageDisplay) {
            this.usageDisplay = usageDisplay;
        }

        public String getUsagedDisplay() {
            return usageDisplay;
        }
    }

    public enum ReferenceType {
        PLAYER_ARGUMENT,
        COMMAND_SENDER,
        NONE
    }
}
