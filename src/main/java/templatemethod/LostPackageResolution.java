package templatemethod;

import chainofresponsibility.PackageIssue;

public class LostPackageResolution extends PackageIssueResolutionTemplate {

    @Override
    protected void investigate(PackageIssue issue) {
        System.out.println("Tracing the last recorded movement for tracking number "
                + issue.getTrackingNumber() + ".");
        System.out.println("Reviewing the reported loss details: " + issue.getDescription());
        System.out.println("Checking shipment records and escalation severity: " + issue.getSeverity());
    }

    @Override
    protected String determineResolution(PackageIssue issue) {
        return "Resolution for lost package " + issue.getTrackingNumber()
                + ": complete the shipment trace, escalate an unlocated parcel, and assess the applicable claim or reimbursement policy.";
    }
}
