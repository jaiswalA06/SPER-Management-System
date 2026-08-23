package bridge;

public class SMSChannel implements DeliveryChannel {

    @Override
    public void send(String recipientContact, String subject, String message) {
        // SMS has no separate subject line, so fold it into the text
        System.out.println("[SMS] To: " + recipientContact
                + " | " + subject + ": " + message);
    }
}
