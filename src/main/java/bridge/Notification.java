package bridge;

import entity.User;

public abstract class Notification {

    // This reference is the actual "bridge" between the abstraction
    // (what kind of message) and the implementor (how it's delivered)
    protected DeliveryChannel channel;

    public Notification(DeliveryChannel channel) {
        this.channel = channel;
    }

    // Lets a stakeholder's preferred channel change at runtime
    // without touching the notification type
    public void setChannel(DeliveryChannel channel) {
        this.channel = channel;
    }

    public abstract void notify(User recipient, String trackingNumber);
}
