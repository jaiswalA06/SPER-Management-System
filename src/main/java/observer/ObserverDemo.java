package observer;

import chainofresponsibility.PackageIssue;
import database.PackageIssueDAO;
import database.UserDAO;
import entity.User;

public class ObserverDemo {

    public static void run() {
        System.out.println("\nPHASE 5 - OBSERVER PATTERN");

        UserDAO userDAO = new UserDAO();
        PackageIssueDAO issueDAO = new PackageIssueDAO();

        PackageIssue issue = issueDAO.findById(1)
                .orElseGet(() -> new PackageIssue(
                        0, "PKG-DEMO", "Lost Package",
                        "Demo issue for observer pattern", "HIGH", 75000
                ));

        PackageIssueManager manager = new PackageIssueManager(issue);

        User ramesh = userDAO.findByName("Ramesh")
                .orElseThrow(() -> new RuntimeException("User 'Ramesh' not found in DB"));
        User sunderlal = userDAO.findByName("Sunderlal")
                .orElseThrow(() -> new RuntimeException("User 'Sunderlal' not found in DB"));
        User daya = userDAO.findByName("Daya")
                .orElseThrow(() -> new RuntimeException("User 'Daya' not found in DB"));

        System.out.println("\nTest 1: Attach observers");
        manager.attach(ramesh);
        manager.attach(sunderlal);
        manager.attach(daya);

        System.out.println("\nTest 2: Status changed to UNDER_INVESTIGATION");
        manager.updateStatus("UNDER_INVESTIGATION");

        System.out.println("\nTest 3: Status changed to ESCALATED");
        manager.updateStatus("ESCALATED");

        System.out.println("\nTest 4: Status changed to RESOLVED");
        manager.updateStatus("RESOLVED");

        System.out.println("\nTest 5: Detach Hub Manager, status changed to CLOSED");
        manager.detach(daya);
        manager.updateStatus("CLOSED");

        if (issue.getIssueId() > 0) {
            issueDAO.updateStatus(issue.getIssueId(), issue.getStatus());
        }
    }

    public static void main(String[] args) {
        run();
    }
}