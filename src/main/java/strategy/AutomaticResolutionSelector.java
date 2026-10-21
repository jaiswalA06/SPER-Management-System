
package strategy;

import chainofresponsibility.PackageIssue;
import enums.IssueType;

public class AutomaticResolutionSelector {

    public ResolutionStrategy selectStrategy(PackageIssue issue) {
        if (issue == null) {
            throw new IllegalArgumentException(
                    "Package issue cannot be null"
            );
        }

        IssueType type = parseIssueType(issue.getIssueType());

        return switch (type) {
            case LOST -> new LostPackageStrategy();
            case DAMAGED -> new DamagedPackageStrategy();
            case DELAYED -> new DelayedPackageStrategy();
            case WRONG_ADDRESS -> new WrongAddressStrategy();
            case WRONG_PACKAGE -> new WrongPackageStrategy();
            case RECIPIENT_UNAVAILABLE ->
                    new RecipientUnavailableStrategy();
        };
    }

    private IssueType parseIssueType(String rawType) {
        if (rawType == null || rawType.isBlank()) {
            throw new IllegalArgumentException(
                    "Issue type cannot be empty"
            );
        }

        String normalized = rawType.trim()
                .toUpperCase()
                .replace(' ', '_');

        return switch (normalized) {
            case "LOST", "LOST_PACKAGE", "MISSING_PACKAGE" ->
                    IssueType.LOST;
            case "DAMAGED", "DAMAGED_PACKAGE" ->
                    IssueType.DAMAGED;
            case "DELAYED", "DELAYED_PACKAGE" ->
                    IssueType.DELAYED;
            case "WRONG_ADDRESS", "WRONG_ADDRESS_PACKAGE" ->
                    IssueType.WRONG_ADDRESS;
            case "WRONG_PACKAGE" ->
                    IssueType.WRONG_PACKAGE;
            case "RECIPIENT_UNAVAILABLE" ->
                    IssueType.RECIPIENT_UNAVAILABLE;
            default -> throw new IllegalArgumentException(
                    "Unsupported issue type: " + rawType
            );
        };
    }
}
