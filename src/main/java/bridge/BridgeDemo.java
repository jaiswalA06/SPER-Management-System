package bridge;

import entity.User;
import enums.Role;

public class BridgeDemo {

    public static void run() {
        System.out.println("\nPHASE 4 - BRIDGE PATTERN");

        User alice = new User(2, "alice", Role.CUSTOMER, "alicepwd");
        User diana = new User(5, "diana", Role.HUB_MANAGER, "dianapwd");

        System.out.println("\nTest 1: Creation notice by Email");
        Notification creation = new CreationNotification(new EmailChannel());
        creation.notify(alice, "PKG1001");

        System.out.println("\nTest 2: Escalation notice by SMS");
        Notification escalation = new EscalationNotification(new SMSChannel());
        escalation.notify(diana, "PKG1003");

        System.out.println("\nTest 3: Resolution notice by App push");
        Notification resolution = new ResolutionNotification(new MobileAppChannel());
        resolution.notify(alice, "PKG1002");

        System.out.println("\nTest 4: Same notification, channel switched at runtime");
        resolution.setChannel(new EmailChannel());
        resolution.notify(alice, "PKG1002");
    }

    public static void main(String[] args) {
        run();
    }
}
