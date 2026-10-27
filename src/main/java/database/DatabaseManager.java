
package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;

public class DatabaseManager {

    private Connection connection;
    private static DatabaseManager instance;

    private static final String URL = "jdbc:sqlite:sper_data.db";

    private DatabaseManager() {
        try {
            connection = DriverManager.getConnection(URL);
            initializeDatabase();
        } catch (SQLException e) {
            System.err.println(
                    "Database connection failed: " + e.getMessage()
            );
        }
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

    private void initializeDatabase() throws SQLException {
        try (Statement stmt = connection.createStatement()) {

            stmt.execute(
                    "CREATE TABLE IF NOT EXISTS users ("
                            + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                            + "name TEXT NOT NULL,"
                            + "role TEXT NOT NULL,"
                            + "password TEXT NOT NULL)"
            );

            stmt.execute(
                    "CREATE TABLE IF NOT EXISTS package_issues ("
                            + "issue_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                            + "tracking_number TEXT NOT NULL,"
                            + "issue_type TEXT NOT NULL,"
                            + "description TEXT,"
                            + "severity TEXT NOT NULL,"
                            + "package_value REAL DEFAULT 0,"
                            + "status TEXT NOT NULL DEFAULT 'REPORTED',"
                            + "customer_id INTEGER,"
                            + "assigned_to INTEGER,"
                            + "handled_by TEXT,"
                            + "created_at TEXT DEFAULT (datetime('now')),"
                            + "resolved_at TEXT,"
                            + "FOREIGN KEY (customer_id) REFERENCES users(id),"
                            + "FOREIGN KEY (assigned_to) REFERENCES users(id))"
            );

            // Safely add the column if the table existed already.
            boolean hasHandledBy = false;

            try (ResultSet rs =
                         stmt.executeQuery("PRAGMA table_info(package_issues)")) {
                while (rs.next()) {
                    if ("handled_by".equalsIgnoreCase(rs.getString("name"))) {
                        hasHandledBy = true;
                        break;
                    }
                }
            }

            if (!hasHandledBy) {
                stmt.execute(
                        "ALTER TABLE package_issues ADD COLUMN handled_by TEXT"
                );
            }

            stmt.execute(
                    "CREATE TABLE IF NOT EXISTS issue_history ("
                            + "history_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                            + "issue_id INTEGER NOT NULL,"
                            + "old_status TEXT,"
                            + "new_status TEXT NOT NULL,"
                            + "changed_by INTEGER,"
                            + "changed_at TEXT DEFAULT (datetime('now')),"
                            + "FOREIGN KEY (issue_id) REFERENCES package_issues(issue_id),"
                            + "FOREIGN KEY (changed_by) REFERENCES users(id))"
            );
        }
    }
}
