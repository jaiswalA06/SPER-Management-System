package templatemethod;

import chainofresponsibility.PackageIssue;

public class WrongAddressPackageResolution extends PackageIssueResolutionTemplate {

    @Override
    protected void investigate(PackageIssue issue) {
        System.out.println("Reviewing the delivery address associated with tracking number "
                + issue.getTrackingNumber() + ".");
        System.out.println("Checking the customer's reported address issue: " + issue.getDescription());
        System.out.println("Verifying available shipment and routing details before rerouting.");
    }

    @Override
    protected String determineResolution(PackageIssue issue) {
        return "Resolution for wrong-address package " + issue.getTrackingNumber()
                + ": verify the corrected address with the customer and arrange an address correction or rerouting where permitted.";
    }
}
