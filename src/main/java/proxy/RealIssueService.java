package proxy;

import entity.User;
import enums.IssueType;
import factorypattern.ConcretePackageIssueFactory;
import factorypattern.PackageIssue;
import factorypattern.PackageIssueFactory;

public class RealIssueService implements IssueService {

    private final PackageIssueFactory factory = new ConcretePackageIssueFactory();

    @Override
    public PackageIssue reportIssue(User requester, int customerId, IssueType issueType) {
        PackageIssue issue = factory.createIssue(issueType);
        System.out.println("Issue reported by '" + requester.getName()
                + "' for customer ID " + customerId + " → " + issueType);
        issue.displayIssue();
        return issue;
    }

    @Override
    public void viewIssue(User requester, int customerId, PackageIssue issue) {
        System.out.println(requester.getName()
                + "' is viewing issue for customer ID " + customerId + ":");
        issue.displayIssue();
    }

    @Override
    public void resolveIssue(User requester, chainofresponsibility.PackageIssue issue) {
        System.out.println("[RealIssueService] " + requester.getName()
                + " is authorized to resolve issue #" + issue.getIssueId()
                + " (tracking: " + issue.getTrackingNumber() + ").");
    }

    @Override
    public void deleteIssue(User requester, PackageIssue issue) {
        System.out.println("Admin '" + requester.getName()
                + "' deleted/closed the issue.");
    }
}