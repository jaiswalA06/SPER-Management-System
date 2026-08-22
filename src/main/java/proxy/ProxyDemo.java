package proxy;

import entity.User;
import enums.IssueType;
import factorypatern.PackageIssue;

import java.util.Optional;

public class ProxyDemo {

    public static void run() {
        System.out.println("\nPHASE 2 - PROXY PATTERN");

        AuthenticationService authService = new AuthenticationService();
        IssueService proxy = new IssueServiceProxy(authService);

        User admin = getUser(authService, 1, "admin123");
        User alice = getUser(authService, 2, "alicepwd");
        User bob = getUser(authService, 3, "bobpwd");
        User charlie = getUser(authService, 4, "charpwd");
        User diana = getUser(authService, 5, "dianapwd");

        System.out.println("\nTest 1: Own issue");
        PackageIssue issue1 = proxy.reportIssue(alice, alice.getUserId(), IssueType.DAMAGED);

        System.out.println("\nTest 2: Other customer");
        try {
            proxy.reportIssue(alice, bob.getUserId(), IssueType.LOST);
        } catch (AuthorizationException e) {
            System.out.println(e.getMessage());
        }

        System.out.println("\nTest 3: Staff reports issue");
        PackageIssue issue2 = proxy.reportIssue(charlie, bob.getUserId(), IssueType.DELAYED);

        System.out.println("\nTest 4: View own issue");
        proxy.viewIssue(alice, alice.getUserId(), issue1);

        System.out.println("\nTest 5: View other issue");
        try {
            proxy.viewIssue(alice, bob.getUserId(), issue2);
        } catch (AuthorizationException e) {
            System.out.println(e.getMessage());
        }

        System.out.println("\nTest 6: Admin views issue");
        proxy.viewIssue(admin, bob.getUserId(), issue2);

        System.out.println("\nTest 7: Manager resolves issue");
        proxy.resolveIssue(diana, issue2);

        System.out.println("\nTest 8: Customer resolves issue");
        try {
            proxy.resolveIssue(alice, issue1);
        } catch (AuthorizationException e) {
            System.out.println(e.getMessage());
        }

        System.out.println("\nTest 9: Admin deletes issue");
        proxy.deleteIssue(admin, issue1);

        System.out.println("\nTest 10: Staff deletes issue");
        try {
            proxy.deleteIssue(charlie, issue1);
        } catch (AuthorizationException e) {
            System.out.println(e.getMessage());
        }

        System.out.println("\nTest 11: Wrong password");
        Optional<User> badLogin = authService.authenticate(2, "wrongpassword");
        if (badLogin.isEmpty()) {
            System.out.println("Authentication failed");
        }
    }

    private static User getUser(AuthenticationService auth, int id, String password) {
        return auth.authenticate(id, password)
                .orElseThrow(() -> new AuthorizationException("Login failed"));
    }
}