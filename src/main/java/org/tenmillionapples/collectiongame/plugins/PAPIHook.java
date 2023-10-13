package org.tenmillionapples.collectiongame.plugins;

import me.clip.placeholderapi.PlaceholderAPI;
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
    private final Map<String, ParamType[]> identifiers = new LinkedHashMap<>();

    public PAPIHook() {
        identifiers.put("current_game_", new ParamType[]{ParamType.INDEX});
        identifiers.put("current_game", new ParamType[]{});
        identifiers.put("display_name_", new ParamType[]{ParamType.GAME});
        identifiers.put("mode_", new ParamType[]{ParamType.GAME});
        identifiers.put("required_amount_", new ParamType[]{ParamType.GAME});
        identifiers.put("collected_amount_", new ParamType[]{ParamType.GAME});
        identifiers.put("uncollected_amount_", new ParamType[]{ParamType.GAME});
        identifiers.put("participants_", new ParamType[]{ParamType.GAME});
        identifiers.put(("prize_"), new ParamType[]{ParamType.GAME});
        identifiers.put(("largest_collection_player_"), new ParamType[]{ParamType.INDEX, ParamType.GAME});
        identifiers.put(("largest_collection_amount_"), new ParamType[]{ParamType.INDEX, ParamType.GAME});
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
        params = PlaceholderAPI.setBracketPlaceholders(player, params);

        for (Map.Entry<String, ParamType[]> entry : identifiers.entrySet()) {
            String id = entry.getKey();
            ParamType[] types = entry.getValue();
            if (!params.startsWith(id))
                continue;

            Object[] objects = new Object[types.length];
            String newParams = params.substring(id.length());
            int i = 0;
            for(ParamType type : types) {
                String currentParam;
                if (types.length > 1)
                    currentParam = i == 0 ? newParams.substring(0, newParams.indexOf('_'))
                            : newParams.substring(newParams.indexOf('_') + 1);
                else
                    currentParam = newParams;

                if (type == ParamType.INDEX) {
                    try {
                        objects[i] = Integer.parseInt(currentParam);
                    } catch (NumberFormatException e) {
                        return "Invalid index";
                    }
                } else if (type == ParamType.GAME) {
                    Game game = CollectionGame.getGameByName(currentParam);
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
            case "current_game": {
                Set<Game> games = CollectionGame.getGames(player);
                if (games.isEmpty()) {
                    return RED + "Not in a game";
                }
                return games.stream().findFirst().get().getName();
            }
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
                return Integer.toString(((Game) params[0]).getCollections().getOrDefault(player.getUniqueId(), new HashSet<>()).size());
            }
            case "uncollected_amount_": {
                Game game = (Game) params[0];
                int requiredSize = game.getRequiredItems().size();
                return Integer.toString(requiredSize - game.getCollections().getOrDefault(player.getUniqueId(), new HashSet<>()).size());
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
