
package strategy;

import chainofresponsibility.PackageIssue;

public class AutomaticEstimationSelector {

    public ResolutionEstimationStrategy selectStrategy(PackageIssue issue) {
        String issueType = issue.getIssueType()
                .trim()
                .toUpperCase()
                .replace(' ', '_');

        switch (issueType) {
            case "LOST":
                return new LostPackageEstimationStrategy();

            case "DAMAGED":
                return new DamagedPackageEstimationStrategy();

            case "DELAYED":
                return new DelayedPackageEstimationStrategy();

            case "WRONG_ADDRESS":
                return new WrongAddressEstimationStrategy();

            case "WRONG_PACKAGE":
                return new WrongPackageEstimationStrategy();

            case "RECIPIENT_UNAVAILABLE":
                return new RecipientUnavailableEstimationStrategy();

            default:
                throw new IllegalArgumentException(
                        "Unsupported issue type: " + issue.getIssueType()
                );
        }
    }
}
