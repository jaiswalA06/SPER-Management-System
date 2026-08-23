package bridge;

import database.UserDAO;
import entity.User;

public class BridgeDemo {

    public static void run() {
        System.out.println("\nPHASE 4 - BRIDGE PATTERN");

        UserDAO userDAO = new UserDAO();

        User ramesh = userDAO.findByName("Ramesh")
                .orElseThrow(() -> new RuntimeException("User 'Ramesh' not found in DB"));
        User daya = userDAO.findByName("Daya")
                .orElseThrow(() -> new RuntimeException("User 'Daya' not found in DB"));

        System.out.println("\nTest 1: Creation notice by Email");
        Notification creation = new CreationNotification(new EmailChannel());
        creation.notify(ramesh, "PKG1001");

        System.out.println("\nTest 2: Escalation notice by SMS");
        Notification escalation = new EscalationNotification(new SMSChannel());
        escalation.notify(daya, "PKG1003");

        System.out.println("\nTest 3: Resolution notice by App push");
        Notification resolution = new ResolutionNotification(new MobileAppChannel());
        resolution.notify(ramesh, "PKG1002");

        System.out.println("\nTest 4: Same notification, channel switched at runtime");
        resolution.setChannel(new EmailChannel());
        resolution.notify(ramesh, "PKG1002");
    }

    public static void main(String[] args) {
        run();
    }
}