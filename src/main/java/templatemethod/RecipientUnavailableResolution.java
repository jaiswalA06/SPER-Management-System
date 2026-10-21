package templatemethod;

import chainofresponsibility.PackageIssue;

public class RecipientUnavailableResolution extends PackageIssueResolutionTemplate {

    @Override
    protected void investigate(PackageIssue issue) {
        System.out.println("Reviewing delivery attempts for tracking number "
                + issue.getTrackingNumber() + ".");
        System.out.println("Checking the recipient-unavailable report: " + issue.getDescription());
        System.out.println("Reviewing the recorded delivery severity: " + issue.getSeverity());
    }

    @Override
    protected String determineResolution(PackageIssue issue) {
        return "Resolution for recipient-unavailable package " + issue.getTrackingNumber()
                + ": coordinate a suitable redelivery attempt or arrange an approved pickup or holding option.";
    }
}
