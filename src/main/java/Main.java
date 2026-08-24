import database.DataSeeder;
import database.DatabaseManager;
import database.PackageIssueDAO;
import database.UserDAO;
import entity.User;
import enums.IssueSeverity;
import enums.IssueType;
import enums.Role;
import factorypattern.ConcretePackageIssueFactory;
import factorypattern.PackageIssueFactory;
import proxy.AuthenticationService;
import proxy.AuthorizationException;
import chainofresponsibility.*;
import bridge.*;
import observer.PackageIssueManager;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class Main {

    private static final Scanner scanner = new Scanner(System.in);
    private static final AuthenticationService authService = new AuthenticationService();
    private static final UserDAO userDAO = new UserDAO();
    private static final PackageIssueDAO issueDAO = new PackageIssueDAO();
    private static final PackageIssueFactory issueFactory = new ConcretePackageIssueFactory();

    // Chain of Responsibility Setup
    private static final PackageIssueHandler supportAgent = new SupportAgent();
    private static final PackageIssueHandler hubManager = new HubManager();
    private static final PackageIssueHandler regionalManager = new RegionalManager();

    static {
        supportAgent.setNextHandler(hubManager);
        hubManager.setNextHandler(regionalManager);
    }

    public static void main(String[] args) {
        DatabaseManager.getInstance();
        new DataSeeder().seedAll();

        boolean running = true;
        while (running) {
            System.out.println("\n========================================================");
            System.out.println("   SMART PACKAGE EXCEPTION & RESOLUTION SYSTEM (SPER)   ");
            System.out.println("========================================================");
            System.out.println("1. Login to Portal");
            System.out.println("2. View Available Demo Accounts");
            System.out.println("0. Exit System");
            System.out.print("Select option: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    handleLogin();
                    break;
                case "2":
                    displayDemoAccounts();
                    break;
                case "0":
                    System.out.println("\nThank you for using SPER Management System. Goodbye!");
                    running = false;
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private static void displayDemoAccounts() {
        System.out.println("\n--- Available System Users ---");
        List<User> users = userDAO.findAll();
        for (User u : users) {
            System.out.printf("ID: %-2d | Name: %-12s | Role: %-16s | Password: %s\n",
                    u.getUserId(), u.getName(), u.getRole(), u.getPassword());
        }
    }

    private static void handleLogin() {
        System.out.println("\n--- User Authentication ---");
        System.out.print("Enter User ID: ");
        int userId;
        try {
            userId = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID format.");
            return;
        }

        System.out.print("Enter Password: ");
        String password = scanner.nextLine().trim();

        Optional<User> authenticatedUser = authService.authenticate(userId, password);
        if (authenticatedUser.isEmpty()) {
            System.out.println("Authentication Failed: Invalid User ID or Password.");
            return;
        }

        User user = authenticatedUser.get();
        System.out.println("\nLogin successful! Welcome, " + user.getName() + " [" + user.getRole() + "]");

        // Role-Based Dashboard Routing
        switch (user.getRole()) {
            case CUSTOMER:
                customerDashboard(user);
                break;
            case ADMIN:
                adminDashboard(user);
                break;
            case SUPPORT_STAFF:
            case HUB_MANAGER:
            case REGIONAL_MANAGER:
            case DELIVERY_AGENT:
                staffDashboard(user);
                break;
        }
    }

    // 1. CUSTOMER DASHBOARD
    private static void customerDashboard(User customer) {
        boolean active = true;
        while (active) {
            System.out.println("\n================ CUSTOMER PORTAL ================");
            System.out.println("Logged in as: " + customer.getName());
            System.out.println("1. Report New Package Issue (Factory + Bridge Notification)");
            System.out.println("2. View My Reported Issues");
            System.out.println("0. Logout");
            System.out.print("Select option: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    reportIssueWorkflow(customer);
                    break;
                case "2":
                    viewCustomerIssues(customer);
                    break;
                case "0":
                    active = false;
                    System.out.println("Logged out from Customer Portal.");
                    break;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    private static void reportIssueWorkflow(User customer) {
        System.out.println("\n--- Report Package Issue ---");
        System.out.print("Enter Tracking Number (e.g. PKG2001): ");
        String trackingNumber = scanner.nextLine().trim();

        System.out.println("Select Issue Type:");
        System.out.println("1. Lost Package");
        System.out.println("2. Damaged Package");
        System.out.println("3. Delayed Package");
        System.out.println("4. Wrong Address");
        System.out.println("5. Wrong Package");
        System.out.println("6. Recipient Unavailable");
        System.out.print("Choice: ");

        IssueType issueType = null;
        switch (scanner.nextLine().trim()) {
            case "1": issueType = IssueType.LOST; break;
            case "2": issueType = IssueType.DAMAGED; break;
            case "3": issueType = IssueType.DELAYED; break;
            case "4": issueType = IssueType.WRONG_ADDRESS; break;
            case "5": issueType = IssueType.WRONG_PACKAGE; break;
            case "6": issueType = IssueType.RECIPIENT_UNAVAILABLE; break;
            default:
                System.out.println("Invalid type selected.");
                return;
        }

        System.out.print("Enter Issue Description: ");
        String description = scanner.nextLine().trim();

        System.out.print("Enter Severity (LOW, MEDIUM, HIGH, CRITICAL): ");
        String severity = scanner.nextLine().trim().toUpperCase();

        System.out.print("Enter Estimated Package Value (₹): ");
        double value;
        try {
            value = Double.parseDouble(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            value = 1000.0;
        }

        // Factory Pattern execution
        factorypattern.PackageIssue factoryIssue = issueFactory.createIssue(issueType);
        factoryIssue.displayIssue();

        // Persist to Database
        int issueId = issueDAO.insert(trackingNumber, issueType.name(), description, severity, value, customer.getUserId());
        System.out.println("Issue #" + issueId + " registered in database.");

        // Bridge Pattern Notification
        System.out.println("\nSelect Preferred Notification Channel:");
        System.out.println("1. Email\n2. SMS\n3. Mobile App Push");
        System.out.print("Choice: ");
        DeliveryChannel channel = switch (scanner.nextLine().trim()) {
            case "2" -> new SMSChannel();
            case "3" -> new MobileAppChannel();
            default -> new EmailChannel();
        };

        Notification notification = new CreationNotification(channel);
        notification.notify(customer, trackingNumber);
    }

    private static void viewCustomerIssues(User customer) {
        System.out.println("\n--- My Reported Issues ---");
        List<chainofresponsibility.PackageIssue> issues = issueDAO.findByCustomerId(customer.getUserId());
        if (issues.isEmpty()) {
            System.out.println("No issues found.");
            return;
        }
        for (var issue : issues) {
            printIssueDetails(issue);
        }
    }

    // 2. ADMIN DASHBOARD
    private static void adminDashboard(User admin) {
        boolean active = true;
        while (active) {
            System.out.println("\n================ ADMIN CONTROL PANEL ================");
            System.out.println("Logged in as: " + admin.getName());
            System.out.println("1. View All Issues in System");
            System.out.println("2. Delete an Issue");
            System.out.println("3. View All Registered Users");
            System.out.println("4. Re-seed Database Defaults");
            System.out.println("0. Logout");
            System.out.print("Select option: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    viewAllIssues();
                    break;
                case "2":
                    System.out.print("Enter Issue ID to Delete: ");
                    try {
                        int id = Integer.parseInt(scanner.nextLine().trim());
                        if (issueDAO.delete(id)) {
                            System.out.println("Issue #" + id + " deleted successfully.");
                        } else {
                            System.out.println("Could not delete issue.");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid ID.");
                    }
                    break;
                case "3":
                    displayDemoAccounts();
                    break;
                case "4":
                    new DataSeeder().seedAll();
                    break;
                case "0":
                    active = false;
                    System.out.println("Logged out from Admin Panel.");
                    break;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    // 3. STAFF & MANAGER DASHBOARD (COR & Observer)
    private static void staffDashboard(User staff) {
        boolean active = true;
        while (active) {
            System.out.println("\n================ STAFF / RESOLUTION PORTAL ================");
            System.out.printf("Logged in as: %s | Role: %s\n", staff.getName(), staff.getRole());
            System.out.println("1. View All Issues");
            System.out.println("2. Process Issue via Chain of Responsibility (Escalate / Resolve)");
            System.out.println("3. Simulate Observer Status Update with Live Broadcast");
            System.out.println("0. Logout");
            System.out.print("Select option: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    viewAllIssues();
                    break;
                case "2":
                    processCORWorkflow();
                    break;
                case "3":
                    simulateObserverWorkflow(staff);
                    break;
                case "0":
                    active = false;
                    System.out.println("Logged out from Staff Portal.");
                    break;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    private static void viewAllIssues() {
        System.out.println("\n--- All System Package Issues ---");
        List<chainofresponsibility.PackageIssue> list = issueDAO.findAll();
        if (list.isEmpty()) {
            System.out.println("No issues found in system.");
            return;
        }
        for (var issue : list) {
            printIssueDetails(issue);
        }
    }

    private static void processCORWorkflow() {
        System.out.print("\nEnter Issue ID to Process: ");
        try {
            int id = Integer.parseInt(scanner.nextLine().trim());
            Optional<chainofresponsibility.PackageIssue> issueOpt = issueDAO.findById(id);
            if (issueOpt.isEmpty()) {
                System.out.println("Issue not found.");
                return;
            }

            var issue = issueOpt.get();
            System.out.println("\n[Chain of Responsibility] Passing issue to Support Agent...");
            supportAgent.handleIssue(issue);

            // Update database status
            issueDAO.updateStatus(issue.getIssueId(), issue.getStatus());
            System.out.println("Database updated. Final Status: " + issue.getStatus());
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID.");
        }
    }

    private static void simulateObserverWorkflow(User operator) {
        System.out.print("\nEnter Issue ID for Observer Live Broadcast: ");
        try {
            int id = Integer.parseInt(scanner.nextLine().trim());
            Optional<chainofresponsibility.PackageIssue> issueOpt = issueDAO.findById(id);
            if (issueOpt.isEmpty()) {
                System.out.println("Issue not found.");
                return;
            }

            var issue = issueOpt.get();
            PackageIssueManager issueManager = new PackageIssueManager(issue);

            // Attach default stakeholders as Observers
            userDAO.findAll().forEach(issueManager::attach);

            System.out.print("Enter New Status (e.g. UNDER_INVESTIGATION, ESCALATED, RESOLVED): ");
            String newStatus = scanner.nextLine().trim().toUpperCase();

            issueManager.updateStatus(newStatus);
            issueDAO.updateStatus(issue.getIssueId(), newStatus);
            issueDAO.recordHistory(issue.getIssueId(), issue.getStatus(), newStatus, operator.getUserId());
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID.");
        }
    }

    private static void printIssueDetails(chainofresponsibility.PackageIssue issue) {
        System.out.printf("[ID: #%d] Tracking: %s | Type: %-15s | Severity: %-7s | Value: ₹%-7.2f | Status: %s\n",
                issue.getIssueId(), issue.getTrackingNumber(), issue.getIssueType(),
                issue.getSeverity(), issue.getPackageValue(), issue.getStatus());
    }
}