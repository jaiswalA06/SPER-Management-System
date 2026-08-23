package factorypattern;

import enums.IssueType;

public class ConcretePackageIssueFactory implements PackageIssueFactory {

    @Override
    public PackageIssue createIssue(IssueType issueType) {

        switch (issueType) {
            case DAMAGED:
                return new DamagedPackageIssue();

            case LOST:
                return new LostPackageIssue();

            case DELAYED:
                return new DelayedPackageIssue();

            case WRONG_ADDRESS:
                return new WrongAddressPackageIssue();

            case WRONG_PACKAGE:
                return new WrongPackageIssue();

            case RECIPIENT_UNAVAILABLE:
                return new RecipientUnavailableIssue();

            default:
                throw new IllegalArgumentException(
                    "Unknown issue type: " + issueType
                );
        }
    }
}