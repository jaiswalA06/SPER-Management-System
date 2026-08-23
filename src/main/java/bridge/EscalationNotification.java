package bridge;

import entity.User;

public class EscalationNotification extends Notification {

    public EscalationNotification(DeliveryChannel channel) {
        super(channel);
    }

    @Override
    public void notify(User recipient, String trackingNumber) {
        String subject = "Issue Escalated";
        String message = "Hi " + recipient.getName()
                + ", the issue for package " + trackingNumber
                + " has been escalated to the next tier.";

        channel.send(recipient.getName(), subject, message);
    }
}
