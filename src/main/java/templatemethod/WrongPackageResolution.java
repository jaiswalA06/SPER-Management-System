package templatemethod;

import chainofresponsibility.PackageIssue;

public class WrongPackageResolution extends PackageIssueResolutionTemplate {

    @Override
    protected void investigate(PackageIssue issue) {
        System.out.println("Comparing the package contents and label details for tracking number "
                + issue.getTrackingNumber() + ".");
        System.out.println("Reviewing the customer's mismatch report: " + issue.getDescription());
        System.out.println("Checking dispatch and sorting records for a possible package mix-up.");
    }

    @Override
    protected String determineResolution(PackageIssue issue) {
        return "Resolution for wrong package " + issue.getTrackingNumber()
                + ": verify the shipment mismatch, coordinate collection or return of the incorrect parcel, and arrange the correct delivery.";
    }
}
