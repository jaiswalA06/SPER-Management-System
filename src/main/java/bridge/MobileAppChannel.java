package bridge;

public class MobileAppChannel implements DeliveryChannel {

    @Override
    public void send(String recipientContact, String subject, String message) {
        System.out.println("[APP PUSH] To: " + recipientContact
                + " | " + subject + " - " + message);
    }
}
