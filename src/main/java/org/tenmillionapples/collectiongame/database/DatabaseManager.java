package org.tenmillionapples.collectiongame.database;

import com.mysql.cj.jdbc.MysqlConnectionPoolDataSource;
import com.mysql.cj.jdbc.MysqlDataSource;
import org.bukkit.inventory.ItemStack;
import org.tenmillionapples.collectiongame.CollectionGame;
import org.tenmillionapples.collectiongame.Config;
import org.tenmillionapples.collectiongame.Game;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.*;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * SQL database manager dependent on and closely linked with the CollectionGame class/module
 * Should not be used by any other plugins
 */
public class DatabaseManager {
    private final CollectionGame game;
    private final MysqlDataSource source;

    public DatabaseManager(CollectionGame game) {
        this.game = game;
        source = new MysqlConnectionPoolDataSource();

        // Configure database
        Config.Database database = Config.getDatabase();
        source.setServerName(database.host);
        source.setPortNumber(database.port);
        source.setDatabaseName(database.databaseName);
        source.setUser(database.user);
        source.setPassword(database.password);

        try {
            testDataSource(source);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates the database tables and datatypes if they don't already exist
     */
    private void checkDatabase() {
        try(Connection connection = source.getConnection()) {
            String createGameTable = "CREATE TABLE games (\n" +
                    "    game_id INT AUTO_INCREMENT PRIMARY KEY,\n" +
                    "    game_display VARCHAR(255) NOT NULL\n" +
                    "    game_name VARCHAR(255) NOT NULL\n" +
                    ");";

            String createCollectionTable = "CREATE TABLE collections (\n" +
                    "    collection_id INT AUTO_INCREMENT PRIMARY KEY,\n" +
                    "    game_id INT NOT NULL,\n" +
                    "    player_uuid VARCHAR(36) NOT NULL,\n" +
                    "    data TEXT,\n" +
                    "    FOREIGN KEY (game_id) REFERENCES games(game_id)\n" +
                    ");";

            Statement statement = connection.createStatement();
            statement.executeUpdate(createGameTable);
            statement.executeUpdate(createCollectionTable);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void loadData() {
        checkDatabase();
        try(Connection connection = source.getConnection()) {
            PreparedStatement selectGameStatement = connection.prepareStatement("SELECT * FROM games");
            PreparedStatement selectCollectionStatement
                    = connection.prepareStatement("SELECT player_uuid, data FROM collections WHERE game_id = ?");

            ResultSet gameResults = selectGameStatement.executeQuery();

            while(gameResults.next()) {
                int gameId = gameResults.getInt("game_id");
                String gameName = gameResults.getString("game_name");
                String gameDisplay = gameResults.getString("game_display");
                Game game = new Game(gameName, gameDisplay);

                selectCollectionStatement.setInt(1, gameId);
                ResultSet collectionResults = selectCollectionStatement.executeQuery();
                while(collectionResults.next()) {
                    UUID playerUUID = UUID.fromString(collectionResults.getString("player_uuid"));
                    ItemStack[] items = ItemSerializer.itemStackArrayFromBase64(collectionResults.getString("data"));

                    game.getCollections().put(playerUUID, Arrays.stream(items).collect(Collectors.toSet()));
                }
            }
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }
        game.getLogger().info("SQL data successfully loaded.");
    }

    public void saveData() {
        try(Connection connection = source.getConnection()) {
            PreparedStatement insertGameStatement = connection.prepareStatement("INSERT INTO games (name) VALUES (?)");
            PreparedStatement insertCollectionStatement
                    = connection.prepareStatement("INSERT INTO collections (game_id, player_uuid, data) VALUES (?, ?, ?)");

            for (Game game : CollectionGame.games) {
                insertGameStatement.setString(1, game.getName());
                insertGameStatement.executeUpdate();

                int gameId = -1;
                try (ResultSet generatedKeys = insertGameStatement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        gameId = generatedKeys.getInt(1);
                    }
                }

                for (Map.Entry<UUID, Set<ItemStack>> entry : game.getCollections().entrySet()) {
                    String itemData = ItemSerializer.itemStackArrayToBase64(entry.getValue().toArray(new ItemStack[0]));
                    insertCollectionStatement.setInt(1, gameId);
                    insertCollectionStatement.setString(2, entry.getKey().toString());
                    insertCollectionStatement.setString(3, itemData);
                    insertCollectionStatement.executeUpdate();
                }
            }
            connection.commit();
            insertGameStatement.close();
            insertCollectionStatement.close();
        } catch(SQLException e) {
            throw new RuntimeException(e);
        }
        game.getLogger().info("SQL data successfully saved.");
    }

    private void testDataSource(DataSource dataSource) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            if (!conn.isValid(1)) {
                throw new SQLException("Could not establish database connection.");
            }
        }
    }
}
