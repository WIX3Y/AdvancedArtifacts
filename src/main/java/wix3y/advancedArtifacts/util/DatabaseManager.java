package wix3y.advancedArtifacts.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Bukkit;
import wix3y.advancedArtifacts.AdvancedArtifacts;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DatabaseManager {
    private HikariDataSource dataSource;
    private final AdvancedArtifacts plugin;

    public DatabaseManager(AdvancedArtifacts instance) {
        this.plugin = instance;
    }

    /**
     * Connect to MySQL database
     *
     * @param ip the ip
     * @param port the port
     * @param database the database
     * @param username the username
     * @param password the password
     * @param poolSize the pool size
     */
    public void connect(String ip, String port, String database, String username, String password, int poolSize) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + ip + ":" + port + "/" + database);
        config.setUsername(username);
        config.setPassword(password);
        config.setMaximumPoolSize(poolSize);

        dataSource = new HikariDataSource(config);

        plugin.getLogger().info("Database connection established.");
    }

    /**
     * Add any missing tables
     *
     * @param table the table name
     * @param columnNames list of column names
     */
    public void initialize(String table, List<String> columnNames) {
        StringBuilder columns = new StringBuilder();
        for (String columnName: columnNames) {
            columns.append(columnName).append(" VARCHAR(64), ");
        }

        try (Connection connection = dataSource.getConnection();
             PreparedStatement sql = connection.prepareStatement(
                     "CREATE TABLE IF NOT EXISTS " + table + " (" +
                             "Player_UUID VARCHAR(36) PRIMARY KEY, " + columns.substring(0, columns.length() - 2) + ")")) {
            sql.execute();
        } catch (Exception e) {
            plugin.getLogger().severe("Error initializing the " + table + " table!");
            e.printStackTrace();
        }

        addMissingColumns(table, columnNames);
    }

    /**
     * Add any missing columns to database table
     *
     * @param table the table name
     * @param columns list of columns to be added
     */
    private void addMissingColumns(String table, List<String> columns) {
        for (String column: columns) {
            try (Connection connection = dataSource.getConnection()) {
                // Check if the column exists in the table
                PreparedStatement checkColumnExistQuery = connection.prepareStatement(
                        "SELECT COUNT(*) " +
                                "FROM INFORMATION_SCHEMA.COLUMNS " +
                                "WHERE table_name = '" + table + "' " +
                                "AND column_name = ? "
                );
                checkColumnExistQuery.setString(1, column);
                ResultSet resultSet = checkColumnExistQuery.executeQuery();

                if (resultSet.next() && resultSet.getInt(1) == 0) {
                    // The column doesn't exist, so add it
                    PreparedStatement addColumnQuery = connection.prepareStatement(
                            "ALTER TABLE " + table + " ADD COLUMN " + column + " VARCHAR(64)"
                    );
                    addColumnQuery.executeUpdate();
                    plugin.getLogger().info("Column " + column + " added to table " + table + "!");
                }
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to check or add columns to table " + table + "!");
                e.printStackTrace();
            }
        }
    }

    /**
     * Write string (max of 64-characters) to specific row in database column
     *
     * @param table the table name
     * @param uuid the player row identifier
     * @param column the column name
     * @param value the string value
     */
    public void writeString(String table, String uuid, String column, String value) {
        if (value != null && value.length() > 64) {
            value = value.substring(0, 64);
        }

        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement updateValueQuery = connection.prepareStatement("INSERT INTO " + table +
                    " (Player_UUID, " + column + ") VALUES (?, ?) " + "ON DUPLICATE KEY UPDATE " + column + " = ?");

            updateValueQuery.setString(1, uuid);
            updateValueQuery.setString(2, value);
            updateValueQuery.setString(3, value);
            updateValueQuery.executeUpdate();

        } catch (Exception e) {
            plugin.getLogger().severe("Failed to write value " + value + " for player " + Bukkit.getOfflinePlayer(UUID.fromString(uuid)).getName() + " to column " + column + " in table " + table + "!");
            e.printStackTrace();
        }
    }

    /**
     * Get data belonging to player from MySQL database
     *
     * @param columns list of columns of interest
     * @param uuid uuid of the player
     * @param table the name of the table
     * @return the player wood cutting data
     */
    public Map<String, DirtyString> getPlayerData(String uuid, List<String> columns, String table) {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement getPlayerDataQuery = connection.prepareStatement(
                    "SELECT * FROM " + table + " WHERE Player_UUID = ?"
            );

            getPlayerDataQuery.setString(1, uuid);
            ResultSet rs = getPlayerDataQuery.executeQuery();

            if (!rs.next()) { // Check that player exists in database, if not add the player
                PreparedStatement insertPlayerQuery = connection.prepareStatement("INSERT INTO " + table + " (Player_UUID) VALUES (?)");
                insertPlayerQuery.setString(1, uuid);
                insertPlayerQuery.executeUpdate();
                rs = getPlayerDataQuery.executeQuery();
                if (!rs.next()) {
                    plugin.getLogger().severe("Failed to retrieve player data for player " + Bukkit.getPlayer(UUID.fromString(uuid)) + " from table " + table + " even after inserting!");
                    return null;
                }
            }

            Map<String, DirtyString> data = new ConcurrentHashMap<>();
            for (String column: columns) {
                try {
                    data.put(column, new DirtyString(rs.getString(column), false));
                } catch (SQLException e) {
                    plugin.getLogger().severe("Failed to fetch data from table " + table + " for player " + Bukkit.getPlayer(UUID.fromString(uuid)) + " from column " + column + "!");
                    e.printStackTrace();
                }
            }
            return  data;

        } catch (Exception e) {
            plugin.getLogger().severe("Failed to fetch data for player " + Bukkit.getPlayer(UUID.fromString(uuid)) + " from table " + table + "!");
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Disconnect from database
     *
     */
    public void disconnect() {
        if (dataSource != null) {
            dataSource.close();
            plugin.getLogger().info("Database connection closed.");
        }
    }
}