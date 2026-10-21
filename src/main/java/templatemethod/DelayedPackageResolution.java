package templatemethod;

import chainofresponsibility.PackageIssue;

public class DelayedPackageResolution extends PackageIssueResolutionTemplate {

    @Override
    protected void investigate(PackageIssue issue) {
        System.out.println("Reviewing the delivery exception for tracking number "
                + issue.getTrackingNumber() + ".");
        System.out.println("Checking the reported delay details: " + issue.getDescription());
        System.out.println("Current recorded severity: " + issue.getSeverity());
    }

    @Override
    protected String determineResolution(PackageIssue issue) {
        return "Resolution for delayed package " + issue.getTrackingNumber()
                + ": delivery delay reviewed; confirm the latest shipment status and arrange the next delivery update.";
    }
}