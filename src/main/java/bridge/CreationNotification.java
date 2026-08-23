package bridge;

import entity.User;

public class CreationNotification extends Notification {

    public CreationNotification(DeliveryChannel channel) {
        super(channel);
    }

    @Override
    public void notify(User recipient, String trackingNumber) {
        String subject = "New Issue Reported";
        String message = "Hi " + recipient.getName()
                + ", your issue for package " + trackingNumber
                + " has been created and is now being tracked.";

        channel.send(recipient.getName(), subject, message);
    }
}
