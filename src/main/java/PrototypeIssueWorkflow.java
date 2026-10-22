package prototype;

import bridge.CreationNotification;
import bridge.DeliveryChannel;
import bridge.EmailChannel;
import bridge.MobileAppChannel;
import bridge.Notification;
import bridge.SMSChannel;
import chainofresponsibility.PackageIssue;
import chainofresponsibility.PackageIssueBuilder;
import database.PackageIssueDAO;
import entity.User;
import enums.IssueSeverity;
import enums.IssueType;
import proxy.AuthorizationException;
import proxy.IssueService;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;
import java.util.function.BiFunction;

// Prototype Pattern — Client: the customer-facing flows that create issues by cloning blueprints.
// It reuses the existing Proxy (authorization), DAO (persistence) and Bridge (notification) layers,
// and the severity rule is passed in from Main so the rule is not duplicated here.
public class PrototypeIssueWorkflow {

    private final Scanner scanner;
    private final PackageIssueDAO issueDAO;
    private final IssueService issueService;
    private final BiFunction<IssueType, Double, IssueSeverity> severityRule;
    private final IssueTemplateRegistry registry = new IssueTemplateRegistry();

    public PrototypeIssueWorkflow(Scanner scanner, PackageIssueDAO issueDAO, IssueService issueService,
                                  BiFunction<IssueType, Double, IssueSeverity> severityRule) {
        this.scanner = scanner;
        this.issueDAO = issueDAO;
        this.issueService = issueService;
        this.severityRule = severityRule;
    }

    // ==========================================
    // Flow 1: report a new issue from a ready-made template
    // ==========================================
    public void reportFromTemplate(User customer) {
        System.out.println("\n--- Report Issue from Template (Prototype) ---");

        List<String> keys = registry.keys();
        for (int i = 0; i < keys.size(); i++) {
            IssueTemplate template = registry.get(keys.get(i)).orElseThrow();
            System.out.printf("%d. %-22s | Severity: %-6s | %s%n",
                    i + 1, template.getTemplateName(), template.getSeverity(), template.getDescription());
        }
        System.out.print("Select template: ");

        int index;
        try {
            index = Integer.parseInt(scanner.nextLine().trim()) - 1;
        } catch (NumberFormatException e) {
            System.out.println("[ERROR] Invalid selection.");
            return;
        }
        if (index < 0 || index >= keys.size()) {
            System.out.println("[ERROR] Invalid selection.");
            return;
        }

        // The prototype in action: work on a clone, the stored template stays untouched
        IssueTemplate issueCopy = registry.get(keys.get(index)).orElseThrow();
        System.out.println("[Prototype] Cloned template: " + issueCopy.getTemplateName());

        String trackingNumber = promptTrackingNumber();

        System.out.println("Default description: " + issueCopy.getDescription());
        System.out.print("Enter Issue Description (press Enter to keep default): ");
        String description = scanner.nextLine().trim();
        if (!description.isBlank()) {
            issueCopy.withDescription(description);
        }

        System.out.printf("Enter Package Value (press Enter to keep INR %.2f): ", issueCopy.getPackageValue());
        String valueInput = scanner.nextLine().trim();
        if (!valueInput.isBlank()) {
            try {
                double value = Double.parseDouble(valueInput);
                if (value < 0) {
                    System.out.println("[WARN] Negative value ignored; keeping INR " + issueCopy.getPackageValue());
                } else {
                    issueCopy.withPackageValue(value);
                }
            } catch (NumberFormatException e) {
                System.out.println("[WARN] Invalid value ignored; keeping INR " + issueCopy.getPackageValue());
            }
        }

        applySeverityRule(issueCopy);

        Optional<PackageIssue> saved = saveIssue(customer, issueCopy, trackingNumber);
        saved.ifPresent(issue -> notifyCustomer(customer, List.of(issue)));
    }

    // ==========================================
    // Flow 2: duplicate an existing issue for several similar packages
    // ==========================================
    public void duplicateExistingIssue(User customer) {
        System.out.println("\n--- Duplicate an Existing Issue (Prototype) ---");

        List<PackageIssue> mine;
        try {
            issueService.viewIssue(customer, customer.getUserId(), null);
            mine = issueDAO.findByCustomerId(customer.getUserId());
        } catch (AuthorizationException e) {
            System.out.println("[ACCESS DENIED] " + e.getMessage());
            return;
        }

        if (mine.isEmpty()) {
            System.out.println("You have no reported issues to duplicate. Report one first.");
            return;
        }
        for (PackageIssue issue : mine) {
            System.out.printf("[ID: #%d] Tracking: %s | Type: %-15s | Severity: %-7s | Value: INR %-7.2f | Status: %s%n",
                    issue.getIssueId(), issue.getTrackingNumber(), issue.getIssueType(),
                    issue.getSeverity(), issue.getPackageValue(), issue.getStatus());
        }

        System.out.print("Enter Issue ID to duplicate: ");
        int issueId;
        try {
            issueId = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("[ERROR] Invalid ID format.");
            return;
        }

        // Only the customer's own issues are listed, so this also guarantees ownership
        Optional<PackageIssue> source = mine.stream().filter(i -> i.getIssueId() == issueId).findFirst();
        if (source.isEmpty()) {
            System.out.println("[ERROR] Issue not found among your reported issues.");
            return;
        }

        IssueTemplate prototype;
        try {
            prototype = IssueTemplate.fromIssue(source.get());
        } catch (IllegalArgumentException e) {
            System.out.println("[ERROR] Cannot duplicate this issue: " + e.getMessage());
            return;
        }
        System.out.println("[Prototype] Using issue #" + issueId + " as the prototype: " + prototype);

        System.out.print("Enter Description for the copies (press Enter to keep the original): ");
        String description = scanner.nextLine().trim();
        if (!description.isBlank()) {
            prototype.withDescription(description);
        }

        System.out.print("Enter Tracking Numbers for the copies (comma separated, e.g. PKG1002, PKG1003): ");
        Set<String> trackingNumbers = new LinkedHashSet<>();
        for (String part : scanner.nextLine().split(",")) {
            String trackingNumber = part.trim().toUpperCase();
            if (trackingNumber.isEmpty()) {
                continue;
            }
            if (!PackageIssueBuilder.isValidTrackingNumber(trackingNumber)) {
                System.out.println("[WARN] Skipped '" + trackingNumber
                        + "': must start with 'PKG' followed by 4 to 8 digits.");
            } else if (trackingNumber.equals(source.get().getTrackingNumber())) {
                System.out.println("[WARN] Skipped '" + trackingNumber + "': it is the package you are copying from.");
            } else if (!trackingNumbers.add(trackingNumber)) {
                System.out.println("[WARN] Skipped '" + trackingNumber + "': entered more than once.");
            }
        }
        if (trackingNumbers.isEmpty()) {
            System.out.println("[ERROR] No valid tracking numbers entered. Nothing was created.");
            return;
        }

        List<PackageIssue> created = new ArrayList<>();
        for (String trackingNumber : trackingNumbers) {
            // One independent clone per package, all derived from the same prototype
            IssueTemplate copy = prototype.clone();
            saveIssue(customer, copy, trackingNumber).ifPresent(created::add);
        }

        System.out.println("[SUMMARY] " + created.size() + " of " + trackingNumbers.size()
                + " duplicate issue(s) registered.");
        if (!created.isEmpty()) {
            notifyCustomer(customer, created);
        }
    }

    // ==========================================
    // Shared helpers
    // ==========================================
    private String promptTrackingNumber() {
        while (true) {
            System.out.print("Enter Tracking Number (e.g. PKG1001): ");
            String trackingNumber = scanner.nextLine().trim().toUpperCase();
            if (PackageIssueBuilder.isValidTrackingNumber(trackingNumber)) {
                return trackingNumber;
            }
            System.out.println("[ERROR] Invalid format. Must start with 'PKG' followed by 4 to 8 digits.");
        }
    }

    // A template's severity is only a starting point: never go below what the severity rule gives for the value
    private void applySeverityRule(IssueTemplate issueCopy) {
        IssueSeverity ruleSeverity = severityRule.apply(issueCopy.getIssueType(), issueCopy.getPackageValue());
        if (ruleSeverity.ordinal() > issueCopy.getSeverity().ordinal()) {
            issueCopy.withSeverity(ruleSeverity);
        }
        System.out.println("[INFO] Evaluated Severity Level: " + issueCopy.getSeverity());
    }

    // Proxy check, build (validated by the builder) and persist. Returns empty if anything failed.
    private Optional<PackageIssue> saveIssue(User customer, IssueTemplate template, String trackingNumber) {
        PackageIssue issue;
        try {
            issue = template.createIssue(trackingNumber, customer.getUserId());
        } catch (IllegalStateException e) {
            System.out.println("[ERROR] Cannot register issue for " + trackingNumber + ": " + e.getMessage());
            return Optional.empty();
        }

        try {
            issueService.reportIssue(customer, customer.getUserId(), template.getIssueType());
        } catch (AuthorizationException e) {
            System.out.println("[ACCESS DENIED] " + e.getMessage());
            return Optional.empty();
        }

        int issueId = issueDAO.insert(issue);
        if (issueId < 0) {
            System.out.println("[ERROR] Failed to save the issue for " + trackingNumber + " in the database.");
            return Optional.empty();
        }
        System.out.println("[SUCCESS] Issue #" + issueId + " registered in database (" + trackingNumber + ").");
        return Optional.of(issue);
    }

    // Bridge Pattern: the channel is chosen once and used for every issue created in this step
    private void notifyCustomer(User customer, List<PackageIssue> issues) {
        System.out.println("\nSelect Notification Channel: 1. Email | 2. SMS | 3. Mobile App Push");
        System.out.print("Choice: ");
        DeliveryChannel channel = switch (scanner.nextLine().trim()) {
            case "2" -> new SMSChannel();
            case "3" -> new MobileAppChannel();
            default -> new EmailChannel();
        };

        Notification notification = new CreationNotification(channel);
        for (PackageIssue issue : issues) {
            notification.notify(customer, issue.getTrackingNumber());
        }
    }
}
