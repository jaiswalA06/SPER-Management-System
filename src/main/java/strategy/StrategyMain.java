
package strategy;

import chainofresponsibility.PackageIssue;
import database.PackageIssueDAO;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class StrategyMain {

    public static void main(String[] args) {
        PackageIssueDAO issueDAO = new PackageIssueDAO();

        try (Scanner scanner = new Scanner(System.in)) {
            List<PackageIssue> issues = issueDAO.findAll();

            if (issues.isEmpty()) {
                System.out.println("No package issues found.");
                return;
            }

            System.out.println("\n--- Existing Package Issues ---");

            for (PackageIssue issue : issues) {
                System.out.println(
                        "ID: " + issue.getIssueId()
                                + " | Tracking: " + issue.getTrackingNumber()
                                + " | Type: " + issue.getIssueType()
                                + " | Value: INR " + issue.getPackageValue()
                                + " | Severity: " + issue.getSeverity()
                );
            }

            System.out.print("\nEnter Issue ID: ");

            if (!scanner.hasNextInt()) {
                System.out.println("Invalid issue ID.");
                return;
            }

            int issueId = scanner.nextInt();
            Optional<PackageIssue> result = issueDAO.findById(issueId);

            if (result.isEmpty()) {
                System.out.println("Issue not found.");
                return;
            }

            PackageIssue issue = result.get();

            AutomaticResolutionSelector selector =
                    new AutomaticResolutionSelector();

            ResolutionStrategy strategy =
                    selector.selectStrategy(issue);

            ResolutionPlan plan = strategy.createPlan(issue);

            System.out.println("\n========================================");
            System.out.println("       SPER RESOLUTION REPORT");
            System.out.println("========================================");
            System.out.println("Issue ID: " + issue.getIssueId());
            System.out.println("Tracking Number: " + issue.getTrackingNumber());
            System.out.println("Issue Type: " + issue.getIssueType());
            System.out.println("Package Value: INR " + issue.getPackageValue());
            System.out.println("Current Severity: " + issue.getSeverity());

            System.out.println("----------------------------------------");
            System.out.println("Selected Strategy: "
                    + strategy.getClass().getSimpleName());
            System.out.println("Resolution Plan: " + plan.getTitle());
            System.out.println("Priority: " + plan.getPriority());
            System.out.println("Reason: " + plan.getReason());

            System.out.println("\nRecommended Actions:");

            int step = 1;
            for (String action : plan.getActions()) {
                System.out.println(step++ + ". " + action);
            }

        } catch (Exception e) {
            System.out.println(
                    "Could not generate resolution plan: "
                            + e.getMessage()
            );
        }
    }
}
