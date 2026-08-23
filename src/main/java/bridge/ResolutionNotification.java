package bridge;

import entity.User;

public class ResolutionNotification extends Notification {

    public ResolutionNotification(DeliveryChannel channel) {
        super(channel);
    }

    @Override
    public void notify(User recipient, String trackingNumber) {
        String subject = "Issue Resolved";
        String message = "Hi " + recipient.getName()
                + ", the issue for package " + trackingNumber
                + " has been marked as resolved. Thank you for your patience.";

        channel.send(recipient.getName(), subject, message);
    }
}
