import bridge.*;
import chainofresponsibility.*;
import database.DataSeeder;
import database.DatabaseManager;
import database.PackageIssueDAO;
import database.UserDAO;
import entity.User;
import enums.IssueSeverity;
import enums.IssueType;
import enums.Role;
import memento.IssueHistoryCaretaker;
import observer.PackageIssueManager;
import prototype.PrototypeIssueWorkflow;
import proxy.AuthenticationService;
import proxy.AuthorizationException;
import proxy.IssueService;
import proxy.IssueServiceProxy;
import strategy.ResolutionEstimate;
import strategy.ResolutionEstimator;
import templatemethod.*;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class Main {

    private static final Scanner scanner = new Scanner(System.in);

    private static final AuthenticationService authService =
            new AuthenticationService();

    private static final UserDAO userDAO = new UserDAO();
    private static final PackageIssueDAO issueDAO = new PackageIssueDAO();

    private static final IssueService issueService =
            new IssueServiceProxy(authService);

    private static final IssueHistoryCaretaker historyCaretaker =
            new IssueHistoryCaretaker();

    private static final ResolutionEstimator resolutionEstimator =
            new ResolutionEstimator();

    private static final PrototypeIssueWorkflow prototypeWorkflow =
            new PrototypeIssueWorkflow(
                    scanner,
                    issueDAO,
                    issueService,
                    Main::determineSeverityViaCOR
            );

    private static final PackageIssueHandler supportAgent =
            new SupportAgent();

    private static final PackageIssueHandler hubManager =
            new HubManager();

    private static final PackageIssueHandler regionalManager =
            new RegionalManager();

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
            System.out.println("2. View Demo User Credentials");
            System.out.println("0. Exit");
            System.out.print("Select option: ");

            switch (scanner.nextLine().trim()) {

                case "1" -> handleLogin();

                case "2" -> displayDemoAccounts();

                case "0" -> {
                    System.out.println(
                            "Exiting SPER Management System. Goodbye!"
                    );
                    running = false;
                }

                default ->
                        System.out.println("[ERROR] Invalid choice.");
            }
        }
    }

    // ---------------- DEMO ACCOUNTS ----------------

    private static void displayDemoAccounts() {

        System.out.println("\n--- Available Users in Database ---");

        for (User user : userDAO.findAll()) {

            System.out.printf(
                    "User ID: %-3d | Name: %-12s | Role: %-18s | Password: %s%n",
                    user.getUserId(),
                    user.getName(),
                    user.getRole(),
                    user.getPassword()
            );
        }
    }

    // ---------------- LOGIN ----------------

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

        Optional<User> found =
                authService.authenticate(userId, password);

        if (found.isEmpty()) {
            System.out.println("[ERROR] Authentication failed.");
            return;
        }

        User user = found.get();

        System.out.println(
                "[SUCCESS] Welcome, " + user.getName()
                        + " [" + user.getRole() + "]"
        );

        if (user.getRole() == Role.CUSTOMER) {
            customerDashboard(user);
        } else if (user.getRole() == Role.ADMIN) {
            adminDashboard(user);
        } else {
            staffDashboard(user);
        }
    }

    // =====================================================
    // CUSTOMER PORTAL
    // =====================================================

    private static void customerDashboard(User customer) {

        boolean active = true;

        while (active) {

            System.out.println("\n================ CUSTOMER PORTAL ================");
            System.out.println("User: " + customer.getName());
            System.out.println("1. Report New Package Issue");
            System.out.println("2. View My Reported Issues");
            System.out.println("3. Report Issue from Template");
            System.out.println("4. Duplicate Existing Issue");
            System.out.println("0. Logout");
            System.out.print("Choice: ");

            switch (scanner.nextLine().trim()) {

                case "1" ->
                        reportIssueWorkflow(customer);

                case "2" ->
                        viewCustomerIssues(customer);

                case "3" ->
                        prototypeWorkflow.reportFromTemplate(customer);

                case "4" ->
                        prototypeWorkflow.duplicateExistingIssue(customer);

                case "0" -> {
                    active = false;
                    System.out.println(
                            "Logged out from Customer Portal."
                    );
                }

                default ->
                        System.out.println("[ERROR] Invalid choice.");
            }
        }
    }

    // ---------------- REPORT ISSUE ----------------

    private static void reportIssueWorkflow(User customer) {

        System.out.println("\n--- Report Package Issue ---");

        String trackingNumber;

        while (true) {

            System.out.print(
                    "Enter Tracking Number (PKG + 4 to 8 digits): "
            );

            trackingNumber =
                    scanner.nextLine().trim().toUpperCase();

            if (!PackageIssueBuilder.isValidTrackingNumber(
                    trackingNumber)) {

                System.out.println(
                        "[ERROR] Invalid tracking number. Example: PKG1234."
                );
                continue;
            }

            if (trackingNumberExists(trackingNumber)) {

                System.out.println(
                        "[ERROR] This tracking number is already registered. "
                                + "Enter a new one."
                );
                continue;
            }

            break;
        }

        IssueType issueType = promptIssueType();

        if (issueType == null) {
            return;
        }

        System.out.print("Enter Issue Description: ");
        String description = scanner.nextLine().trim();

        if (description.isBlank()) {
            System.out.println(
                    "[ERROR] Description cannot be empty."
            );
            return;
        }

        double value;

        try {

            System.out.print("Enter Package Value (INR): ");

            value = Double.parseDouble(
                    scanner.nextLine().trim()
            );

            if (!Double.isFinite(value) || value < 0) {
                System.out.println(
                        "[ERROR] Package value must be a non-negative number."
                );
                return;
            }

        } catch (NumberFormatException e) {

            System.out.println(
                    "[ERROR] Enter a valid numeric package value."
            );
            return;
        }

        IssueSeverity severity =
                determineSeverityViaCOR(issueType, value);

        System.out.println(
                "[INFO] Evaluated Severity Level: " + severity
        );

        PackageIssue issue;

        try {

            issue = PackageIssue.builder()
                    .trackingNumber(trackingNumber)
                    .issueType(issueType)
                    .description(description)
                    .severity(severity)
                    .packageValue(value)
                    .customerId(customer.getUserId())
                    .build();

        } catch (IllegalStateException e) {

            System.out.println(
                    "[ERROR] Cannot register issue: " + e.getMessage()
            );
            return;
        }

        try {

            issueService.reportIssue(
                    customer,
                    customer.getUserId(),
                    issueType
            );

            int issueId = issueDAO.insert(issue);

            if (issueId < 0) {
                System.out.println(
                        "[ERROR] Failed to save issue to database."
                );
                return;
            }

            System.out.println(
                    "[SUCCESS] Issue #" + issueId
                            + " registered in database."
            );

            printEstimate(issue);
            sendCreationNotification(customer, issue);

        } catch (AuthorizationException e) {

            System.out.println(
                    "[ACCESS DENIED] " + e.getMessage()
            );
        }
    }

    private static boolean trackingNumberExists(
            String trackingNumber) {

        return issueDAO.findAll()
                .stream()
                .anyMatch(issue ->
                        trackingNumber.equalsIgnoreCase(
                                issue.getTrackingNumber()
                        )
                );
    }

    // ---------------- ISSUE TYPE ----------------

    private static IssueType promptIssueType() {

        System.out.println("Select Issue Type:");
        System.out.println("1. Lost Package");
        System.out.println("2. Damaged Package");
        System.out.println("3. Delayed Package");
        System.out.println("4. Wrong Address");
        System.out.println("5. Wrong Package");
        System.out.println("6. Recipient Unavailable");
        System.out.print("Choice: ");

        return switch (scanner.nextLine().trim()) {

            case "1" -> IssueType.LOST;
            case "2" -> IssueType.DAMAGED;
            case "3" -> IssueType.DELAYED;
            case "4" -> IssueType.WRONG_ADDRESS;
            case "5" -> IssueType.WRONG_PACKAGE;
            case "6" -> IssueType.RECIPIENT_UNAVAILABLE;

            default -> {
                System.out.println(
                        "[ERROR] Invalid issue type."
                );
                yield null;
            }
        };
    }

    // ---------------- SEVERITY ----------------

    private static IssueSeverity determineSeverityViaCOR(
            IssueType type,
            double value) {

        if (value > 25000 || type == IssueType.LOST) {
            return IssueSeverity.HIGH;
        }

        if (value > 5000
                || type == IssueType.DAMAGED
                || type == IssueType.WRONG_ADDRESS) {

            return IssueSeverity.MEDIUM;
        }

        return IssueSeverity.LOW;
    }

    // ---------------- RESOLUTION ESTIMATE ----------------

    private static void printEstimate(PackageIssue issue) {

        try {

            ResolutionEstimate estimate =
                    resolutionEstimator.estimate(issue);

            System.out.println(
                    "\n--- Automatic Resolution Estimate ---"
            );

            System.out.println(
                    "Selected Resolution Method: "
                            + resolutionEstimator
                            .getSelectedStrategyName(issue)
            );

            System.out.println(
                    "Estimated Resolution Time: "
                            + estimate.getEstimatedDays()
                            + " day(s)"
            );

            System.out.printf(
                    "Estimated Customer Refund: INR %.2f%n",
                    estimate.getEstimatedRefund()
            );

            System.out.println(
                    "Reason: " + estimate.getReason()
            );

            System.out.println(
                    "Note: This is an illustrative estimate, "
                            + "not a guaranteed refund."
            );

        } catch (IllegalArgumentException e) {

            System.out.println(
                    "[WARN] Could not estimate this issue: "
                            + e.getMessage()
            );
        }
    }

    // ---------------- NOTIFICATIONS ----------------

    private static void sendCreationNotification(
            User customer,
            PackageIssue issue) {

        DeliveryChannel channel = promptChannel();

        new CreationNotification(channel)
                .notify(customer, issue.getTrackingNumber());
    }

    private static DeliveryChannel promptChannel() {

        while (true) {

            System.out.println(
                    "\nSelect Notification Channel: "
                            + "1. Email | 2. SMS | 3. Mobile App Push"
            );

            System.out.print("Choice: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {

                case "1":
                    return new EmailChannel();

                case "2":
                    return new SMSChannel();

                case "3":
                    return new MobileAppChannel();

                default:
                    System.out.println(
                            "[ERROR] Invalid choice. "
                                    + "Please enter 1, 2, or 3."
                    );
            }
        }
    }

    // ---------------- CUSTOMER ISSUE HISTORY ----------------

    private static void viewCustomerIssues(User customer) {

        System.out.println("\n--- My Reported Issues ---");

        try {

            issueService.viewIssue(
                    customer,
                    customer.getUserId(),
                    null
            );

            List<PackageIssue> issues =
                    issueDAO.findByCustomerId(customer.getUserId());

            if (issues.isEmpty()) {
                System.out.println("No issues found.");
                return;
            }

            issues.forEach(Main::printIssueDetails);

        } catch (AuthorizationException e) {

            System.out.println(
                    "[ACCESS DENIED] " + e.getMessage()
            );
        }
    }

    // =====================================================
    // ADMIN PORTAL
    // =====================================================

    private static void adminDashboard(User admin) {

        boolean active = true;

        while (active) {

            System.out.println(
                    "\n================ ADMIN CONTROL PANEL ================"
            );

            System.out.println("Admin: " + admin.getName());
            System.out.println("1. View All Issues");
            System.out.println("2. Delete an Issue");
            System.out.println("3. View All Registered Users");
            System.out.println("4. Re-seed Database Defaults");
            System.out.println("0. Logout");
            System.out.print("Choice: ");

            switch (scanner.nextLine().trim()) {

                case "1" ->
                        viewAllIssues();

                case "2" ->
                        handleDeleteIssue(admin);

                case "3" ->
                        displayDemoAccounts();

                case "4" ->
                        new DataSeeder().seedAll();

                case "0" ->
                        active = false;

                default ->
                        System.out.println("[ERROR] Invalid choice.");
            }
        }
    }

    // ---------------- DELETE ISSUE ----------------

    private static void handleDeleteIssue(User admin) {

        System.out.print("Enter Issue ID to Delete: ");

        try {

            int id = Integer.parseInt(
                    scanner.nextLine().trim()
            );

            issueService.deleteIssue(admin, null);

            if (issueDAO.delete(id)) {

                System.out.println(
                        "[SUCCESS] Issue #" + id + " deleted."
                );

            } else {

                System.out.println(
                        "[ERROR] Issue not found."
                );
            }

        } catch (AuthorizationException e) {

            System.out.println(
                    "[ACCESS DENIED] " + e.getMessage()
            );

        } catch (NumberFormatException e) {

            System.out.println("[ERROR] Invalid ID.");
        }
    }

    // =====================================================
    // STAFF / MANAGER PORTAL
    // =====================================================

    private static void staffDashboard(User staff) {

        boolean active = true;

        while (active) {

            System.out.println(
                    "\n================ STAFF PORTAL ================"
            );

            System.out.printf(
                    "User: %s | Role: %s%n",
                    staff.getName(),
                    staff.getRole()
            );

            System.out.println(
                    "1. View Issues Available "
            );

            System.out.println(
                    "2. Process and Resolve Issue"
            );

            System.out.println(
                    "3. Manually Update Issue Status"
            );

            System.out.println("0. Logout");
            System.out.print("Choice: ");

            switch (scanner.nextLine().trim()) {

                case "1" ->
                        viewIssuesForRole(staff);

                case "2" ->
                        processAndResolveIssue(staff);

                case "3" ->
                        manuallyUpdateIssueStatus(staff);

                case "0" ->
                        active = false;

                default ->
                        System.out.println("[ERROR] Invalid choice.");
            }
        }
    }

    // ---------------- MANUAL STATUS UPDATE ----------------

    private static void manuallyUpdateIssueStatus(User operator) {

        System.out.println(
                "\n========== MANUAL ISSUE STATUS UPDATE =========="
        );

        PackageIssue issue =
                promptIssueById("Enter Issue ID to Update: ");

        if (issue == null) {
            return;
        }

        String oldStatus = issue.getStatus();

        System.out.println("\nIssue ID: #" + issue.getIssueId());
        System.out.println(
                "Tracking Number: " + issue.getTrackingNumber()
        );
        System.out.println("Current Status: " + oldStatus);

        System.out.println("\nChoose the new status:");
        System.out.println("1. REPORTED");
        System.out.println("2. IN_PROGRESS");
        System.out.println("3. RESOLVED");
        System.out.println("4. ESCALATED");
        System.out.println("0. Cancel");
        System.out.print("Enter choice: ");

        String choice = scanner.nextLine().trim();

        if ("0".equals(choice)) {
            System.out.println(
                    "[INFO] Status update cancelled."
            );
            return;
        }

        String newStatus = switch (choice) {

            case "1" -> "REPORTED";
            case "2" -> "IN_PROGRESS";
            case "3" -> "RESOLVED";
            case "4" -> "ESCALATED";

            default -> null;
        };

        if (newStatus == null) {

            System.out.println(
                    "[ERROR] Invalid status choice."
            );
            return;
        }

        if (newStatus.equalsIgnoreCase(oldStatus)) {

            System.out.println(
                    "[INFO] The issue already has status "
                            + newStatus + "."
            );
            return;
        }

        /*
         * Apply the existing service authorization rule before
         * updating the issue. Confirm that your proxy permits this
         * operation for the intended staff roles.
         */
        try {

            issueService.resolveIssue(operator, issue);

        } catch (AuthorizationException e) {

            System.out.println(
                    "[ACCESS DENIED] " + e.getMessage()
            );
            return;
        }

        // Save the previous state for the existing Memento workflow.
        historyCaretaker.save(issue);

        boolean updated = issueDAO.updateStatus(
                issue.getIssueId(),
                newStatus
        );

        if (!updated) {

            historyCaretaker.discardLast(issue.getIssueId());

            System.out.println(
                    "[ERROR] Could not update the issue status "
                            + "in the database."
            );

            return;
        }

        // Update the in-memory object after the database succeeds.
        issue.setStatus(newStatus);

        // Record the old and new status in the issue history.
        issueDAO.recordHistory(
                issue.getIssueId(),
                oldStatus,
                newStatus,
                operator.getUserId()
        );

        System.out.println(
                "\n[SUCCESS] Status updated successfully!"
        );

        System.out.println(
                "Issue ID: #" + issue.getIssueId()
        );

        System.out.println(
                "Previous Status: " + oldStatus
        );

        System.out.println(
                "New Status: " + newStatus
        );

        // Notify observers about the updated status.
        broadcastStatus(issue);

        // A manually selected RESOLVED status does not run the
        // full resolution workflow or send the resolution notification.
        if ("RESOLVED".equalsIgnoreCase(newStatus)) {
            System.out.println(
                    "[INFO] Status changed manually. "
                            + "The resolution workflow was not executed."
            );
        }
    }

    // ---------------- AUTOMATIC PROCESS AND RESOLVE ----------------

    private static void processAndResolveIssue(User operator) {

        PackageIssue issue =
                promptIssueById("Enter Issue ID to Process and Resolve: ");

        if (issue == null) {
            return;
        }

        if ("RESOLVED".equalsIgnoreCase(issue.getStatus())) {

            System.out.println(
                    "[INFO] This issue is already resolved."
            );

            return;
        }

        String oldStatus = issue.getStatus();

        historyCaretaker.save(issue);

        System.out.println(
                "[COR] Routing issue to the responsible handler..."
        );

        supportAgent.handleIssue(issue);

        if (issue.getHandledBy() == null
                || issue.getHandledBy().isBlank()) {

            historyCaretaker.discardLast(issue.getIssueId());

            System.out.println(
                    "[ERROR] No responsible handler was assigned."
            );

            return;
        }

        System.out.println(
                "\n[COR COMPLETE] Responsible handler: "
                        + issue.getHandledBy() + "."
        );

        System.out.println(
                "[HANDOFF] Passing issue to the resolution workflow."
        );

        String normalized = issue.getIssueType()
                .trim()
                .toUpperCase()
                .replaceAll("\\s+", "_");

        PackageIssueResolutionTemplate template = switch (normalized) {

            case "LOST", "LOST_PACKAGE" ->
                    new LostPackageResolution();

            case "DAMAGED", "DAMAGED_PACKAGE" ->
                    new DamagedPackageResolution();

            case "DELAYED", "DELAYED_PACKAGE" ->
                    new DelayedPackageResolution();

            case "WRONG_ADDRESS", "WRONG_ADDRESS_PACKAGE" ->
                    new WrongAddressPackageResolution();

            case "WRONG_PACKAGE", "WRONG_PACKAGE_PACKAGE" ->
                    new WrongPackageResolution();

            case "RECIPIENT_UNAVAILABLE",
                 "RECIPIENT_UNAVAILABLE_PACKAGE" ->
                    new RecipientUnavailableResolution();

            default -> null;
        };

        if (template == null) {

            historyCaretaker.discardLast(issue.getIssueId());

            System.out.println(
                    "[ERROR] No resolution workflow for type: "
                            + issue.getIssueType()
            );

            return;
        }

        try {

            issueService.resolveIssue(operator, issue);

            if (!template.resolve(issue)) {

                historyCaretaker.discardLast(issue.getIssueId());
                return;
            }

            boolean saved = issueDAO.updateStatusAndHandler(
                    issue.getIssueId(),
                    "RESOLVED",
                    issue.getHandledBy()
            );

            if (!saved) {

                System.out.println(
                        "[ERROR] Resolution ran, but handler details "
                                + "could not be saved."
                );

                return;
            }

            issue.setStatus("RESOLVED");

            issueDAO.recordHistory(
                    issue.getIssueId(),
                    oldStatus,
                    "RESOLVED",
                    operator.getUserId()
            );

            System.out.println(
                    "[RESOLUTION COMPLETE] "
                            + "Issue resolved after COR routing."
            );

            System.out.println("Final Status: RESOLVED");

            System.out.println(
                    "[DATABASE] Handled By: " + issue.getHandledBy()
            );

            broadcastStatus(issue);
            notifyResolvedCustomer(issue);

        } catch (AuthorizationException e) {

            historyCaretaker.discardLast(issue.getIssueId());

            System.out.println(
                    "[ACCESS DENIED] " + e.getMessage()
            );
        }
    }

    // ---------------- ALTERNATIVE RESOLUTION WORKFLOW ----------------

    private static void handleResolveIssue(User staff) {

        PackageIssue issue =
                promptIssueById("Enter Issue ID to Resolve: ");

        if (issue == null) {
            return;
        }

        String normalized = issue.getIssueType()
                .trim()
                .toUpperCase()
                .replaceAll("\\s+", "_");

        PackageIssueResolutionTemplate template = switch (normalized) {

            case "LOST", "LOST_PACKAGE" ->
                    new LostPackageResolution();

            case "DAMAGED", "DAMAGED_PACKAGE" ->
                    new DamagedPackageResolution();

            case "DELAYED", "DELAYED_PACKAGE" ->
                    new DelayedPackageResolution();

            case "WRONG_ADDRESS", "WRONG_ADDRESS_PACKAGE" ->
                    new WrongAddressPackageResolution();

            case "WRONG_PACKAGE", "WRONG_PACKAGE_PACKAGE" ->
                    new WrongPackageResolution();

            case "RECIPIENT_UNAVAILABLE",
                 "RECIPIENT_UNAVAILABLE_PACKAGE" ->
                    new RecipientUnavailableResolution();

            default -> null;
        };

        if (template == null) {

            System.out.println(
                    "[ERROR] No resolution workflow for type: "
                            + issue.getIssueType()
            );

            return;
        }

        try {

            issueService.resolveIssue(staff, issue);

            String oldStatus = issue.getStatus();

            historyCaretaker.save(issue);

            if (template.resolve(issue)) {

                issueDAO.recordHistory(
                        issue.getIssueId(),
                        oldStatus,
                        "RESOLVED",
                        staff.getUserId()
                );

                broadcastStatus(issue);
                notifyResolvedCustomer(issue);

            } else {

                historyCaretaker.discardLast(issue.getIssueId());
            }

        } catch (AuthorizationException e) {

            System.out.println(
                    "[ACCESS DENIED] " + e.getMessage()
            );
        }
    }

    // =====================================================
    // OBSERVER PATTERN AND STATUS NOTIFICATIONS
    // =====================================================

    private static void broadcastStatus(PackageIssue issue) {

        PackageIssueManager manager =
                new PackageIssueManager(issue);

        userDAO.findAll().forEach(manager::attach);

        manager.notifyObservers();
    }

    private static void notifyResolvedCustomer(
            PackageIssue issue) {

        Optional<User> customer = userDAO.findAll()
                .stream()
                .filter(user ->
                        user.getUserId() == issue.getCustomerId()
                )
                .findFirst();

        if (customer.isPresent()) {

            DeliveryChannel channel = promptChannel();

            new ResolutionNotification(channel)
                    .notify(
                            customer.get(),
                            issue.getTrackingNumber()
                    );

        } else {

            System.out.println(
                    "[WARN] Customer account not found; "
                            + "resolution notification skipped."
            );
        }
    }

    // =====================================================
    // ISSUE LOOKUP
    // =====================================================

    private static PackageIssue promptIssueById(String prompt) {

        System.out.print(prompt);

        try {

            int id = Integer.parseInt(
                    scanner.nextLine().trim()
            );

            Optional<PackageIssue> found =
                    issueDAO.findById(id);

            if (found.isEmpty()) {

                System.out.println(
                        "[ERROR] Issue not found."
                );

                return null;
            }

            return found.get();

        } catch (NumberFormatException e) {

            System.out.println(
                    "[ERROR] Invalid Issue ID."
            );

            return null;
        }
    }

    // =====================================================
    // VIEW ISSUES BY ROLE
    // =====================================================

    private static void viewIssuesForRole(User user) {

        if (user.getRole() == Role.DELIVERY_AGENT) {

            System.out.println(
                    "\n--- Issues Assigned to "
                            + user.getName() + " ---"
            );

            List<PackageIssue> issues =
                    issueDAO.findAssignedTo(user.getUserId());

            if (issues.isEmpty()) {

                System.out.println(
                        "No issues are currently assigned to you."
                );

                return;
            }

            issues.forEach(Main::printIssueDetails);
            return;
        }

        // Support staff, hub managers, regional managers,
        // and admins can view all issues.
        viewAllIssues();
    }

    // ---------------- VIEW ALL ISSUES ----------------

    private static void viewAllIssues() {

        System.out.println("\n--- All System Issues ---");

        List<PackageIssue> issues = issueDAO.findAll();

        if (issues.isEmpty()) {

            System.out.println("No issues found.");
            return;
        }

        issues.forEach(Main::printIssueDetails);
    }

    // ---------------- DISPLAY ISSUE DETAILS ----------------

    private static void printIssueDetails(PackageIssue issue) {

        System.out.printf(
                "[ID: #%d] Tracking: %s | Type: %-20s "
                        + "| Severity: %-8s | Value: INR %-10.2f "
                        + "| Status: %s%n",
                issue.getIssueId(),
                issue.getTrackingNumber(),
                issue.getIssueType(),
                issue.getSeverity(),
                issue.getPackageValue(),
                issue.getStatus()
        );

        System.out.println(
                "    Handled by: "
                        + (issue.getHandledBy() == null
                        ? "Not assigned yet"
                        : issue.getHandledBy())
        );
    }
}