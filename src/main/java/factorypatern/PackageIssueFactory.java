package factorypatern;

import enums.IssueType;

public interface PackageIssueFactory {
    PackageIssue createIssue(IssueType issueType);
}