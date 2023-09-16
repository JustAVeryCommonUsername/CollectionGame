package org.tenmillionapples.collectiongame.plugins;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tenmillionapples.collectiongame.CollectionGame;
import org.tenmillionapples.collectiongame.Config;
import org.tenmillionapples.collectiongame.Game;

import java.util.*;
import java.util.stream.Collectors;

import static org.bukkit.ChatColor.RED;

public class PAPIHook extends PlaceholderExpansion{
    private final CollectionGame game;

    public PAPIHook(CollectionGame game) {
        this.game = game;
        register();
    }

    @Override
    public @NotNull String getIdentifier() {
        return "collectiongame";
    }

    @Override
    public @NotNull String getAuthor() {
        return "TenMillionApples";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        Map<String, ParamType[]> identifiers = new HashMap<>();
        identifiers.put("current_game_", new ParamType[]{ParamType.INDEX});
        identifiers.put("display_name_", new ParamType[]{ParamType.GAME});
        identifiers.put("mode_", new ParamType[]{ParamType.GAME});
        identifiers.put("required_amount_", new ParamType[]{ParamType.GAME});
        identifiers.put("collected_amount_", new ParamType[]{ParamType.GAME});
        identifiers.put("participants_", new ParamType[]{ParamType.GAME});
        identifiers.put(("prize_"), new ParamType[]{ParamType.GAME});
        identifiers.put(("largest_collection_player_"), new ParamType[]{ParamType.INDEX, ParamType.GAME});
        identifiers.put(("largest_collection_amount_"), new ParamType[]{ParamType.INDEX, ParamType.GAME});

        for (Map.Entry<String, ParamType[]> entry : identifiers.entrySet()) {
            String id = entry.getKey();
            ParamType[] types = entry.getValue();
            if (!params.startsWith(id))
                continue;

            Object[] objects = new Object[types.length];
            String newParams = params.substring(id.length());
            int i = 0;
            for(ParamType type : types) {
                if (type == ParamType.INDEX) {
                    try {
                        objects[i] = Integer.parseInt(newParams.substring(0, newParams.indexOf("_")));
                    } catch (NumberFormatException e) {
                        return "Invalid index";
                    }
                } else if (type == ParamType.GAME) {
                    String name = newParams.substring(newParams.indexOf("_"));
                    Game game = CollectionGame.getGameByName(name);
                    if (game == null) {
                        return "Unknown game";
                    }
                    objects[i] = game;
                }
                i++;
            }
            return placeholderInternal(player, id, objects);
        }
        return null;
    }

    private String placeholderInternal(OfflinePlayer player, @NotNull String id, Object[] params) {
        switch (id) {
            case "current_game_": {
                Set<Game> games = CollectionGame.getGames(player);
                if (games.isEmpty()) {
                    return RED + "Not in a game";
                }
                try {
                    List<Game> gamesSorted = games.stream().sorted(Comparator.comparing(Game::getName)).collect(Collectors.toList());
                    return gamesSorted.get((int) params[0]).getName();
                } catch (IndexOutOfBoundsException e) {
                    return RED + "Invalid index";
                }
            }
            case "display_name_": {
                return ((Game) params[0]).getDisplayName();
            }
            case "mode_": {
                return ((Game) params[0]).getMode().name().toLowerCase();
            }
            case "required_amount_": {
                return Integer.toString(((Game) params[0]).getRequiredItems().size());
            }
            case "collected_amount_": {
                return Integer.toString(((Game) params[0]).getCollections().get(player.getUniqueId()).size());
            }
            case "participants_": {
                return Integer.toString(((Game) params[0]).getParticipants().size());
            }
            case "prize_": {
                return Double.toString(Config.getPrize(((Game) params[0]).getName()));
            }
            case "largest_collection_player_": {
                int index = (int) params[0];
                Game game = (Game) params[1];

                OfflinePlayer other = game.getPlayerForPlace(index);
                if (other == null) {
                    return RED + "No player";
                }
                return other.getName();
            }
            case "largest_collection_amount_": {
                int index = (int) params[0];
                Game game = (Game) params[1];

                OfflinePlayer other = game.getPlayerForPlace(index);
                if (other == null) {
                    return RED + "No amount";
                }
                return Integer.toString(game.getCollections().get(other.getUniqueId()).size());
            }
            default: {
                return null;
            }
        }
    }

    public enum ParamType {
        GAME,
        INDEX
    }
}
