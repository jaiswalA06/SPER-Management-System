package templatemethod;

import chainofresponsibility.PackageIssue;

public class DamagedPackageResolution extends PackageIssueResolutionTemplate {

    @Override
    protected void investigate(PackageIssue issue) {
        System.out.println("Inspecting the damage report for tracking number "
                + issue.getTrackingNumber() + ".");
        System.out.println("Reviewing the customer's description: " + issue.getDescription());
        System.out.println("Recorded package value: " + issue.getPackageValue());
    }

    @Override
    protected String determineResolution(PackageIssue issue) {
        return "Resolution for damaged package " + issue.getTrackingNumber()
                + ": damage assessment completed; proceed with the applicable compensation or replacement policy.";
    }
}