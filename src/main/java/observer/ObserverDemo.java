package observer;

import chainofresponsibility.PackageIssue;
import entity.User;
import enums.Role;

public class ObserverDemo {

    public static void run() {
        System.out.println("\n===== Observer Pattern Demo =====");

        // Create a package issue
        PackageIssue issue = new PackageIssue(101, "TRK-2024-XYZ", "LOST",
                "High-value laptop lost in transit.", "HIGH", 75000.0);

        // Wrap it in a manager (Subject)
        PackageIssueManager manager = new PackageIssueManager(issue);

        // Register stakeholders as observers
        User customer     = new User(1, "Rahul Sharma", Role.CUSTOMER,      "pass123");
        User supportAgent = new User(2, "Priya Mehta",  Role.SUPPORT_STAFF, "agent456");
        User hubManager   = new User(3, "Amit Verma",   Role.HUB_MANAGER,   "hub789");

        manager.attach(customer);
        manager.attach(supportAgent);
        manager.attach(hubManager);

        // Simulate status transitions — observers are notified automatically
        manager.updateStatus("UNDER_INVESTIGATION");
        manager.updateStatus("ESCALATED");
        manager.updateStatus("RESOLVED");

        // Detach Hub Manager — no longer needed after resolution
        System.out.println("\n[Detaching Hub Manager...]");
        manager.detach(hubManager);
        manager.updateStatus("CLOSED");
    }

    public static void main(String[] args) {
        run();
    }
}
