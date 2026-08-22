package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private Connection connection;
    private static DatabaseManager instance;
    private static final String URL = "jdbc:sqlite:sper_data.db";

    private DatabaseManager() {
        try {
            connection = DriverManager.getConnection(URL);
            initializeDatabase();
        } catch (SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
        }
    }
    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

    private void initializeDatabase() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL," +
                "role TEXT NOT NULL);";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }
}
