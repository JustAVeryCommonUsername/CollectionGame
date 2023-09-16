package org.tenmillionapples.collectiongame;

import com.google.gson.TypeAdapter;
import com.mysql.cj.jdbc.MysqlConnectionPoolDataSource;
import com.mysql.cj.jdbc.MysqlDataSource;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Set;
import java.util.UUID;

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
                    "    game_name VARCHAR(255) NOT NULL\n" +
                    ");";
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void loadData() {
        checkDatabase();
    }

    public void saveData() {

    }

    private byte[] serializeCollection(Game game, UUID uuid) {
        Set<ItemStack> items = game.getCollections().get(uuid);
        ItemStack item = null;
    }

    private void deserializeCollection(byte[] data) {

    }

    private void testDataSource(DataSource dataSource) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            if (!conn.isValid(1)) {
                throw new SQLException("Could not establish database connection.");
            }
        }
    }
}
