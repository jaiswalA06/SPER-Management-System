package proxy;

import entity.User;
import enums.IssueType;
import factorypattern.PackageIssue;

public class ProxyDemo {

    public static void run() {

        AuthenticationService authService = new AuthenticationService();
        IssueService proxy = new IssueServiceProxy(authService);

        User admin      = getUser(authService, 1, "admin123");
        User ramesh     = getUser(authService, 2, "ramesh123");
        User rahul      = getUser(authService, 3, "rahul123");
        User sunderlal  = getUser(authService, 4, "sunder123");
        User daya       = getUser(authService, 5, "daya123");

        System.out.println("\nTest 1: Own issue");
        PackageIssue issue1 = proxy.reportIssue(ramesh, ramesh.getUserId(), IssueType.DAMAGED);

        System.out.println("\nTest 2: Other customer");
        try {
            proxy.reportIssue(ramesh, rahul.getUserId(), IssueType.LOST);
        } catch (AuthorizationException e) {
            System.out.println(e.getMessage());
        }

        System.out.println("\nTest 3: Staff reports issue");
        PackageIssue issue2 = proxy.reportIssue(sunderlal, rahul.getUserId(), IssueType.DELAYED);

        System.out.println("\nTest 4: View own issue");
        proxy.viewIssue(ramesh, ramesh.getUserId(), issue1);

        System.out.println("\nTest 5: View other issue");
        try {
            proxy.viewIssue(ramesh, rahul.getUserId(), issue2);
        } catch (AuthorizationException e) {
            System.out.println(e.getMessage());
        }

        System.out.println("\nTest 6: Admin views issue");
        proxy.viewIssue(admin, rahul.getUserId(), issue2);

        System.out.println("\nTest 7: Manager resolves issue");
        proxy.resolveIssue(daya, issue2);

        System.out.println("\nTest 8: Customer resolves issue");
        try {
            proxy.resolveIssue(ramesh, issue1);
        } catch (AuthorizationException e) {
            System.out.println(e.getMessage());
        }

        System.out.println("\nTest 9: Admin deletes issue");
        proxy.deleteIssue(admin, issue1);

        System.out.println("\nTest 10: Staff deletes issue");
        try {
            proxy.deleteIssue(sunderlal, issue1);
        } catch (AuthorizationException e) {
            System.out.println(e.getMessage());
        }

        System.out.println("\nTest 11: Wrong password");
        var badLogin = authService.authenticate(2, "wrongpassword");
        if (badLogin.isEmpty()) {
            System.out.println("Authentication failed");
        }
    }

    private static User getUser(AuthenticationService auth, int id, String password) {
        return auth.authenticate(id, password)
                .orElseThrow(() -> new AuthorizationException("Login failed for user ID " + id));
    }
}