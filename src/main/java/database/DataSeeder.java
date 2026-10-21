package database;

import entity.User;
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
        seedUser("admin",      Role.ADMIN,            "admin123");
        seedUser("Ramesh",     Role.CUSTOMER,         "ramesh123");
        seedUser("Rahul",      Role.CUSTOMER,         "rahul123");
        seedUser("Sunderlal",  Role.SUPPORT_STAFF,    "sunder123");
        seedUser("Daya",       Role.HUB_MANAGER,      "daya123");
        seedUser("Venkatesh",  Role.REGIONAL_MANAGER, "venkat123");
        seedUser("Alice",      Role.DELIVERY_AGENT,   "alice123");
        System.out.println("Users seeded.");
    }

    private void seedUser(String name, Role role, String password) {
        try {
            User user = User.builder()
                    .name(name)
                    .role(role)
                    .password(password)
                    .build();
            userDAO.insert(user);
        } catch (IllegalStateException e) {
            System.err.println("Skipping invalid seed user '" + name + "': " + e.getMessage());
        }
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