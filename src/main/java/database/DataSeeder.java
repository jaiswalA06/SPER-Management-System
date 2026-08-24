package database;

import enums.Role;

public class DataSeeder {

    private final UserDAO userDAO;
    private final PackageIssueDAO issueDAO;

    public DataSeeder() {
        this.userDAO = new UserDAO();
        this.issueDAO = new PackageIssueDAO();
    }

    public void seedAll() {
        seedUsers();
        seedIssues();
    }

    private void seedUsers() {
        if (!userDAO.isEmpty()) {
            return;
        }

        System.out.println("Seeding users...");
        userDAO.insert("admin",      Role.ADMIN,            "admin123");
        userDAO.insert("Ramesh",     Role.CUSTOMER,         "ramesh123");
        userDAO.insert("Rahul",      Role.CUSTOMER,         "rahul123");
        userDAO.insert("Sunderlal",  Role.SUPPORT_STAFF,    "sunder123");
        userDAO.insert("Daya",       Role.HUB_MANAGER,      "daya123");
        userDAO.insert("Venkatesh",  Role.REGIONAL_MANAGER, "venkat123");
        userDAO.insert("Alice",      Role.DELIVERY_AGENT,   "alice123");
        System.out.println("Users seeded.");
    }

    private void seedIssues() {
        if (!issueDAO.isEmpty()) {
            return;
        }

        System.out.println("  Seeding package issues");

        issueDAO.insert("PKG1001", "Delayed Package",
                "Package delayed by one day", "LOW", 2000, 2);

        issueDAO.insert("PKG1002", "Damaged Package",
                "Package damaged during delivery", "MEDIUM", 15000, 2);

        issueDAO.insert("PKG1003", "Lost Package",
                "High-value package is missing", "HIGH", 50000, 3);

        issueDAO.insert("PKG1004", "Damaged Package",
                "Expensive package was damaged", "LOW", 40000, 3);

        System.out.println("  Issues seeded.");
    }
}