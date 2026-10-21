package proxy;

import entity.User;
import enums.IssueType;
import factorypattern.PackageIssue;

public interface IssueService {
    PackageIssue reportIssue(User requester, int customerId, IssueType issueType);

    void viewIssue(User requester, int customerId, PackageIssue issue);

    void resolveIssue(User requester, chainofresponsibility.PackageIssue issue);

    void deleteIssue(User requester, PackageIssue issue);
}