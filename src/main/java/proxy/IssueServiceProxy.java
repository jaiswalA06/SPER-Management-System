package proxy;

import entity.User;
import enums.IssueType;
import enums.Role;
import factorypattern.PackageIssue;

public class IssueServiceProxy implements IssueService {

    private final RealIssueService realService;
    private final AuthenticationService authService;

    public IssueServiceProxy(AuthenticationService authService) {
        this.realService = new RealIssueService();
        this.authService = authService;
    }

    @Override
    public PackageIssue reportIssue(User requester, int customerId, IssueType issueType) {
        System.out.println("Checking user");
        authenticate(requester);

        Role role = requester.getRole();

        if (role == Role.DELIVERY_AGENT) {
            throw new AuthorizationException("Agents cannot report issues");
        }

        if (role == Role.CUSTOMER && requester.getUserId() != customerId) {
            throw new AuthorizationException("Cannot report for others");
        }

        System.out.println("Access granted");
        return realService.reportIssue(requester, customerId, issueType);
    }

    @Override
    public void viewIssue(User requester, int customerId, PackageIssue issue) {
        System.out.println("Checking user");
        authenticate(requester);

        Role role = requester.getRole();

        if (role == Role.DELIVERY_AGENT) {
            throw new AuthorizationException("Agents cannot view issues");
        }

        if (role == Role.CUSTOMER && requester.getUserId() != customerId) {
            throw new AuthorizationException("Cannot view others issues");
        }

        System.out.println("Access granted");
        realService.viewIssue(requester, customerId, issue);
    }

    @Override
    public void resolveIssue(User requester, PackageIssue issue) {
        System.out.println("Checking user");
        authenticate(requester);

        Role role = requester.getRole();

        if (role == Role.CUSTOMER || role == Role.DELIVERY_AGENT) {
            throw new AuthorizationException("Role cannot resolve issues");
        }

        System.out.println("Access granted");
        realService.resolveIssue(requester, issue);
    }

    @Override
    public void deleteIssue(User requester, PackageIssue issue) {
        System.out.println("Checking user");
        authenticate(requester);

        if (requester.getRole() != Role.ADMIN) {
            throw new AuthorizationException("Only admin can delete");
        }

        System.out.println("Access granted");
        realService.deleteIssue(requester, issue);
    }

    private void authenticate(User requester) {
        if (requester == null) {
            throw new AuthorizationException("User not provided");
        }

        boolean valid = authService.authenticate(requester.getUserId(), requester.getPassword()).isPresent();

        if (!valid) {
            throw new AuthorizationException("Authentication failed");
        }

        System.out.println("Authentication successful");
    }
}