package observer;

// Observer Pattern — Subject defines the publish/subscribe contract
public interface Subject {
    void attach(Observer observer);
    void detach(Observer observer);
    void notifyObservers();
}
