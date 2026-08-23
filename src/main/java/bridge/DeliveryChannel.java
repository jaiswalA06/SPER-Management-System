package bridge;

public interface DeliveryChannel {
    void send(String recipientContact, String subject, String message);
}
