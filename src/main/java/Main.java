import database.DatabaseManager;

public class Main {
    public static void main(String[] args) {
        DatabaseManager dbManager = DatabaseManager.getInstance();
        if (dbManager.getConnection() != null) {
            System.out.println("SQLite setup complete!");
        }
    }
}