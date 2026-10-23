package prototype;

import bridge.*;
import chainofresponsibility.PackageIssue;
import chainofresponsibility.PackageIssueBuilder;
import database.PackageIssueDAO;
import entity.User;
import enums.IssueSeverity;
import enums.IssueType;
import proxy.AuthorizationException;
import proxy.IssueService;
import strategy.ResolutionEstimate;
import strategy.ResolutionEstimator;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;
import java.util.function.BiFunction;

// Prototype workflow connected to the customer menu. Each request clones a blueprint,
// validates it through Builder, checks permission through Proxy, persists it, estimates via Strategy,
// and sends a Bridge notification.
public class PrototypeIssueWorkflow {
    private final Scanner scanner;
    private final PackageIssueDAO issueDAO;
    private final IssueService issueService;
    private final BiFunction<IssueType, Double, IssueSeverity> severityRule;
    private final IssueTemplateRegistry registry = new IssueTemplateRegistry();
    private final ResolutionEstimator estimator = new ResolutionEstimator();

    public PrototypeIssueWorkflow(Scanner scanner, PackageIssueDAO issueDAO, IssueService issueService,
                                  BiFunction<IssueType, Double, IssueSeverity> severityRule) {
        this.scanner = scanner;
        this.issueDAO = issueDAO;
        this.issueService = issueService;
        this.severityRule = severityRule;
    }

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

        IssueTemplate copy = registry.get(keys.get(index)).orElseThrow();
        System.out.println("[Prototype] Cloned template: " + copy.getTemplateName());
        String trackingNumber = promptTrackingNumber();

        System.out.println("Default description: " + copy.getDescription());
        System.out.print("Enter description (Enter keeps default): ");
        String description = scanner.nextLine().trim();
        if (!description.isBlank()) copy.withDescription(description);

        System.out.printf("Package value (Enter keeps INR %.2f): ", copy.getPackageValue());
        String valueText = scanner.nextLine().trim();
        if (!valueText.isBlank()) {
            try {
                double value = Double.parseDouble(valueText);
                if (!Double.isFinite(value) || value < 0) {
                    System.out.println("[ERROR] Package value must be non-negative.");
                    return;
                }
                copy.withPackageValue(value);
            } catch (NumberFormatException e) {
                System.out.println("[ERROR] Enter a valid package value.");
                return;
            }
        }

        applySeverityRule(copy);
        saveIssue(customer, copy, trackingNumber).ifPresent(issue -> {
            printEstimate(issue);
            sendCreationNotification(customer, issue);
        });
    }

    public void duplicateExistingIssue(User customer) {
        System.out.println("\n--- Duplicate an Existing Issue (Prototype) ---");
        try {
            issueService.viewIssue(customer, customer.getUserId(), null);
        } catch (AuthorizationException e) {
            System.out.println("[ACCESS DENIED] " + e.getMessage());
            return;
        }

        List<PackageIssue> mine = issueDAO.findByCustomerId(customer.getUserId());
        if (mine.isEmpty()) {
            System.out.println("You have no issues to duplicate. Report one first.");
            return;
        }
        for (PackageIssue issue : mine) {
            System.out.printf("[ID: #%d] Tracking: %s | Type: %s | Status: %s%n",
                    issue.getIssueId(), issue.getTrackingNumber(), issue.getIssueType(), issue.getStatus());
        }

        System.out.print("Enter Issue ID to duplicate: ");
        int issueId;
        try {
            issueId = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("[ERROR] Invalid Issue ID.");
            return;
        }
        Optional<PackageIssue> source = mine.stream().filter(i -> i.getIssueId() == issueId).findFirst();
        if (source.isEmpty()) {
            System.out.println("[ERROR] Issue not found among your own issues.");
            return;
        }

        IssueTemplate prototype;
        try {
            prototype = IssueTemplate.fromIssue(source.get());
        } catch (IllegalArgumentException e) {
            System.out.println("[ERROR] Cannot duplicate issue: " + e.getMessage());
            return;
        }
        System.out.println("[Prototype] Using issue #" + issueId + " as the prototype: " + prototype);

        System.out.print("Description for copies (Enter keeps original): ");
        String description = scanner.nextLine().trim();
        if (!description.isBlank()) prototype.withDescription(description);

        System.out.print("Tracking numbers separated by commas (e.g. PKG1234, PKG1235): ");
        Set<String> trackingNumbers = new LinkedHashSet<>();
        for (String part : scanner.nextLine().split(",")) {
            String tracking = part.trim().toUpperCase();
            if (tracking.isBlank()) continue;
            if (!PackageIssueBuilder.isValidTrackingNumber(tracking)) {
                System.out.println("[WARN] Skipped " + tracking + ": invalid format.");
            } else if (tracking.equalsIgnoreCase(source.get().getTrackingNumber())) {
                System.out.println("[WARN] Skipped source tracking number " + tracking + ".");
            } else if (trackingNumberExists(tracking)) {
                System.out.println("[WARN] Skipped " + tracking + ": tracking number is already registered.");
            } else if (!trackingNumbers.add(tracking)) {
                System.out.println("[WARN] Skipped duplicate tracking number " + tracking + ".");
            }
        }
        if (trackingNumbers.isEmpty()) {
            System.out.println("[ERROR] No valid new tracking numbers supplied.");
            return;
        }

        List<PackageIssue> created = new ArrayList<>();
        for (String tracking : trackingNumbers) {
            IssueTemplate copy = prototype.clone();
            applySeverityRule(copy);
            saveIssue(customer, copy, tracking).ifPresent(created::add);
        }
        System.out.printf("[SUMMARY] %d of %d duplicate issue(s) registered.%n",
                created.size(), trackingNumbers.size());

        for (PackageIssue issue : created) printEstimate(issue);
        if (!created.isEmpty()) sendCreationNotification(customer, created.get(0));
    }

    private void applySeverityRule(IssueTemplate template) {
        IssueSeverity calculated = severityRule.apply(template.getIssueType(), template.getPackageValue());
        if (calculated.ordinal() > template.getSeverity().ordinal()) template.withSeverity(calculated);
        System.out.println("[INFO] Evaluated Severity Level: " + template.getSeverity());
    }

    private Optional<PackageIssue> saveIssue(User customer, IssueTemplate template, String trackingNumber) {
        PackageIssue issue;
        try {
            issue = template.createIssue(trackingNumber, customer.getUserId());
            issueService.reportIssue(customer, customer.getUserId(), template.getIssueType());
        } catch (IllegalStateException e) {
            System.out.println("[ERROR] Cannot register issue: " + e.getMessage());
            return Optional.empty();
        } catch (AuthorizationException e) {
            System.out.println("[ACCESS DENIED] " + e.getMessage());
            return Optional.empty();
        }

        int id = issueDAO.insert(issue);
        if (id < 0) {
            System.out.println("[ERROR] Failed to save issue for " + trackingNumber + ".");
            return Optional.empty();
        }
        System.out.println("[SUCCESS] Issue #" + id + " registered in database (" + trackingNumber + ").");
        return Optional.of(issue);
    }

    private void printEstimate(PackageIssue issue) {
        try {
            ResolutionEstimate estimate = estimator.estimate(issue);
            System.out.println("\n--- Automatic Resolution Estimate (Strategy) ---");
            System.out.println("Selected Strategy: " + estimator.getSelectedStrategyName(issue));
            System.out.println("Estimated Resolution Time: " + estimate.getEstimatedDays() + " day(s)");
            System.out.printf("Estimated Customer Refund: INR %.2f%n", estimate.getEstimatedRefund());
            System.out.println("Reason: " + estimate.getReason());
            System.out.println("Note: Estimate is illustrative and subject to company policy.");
        } catch (IllegalArgumentException e) {
            System.out.println("[WARN] Could not estimate issue: " + e.getMessage());
        }
    }

    private void sendCreationNotification(User customer, PackageIssue issue) {
        System.out.println("\nSelect Notification Channel: 1. Email | 2. SMS | 3. Mobile App Push");
        System.out.print("Choice: ");
        DeliveryChannel channel = switch (scanner.nextLine().trim()) {
            case "2" -> new SMSChannel();
            case "3" -> new MobileAppChannel();
            default -> new EmailChannel();
        };
        new CreationNotification(channel).notify(customer, issue.getTrackingNumber());
    }

    private boolean trackingNumberExists(String trackingNumber) {
        return issueDAO.findAll().stream()
                .anyMatch(issue -> trackingNumber.equalsIgnoreCase(issue.getTrackingNumber()));
    }

    private String promptTrackingNumber() {
        while (true) {
            System.out.print("Enter Tracking Number (PKG + 4 to 8 digits): ");
            String tracking = scanner.nextLine().trim().toUpperCase();
            if (!PackageIssueBuilder.isValidTrackingNumber(tracking)) {
                System.out.println("[ERROR] Invalid format. Example: PKG1234.");
                continue;
            }
            if (trackingNumberExists(tracking)) {
                System.out.println("[ERROR] This tracking number is already registered. Enter a new one.");
                continue;
            }
            return tracking;
        }
    }
}
