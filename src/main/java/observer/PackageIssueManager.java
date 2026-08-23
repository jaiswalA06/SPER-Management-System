package observer;

import chainofresponsibility.PackageIssue;
import java.util.ArrayList;
import java.util.List;

// Observer Pattern — Concrete Subject. Manages a PackageIssue and notifies all observers on every status change.
public class PackageIssueManager implements Subject {

    private final PackageIssue issue;
    private final List<Observer> observers = new ArrayList<>();

    public PackageIssueManager(PackageIssue issue) {
        this.issue = issue;
    }

    @Override
    public void attach(Observer observer) {
        observers.add(observer);
        System.out.println("[PackageIssueManager] Observer attached: " + observer);
    }

    @Override
    public void detach(Observer observer) {
        observers.remove(observer);
        System.out.println("[PackageIssueManager] Observer detached: " + observer);
    }

    @Override
    public void notifyObservers() {
        System.out.println("[PackageIssueManager] Notifying " + observers.size() + " observer(s)...");
        for (Observer observer : observers) {
            observer.update(issue);
        }
    }

    // Updates the issue status and automatically notifies all registered observers
    public void updateStatus(String newStatus) {
        String oldStatus = issue.getStatus();
        issue.setStatus(newStatus);
        System.out.println("\n[PackageIssueManager] Issue #" + issue.getIssueId()
                + " status: [" + oldStatus + "] → [" + newStatus + "]");
        notifyObservers();
    }

    public PackageIssue getIssue() {
        return issue;
    }
}
