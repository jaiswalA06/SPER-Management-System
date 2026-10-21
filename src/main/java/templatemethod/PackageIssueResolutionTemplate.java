package templatemethod;

import chainofresponsibility.PackageIssue;
import database.PackageIssueDAO;

/**
 * Defines the fixed workflow for resolving a package issue.
 * Subclasses provide issue-specific investigation and resolution details.
 */
public abstract class PackageIssueResolutionTemplate {

    private final PackageIssueDAO issueDAO;

    protected PackageIssueResolutionTemplate() {
        this(new PackageIssueDAO());
    }

    protected PackageIssueResolutionTemplate(PackageIssueDAO issueDAO) {
        this.issueDAO = issueDAO;
    }

    public final boolean resolve(PackageIssue issue) {
        if (issue == null || issue.getIssueId() <= 0) {
            System.out.println("[ERROR] A valid persisted issue is required.");
            return false;
        }

        if ("RESOLVED".equalsIgnoreCase(issue.getStatus())) {
            System.out.println("[INFO] Issue #" + issue.getIssueId() + " is already resolved.");
            return false;
        }

        System.out.println("\n--- Template Method: Issue Resolution ---");
        System.out.println("Step 1: Validating issue #" + issue.getIssueId() + "...");
        System.out.println("[SUCCESS] Issue is valid.");

        System.out.println("Step 2: Investigating issue...");
        investigate(issue);

        System.out.println("Step 3: Determining resolution...");
        String resolution = determineResolution(issue);

        System.out.println("Step 4: Updating issue status...");
        if (!issueDAO.resolveIssue(issue.getIssueId(), "RESOLVED")) {
            System.out.println("[ERROR] Could not persist the resolved status.");
            return false;
        }
        issue.setStatus("RESOLVED");

        System.out.println("Step 5: Resolution outcome");
        System.out.println(resolution);
        System.out.println("[SUCCESS] Issue #" + issue.getIssueId() + " marked as RESOLVED.");
        return true;
    }

    protected abstract void investigate(PackageIssue issue);

    protected abstract String determineResolution(PackageIssue issue);
}