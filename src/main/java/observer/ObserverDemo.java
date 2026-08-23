package observer;

import chainofresponsibility.PackageIssue;
import entity.User;
import enums.Role;

public class ObserverDemo {

    public static void run() {
        System.out.println("\nPHASE 5 - OBSERVER PATTERN");

        PackageIssue issue = new PackageIssue(
                101, "PKG1001", "Lost Package",
                "High-value laptop lost in transit.", "HIGH", 75000
        );

        PackageIssueManager manager = new PackageIssueManager(issue);

        User alice  = new User(2, "alice",  Role.CUSTOMER,      "alicepwd");
        User charlie = new User(4, "charlie", Role.SUPPORT_STAFF, "charpwd");
        User diana  = new User(5, "diana",  Role.HUB_MANAGER,   "dianapwd");

        System.out.println("\nTest 1: Attach observers");
        manager.attach(alice);
        manager.attach(charlie);
        manager.attach(diana);

        System.out.println("\nTest 2: Status changed to UNDER_INVESTIGATION");
        manager.updateStatus("UNDER_INVESTIGATION");

        System.out.println("\nTest 3: Status changed to ESCALATED");
        manager.updateStatus("ESCALATED");

        System.out.println("\nTest 4: Status changed to RESOLVED");
        manager.updateStatus("RESOLVED");

        System.out.println("\nTest 5: Detach Hub Manager, status changed to CLOSED");
        manager.detach(diana);
        manager.updateStatus("CLOSED");
    }

    public static void main(String[] args) {
        run();
    }
}
