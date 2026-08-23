package bridge;

public class EmailChannel implements DeliveryChannel {

    @Override
    public void send(String recipientContact, String subject, String message) {
        System.out.println("[EMAIL] To: " + recipientContact
                + " | Subject: " + subject
                + " | " + message);
    }
}
