import database.DataSeeder;
import database.DatabaseManager;
import database.PackageIssueDAO;
import database.UserDAO;
import entity.User;
import enums.IssueSeverity;
import enums.IssueType;
import enums.Role;
import proxy.AuthenticationService;
import proxy.AuthorizationException;
import proxy.IssueService;
import proxy.IssueServiceProxy;
import chainofresponsibility.*;
import bridge.*;
import observer.PackageIssueManager;
import templatemethod.DamagedPackageResolution;
import templatemethod.DelayedPackageResolution;
import templatemethod.LostPackageResolution;
import templatemethod.PackageIssueResolutionTemplate;
import templatemethod.RecipientUnavailableResolution;
import templatemethod.WrongAddressPackageResolution;
import templatemethod.WrongPackageResolution;
import memento.IssueHistoryCaretaker;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class Main {

    private static final Scanner scanner = new Scanner(System.in);
    private static final AuthenticationService authService = new AuthenticationService();
    private static final UserDAO userDAO = new UserDAO();
    private static final PackageIssueDAO issueDAO = new PackageIssueDAO();

    private static final IssueService issueService = new IssueServiceProxy(authService);

    private static final IssueHistoryCaretaker historyCaretaker = new IssueHistoryCaretaker();

    private static final PackageIssueHandler supportAgent = new SupportAgent();
    private static final PackageIssueHandler hubManager = new HubManager();
    private static final PackageIssueHandler regionalManager = new RegionalManager();

    static {
        supportAgent.setNextHandler(hubManager);
        hubManager.setNextHandler(regionalManager);
    }

    public static void main(String[] args) {
        // Initialize SQLite DB & Seed default data
        DatabaseManager.getInstance();
        new DataSeeder().seedAll();

        boolean running = true;
        while (running) {
            System.out.println("\n========================================================");
            System.out.println("   SMART PACKAGE EXCEPTION & RESOLUTION SYSTEM (SPER)   ");
            System.out.println("========================================================");
            System.out.println("1. Login to Portal");
            System.out.println("2. View Demo User Credentials");
            System.out.println("0. Exit");
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
                    System.out.println("\nExiting SPER Management System. Goodbye!");
                    running = false;
                    break;
                default:
                    System.out.println("[ERROR] Invalid choice. Please try again.");
            }
        }
    }

    private static void displayDemoAccounts() {
        System.out.println("\n--- Available Users in Database ---");
        List<User> users = userDAO.findAll();
        for (User u : users) {
            System.out.printf("User ID: %-2d | Name: %-12s | Role: %-16s | Password: %s\n",
                    u.getUserId(), u.getName(), u.getRole(), u.getPassword());
        }
    }

    private static void handleLogin() {
        System.out.println("\n--- User Login ---");
        System.out.print("Enter User ID: ");
        int userId;
        try {
            userId = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("[ERROR] Invalid ID format.");
            return;
        }

        System.out.print("Enter Password: ");
        String password = scanner.nextLine().trim();

        // Authenticate via AuthenticationService
        Optional<User> authenticatedUser = authService.authenticate(userId, password);
        if (authenticatedUser.isEmpty()) {
            System.out.println("[ERROR] Authentication Failed: Invalid credentials.");
            return;
        }

        User user = authenticatedUser.get();
        System.out.println("\n[SUCCESS] Welcome, " + user.getName() + " [" + user.getRole() + "]");

        // Route to respective role view
        if (user.getRole() == Role.CUSTOMER) {
            customerDashboard(user);
        } else if (user.getRole() == Role.ADMIN) {
            adminDashboard(user);
        } else {
            staffDashboard(user);
        }
    }

    // ==========================================
    // 1. CUSTOMER WORKFLOW
    // ==========================================
    private static void customerDashboard(User customer) {
        boolean active = true;
        while (active) {
            System.out.println("\n================ CUSTOMER PORTAL ================");
            System.out.println("User: " + customer.getName());
            System.out.println("1. Report New Package Issue");
            System.out.println("2. View My Reported Issues");
            System.out.println("0. Logout");
            System.out.print("Choice: ");

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
                    System.out.println("[ERROR] Invalid choice.");
            }
        }
    }

    private static void reportIssueWorkflow(User customer) {
        System.out.println("\n--- Report Package Issue ---");

        // 1. Validate Tracking Number Format (Starts with PKG + digits)
        String trackingNumber;
        while (true) {
            System.out.print("Enter Tracking Number (e.g. PKG1001): ");
            trackingNumber = scanner.nextLine().trim().toUpperCase();

            if (PackageIssueBuilder.isValidTrackingNumber(trackingNumber)) {
                break;
            }
            System.out.println("[ERROR] Invalid format. Must start with 'PKG' followed by 4 to 8 digits.");
        }

        // 2. Select Issue Type
        System.out.println("Select Issue Type:");
        System.out.println("1. Lost Package");
        System.out.println("2. Damaged Package");
        System.out.println("3. Delayed Package");
        System.out.println("4. Wrong Address");
        System.out.println("5. Wrong Package");
        System.out.println("6. Recipient Unavailable");
        System.out.print("Choice: ");

        IssueType issueType = switch (scanner.nextLine().trim()) {
            case "1" -> IssueType.LOST;
            case "2" -> IssueType.DAMAGED;
            case "3" -> IssueType.DELAYED;
            case "4" -> IssueType.WRONG_ADDRESS;
            case "5" -> IssueType.WRONG_PACKAGE;
            case "6" -> IssueType.RECIPIENT_UNAVAILABLE;
            default -> null;
        };

        if (issueType == null) {
            System.out.println("[ERROR] Invalid selection.");
            return;
        }

        System.out.print("Enter Issue Description: ");
        String description = scanner.nextLine().trim();

        System.out.print("Enter Package Value: ");
        double value;
        try {
            value = Double.parseDouble(scanner.nextLine().trim());
            if (value < 0) value = 0.0;
        } catch (NumberFormatException e) {
            value = 1000.0;
        }

        // 3. Determine Severity via Chain of Responsibility rules
        IssueSeverity severity = determineSeverityViaCOR(issueType, value);
        System.out.println("[INFO] Evaluated Severity Level: " + severity);

        // 3b. Builder Pattern: assemble and validate the issue before it is saved
        chainofresponsibility.PackageIssue issue;
        try {
            issue = chainofresponsibility.PackageIssue.builder()
                    .trackingNumber(trackingNumber)
                    .issueType(issueType)
                    .description(description)
                    .severity(severity)
                    .packageValue(value)
                    .customerId(customer.getUserId())
                    .build();
        } catch (IllegalStateException e) {
            System.out.println("[ERROR] Cannot register issue: " + e.getMessage());
            return;
        }

        // 4. Proxy Layer Interception: Checks permissions & delegates to Factory
        try {
            issueService.reportIssue(customer, customer.getUserId(), issueType);

            // 5. Persist to Database
            int issueId = issueDAO.insert(issue);
            if (issueId < 0) {
                System.out.println("[ERROR] Failed to save the issue in the database.");
                return;
            }
            System.out.println("[SUCCESS] Issue #" + issueId + " registered in database.");

            // 6. Bridge Pattern Notification Dispatch
            System.out.println("\nSelect Notification Channel: 1. Email | 2. SMS | 3. Mobile App Push");
            System.out.print("Choice: ");
            DeliveryChannel channel = switch (scanner.nextLine().trim()) {
                case "2" -> new SMSChannel();
                case "3" -> new MobileAppChannel();
                default -> new EmailChannel();
            };

            Notification notification = new CreationNotification(channel);
            notification.notify(customer, issue.getTrackingNumber());

        } catch (AuthorizationException e) {
            System.out.println("[ACCESS DENIED] " + e.getMessage());
        }
    }

    private static IssueSeverity determineSeverityViaCOR(IssueType type, double packageValue) {
        if (packageValue > 25000 || type == IssueType.LOST) {
            return IssueSeverity.HIGH;
        } else if (packageValue > 5000 || type == IssueType.DAMAGED || type == IssueType.WRONG_ADDRESS) {
            return IssueSeverity.MEDIUM;
        } else {
            return IssueSeverity.LOW;
        }
    }

    private static void viewCustomerIssues(User customer) {
        System.out.println("\n--- My Reported Issues ---");
        try {
            issueService.viewIssue(customer, customer.getUserId(), null);

            List<chainofresponsibility.PackageIssue> list = issueDAO.findByCustomerId(customer.getUserId());
            if (list.isEmpty()) {
                System.out.println("No issues found.");
                return;
            }
            for (var issue : list) {
                printIssueDetails(issue);
            }
        } catch (AuthorizationException e) {
            System.out.println("[ACCESS DENIED] " + e.getMessage());
        }
    }

    // ==========================================
    // 2. ADMIN WORKFLOW
    // ==========================================
    private static void adminDashboard(User admin) {
        boolean active = true;
        while (active) {
            System.out.println("\n================ ADMIN CONTROL PANEL ================");
            System.out.println("Admin: " + admin.getName());
            System.out.println("1. View All Issues");
            System.out.println("2. Delete an Issue (Proxy Guarded)");
            System.out.println("3. View All Registered Users");
            System.out.println("4. Re-seed Database Defaults");
            System.out.println("0. Logout");
            System.out.print("Choice: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    viewAllIssues();
                    break;
                case "2":
                    handleDeleteIssue(admin);
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
                    System.out.println("[ERROR] Invalid choice.");
            }
        }
    }

    private static void handleDeleteIssue(User admin) {
        System.out.print("Enter Issue ID to Delete: ");
        try {
            int id = Integer.parseInt(scanner.nextLine().trim());

            // Proxy Layer checks admin authorization
            issueService.deleteIssue(admin, null);

            if (issueDAO.delete(id)) {
                System.out.println("[SUCCESS] Issue #" + id + " deleted.");
            } else {
                System.out.println("[ERROR] Issue not found.");
            }
        } catch (AuthorizationException e) {
            System.out.println("[ACCESS DENIED] " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("[ERROR] Invalid ID format.");
        }
    }

    // ==========================================
    // 3. STAFF / MANAGER WORKFLOW
    // ==========================================
    private static void staffDashboard(User staff) {
        boolean active = true;
        while (active) {
            System.out.println("\n================ STAFF PORTAL ================");
            System.out.printf("User: %s | Role: %s\n", staff.getName(), staff.getRole());
            System.out.println("1. View All Issues");
            System.out.println("2. Process Issue via Chain of Responsibility");
            System.out.println("3. Resolve Issue (Proxy Guarded)");
            System.out.println("4. Observer Broadcast Status Update");
            System.out.println("5. Undo Last Status Change (Memento)");
            System.out.println("0. Logout");
            System.out.print("Choice: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    viewAllIssues();
                    break;
                case "2":
                    processCORWorkflow();
                    break;
                case "3":
                    handleResolveIssue(staff);
                    break;
                case "4":
                    simulateObserverWorkflow(staff);
                    break;
                case "5":
                    handleUndoStatusChange(staff);
                    break;
                case "0":
                    active = false;
                    System.out.println("Logged out from Staff Portal.");
                    break;
                default:
                    System.out.println("[ERROR] Invalid choice.");
            }
        }
    }

    private static void handleResolveIssue(User staff) {
        System.out.print("Enter Issue ID to Resolve: ");
        try {
            int id = Integer.parseInt(scanner.nextLine().trim());
            Optional<chainofresponsibility.PackageIssue> issueOpt = issueDAO.findById(id);
            if (issueOpt.isEmpty()) {
                System.out.println("[ERROR] Issue not found.");
                return;
            }

            chainofresponsibility.PackageIssue issue = issueOpt.get();

            // Normalize enum names and older labels such as "Lost Package" or "Wrong Address".
            String issueType = issue.getIssueType().trim().toUpperCase().replaceAll("\\s+", "_");

            PackageIssueResolutionTemplate resolutionTemplate = switch (issueType) {
                case "LOST", "LOST_PACKAGE" -> new LostPackageResolution();
                case "DAMAGED", "DAMAGED_PACKAGE" -> new DamagedPackageResolution();
                case "DELAYED", "DELAYED_PACKAGE" -> new DelayedPackageResolution();
                case "WRONG_ADDRESS", "WRONG_ADDRESS_PACKAGE" -> new WrongAddressPackageResolution();
                case "WRONG_PACKAGE" -> new WrongPackageResolution();
                case "RECIPIENT_UNAVAILABLE", "RECIPIENT_UNAVAILABLE_PACKAGE" ->
                        new RecipientUnavailableResolution();
                default -> null;
            };

            if (resolutionTemplate == null) {
                System.out.println("[ERROR] No Template Method resolution is configured for issue type: "
                        + issue.getIssueType());
                return;
            }

            // Proxy Layer checks authentication and role before resolution.
            issueService.resolveIssue(staff, issue);

            // Memento: snapshot before the status changes
            String oldStatus = issue.getStatus();
            historyCaretaker.save(issue);

            // Template Method controls the fixed resolution workflow.
            if (resolutionTemplate.resolve(issue)) {
                issueDAO.recordHistory(id, oldStatus, "RESOLVED", staff.getUserId());
            } else {
                historyCaretaker.discardLast(id);
            }
        } catch (AuthorizationException e) {
            System.out.println("[ACCESS DENIED] " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("[ERROR] Invalid ID format.");
        }
    }

    private static void viewAllIssues() {
        System.out.println("\n--- All System Issues ---");
        List<chainofresponsibility.PackageIssue> list = issueDAO.findAll();
        if (list.isEmpty()) {
            System.out.println("No issues found.");
            return;
        }
        for (var issue : list) {
            printIssueDetails(issue);
        }
    }

    private static void processCORWorkflow() {
        System.out.print("Enter Issue ID to Process via COR: ");
        try {
            int id = Integer.parseInt(scanner.nextLine().trim());
            Optional<chainofresponsibility.PackageIssue> issueOpt = issueDAO.findById(id);
            if (issueOpt.isEmpty()) {
                System.out.println("[ERROR] Issue not found.");
                return;
            }

            var issue = issueOpt.get();
            historyCaretaker.save(issue);
            System.out.println("\n[Chain of Responsibility] Passing issue to Support Agent...");
            supportAgent.handleIssue(issue);

            // Update database status
            if (!issueDAO.updateStatus(issue.getIssueId(), issue.getStatus())) {
                historyCaretaker.discardLast(id);
                System.out.println("[ERROR] Failed to update status in database.");
                return;
            }
            System.out.println("[SUCCESS] Database updated. Final Status: " + issue.getStatus());
        } catch (NumberFormatException e) {
            System.out.println("[ERROR] Invalid ID format.");
        }
    }

    private static void simulateObserverWorkflow(User operator) {
        System.out.print("Enter Issue ID for Live Observer Broadcast: ");
        try {
            int id = Integer.parseInt(scanner.nextLine().trim());
            Optional<chainofresponsibility.PackageIssue> issueOpt = issueDAO.findById(id);
            if (issueOpt.isEmpty()) {
                System.out.println("[ERROR] Issue not found.");
                return;
            }

            var issue = issueOpt.get();
            PackageIssueManager issueManager = new PackageIssueManager(issue);

            // Attach all users as observers
            userDAO.findAll().forEach(issueManager::attach);

            System.out.print("Enter New Status (e.g. UNDER_INVESTIGATION, ESCALATED, RESOLVED): ");
            String newStatus = scanner.nextLine().trim().toUpperCase();

            String oldStatus = issue.getStatus();
            historyCaretaker.save(issue);

            issueManager.updateStatus(newStatus);
            if (!issueDAO.updateStatus(issue.getIssueId(), newStatus)) {
                historyCaretaker.discardLast(id);
                System.out.println("[ERROR] Failed to update status in database.");
                return;
            }
            issueDAO.recordHistory(issue.getIssueId(), oldStatus, newStatus, operator.getUserId());
        } catch (NumberFormatException e) {
            System.out.println("[ERROR] Invalid ID format.");
        }
    }

    private static void handleUndoStatusChange(User operator) {
        System.out.print("Enter Issue ID to Undo: ");
        try {
            int id = Integer.parseInt(scanner.nextLine().trim());
            Optional<chainofresponsibility.PackageIssue> issueOpt = issueDAO.findById(id);
            if (issueOpt.isEmpty()) {
                System.out.println("[ERROR] Issue not found.");
                return;
            }

            Optional<chainofresponsibility.PackageIssue.Memento> snapshot = historyCaretaker.peekLast(id);
            if (snapshot.isEmpty()) {
                System.out.println("[INFO] No status changes to undo for Issue #" + id + " in this session.");
                return;
            }

            // Memento: restore the issue from its last snapshot and persist it
            var issue = issueOpt.get();
            String currentStatus = issue.getStatus();
            issue.restore(snapshot.get());

            if (issueDAO.restoreStatus(id, issue.getStatus())) {
                historyCaretaker.discardLast(id);
                issueDAO.recordHistory(id, currentStatus, issue.getStatus(), operator.getUserId());
                System.out.println("[SUCCESS] Issue #" + id + " status: [" + currentStatus + "] → ["
                        + issue.getStatus() + "] (snapshot from " + snapshot.get().getSavedAt() + ")");
                System.out.println("[INFO] Remaining undo steps: " + historyCaretaker.getHistorySize(id));
            } else {
                System.out.println("[ERROR] Failed to restore status in database.");
            }
        } catch (NumberFormatException e) {
            System.out.println("[ERROR] Invalid ID format.");
        }
    }

    private static void printIssueDetails(chainofresponsibility.PackageIssue issue) {
        System.out.printf("[ID: #%d] Tracking: %s | Type: %-15s | Severity: %-7s | Value: INR %-7.2f | Status: %s\n",
                issue.getIssueId(), issue.getTrackingNumber(), issue.getIssueType(),
                issue.getSeverity(), issue.getPackageValue(), issue.getStatus());
    }
}