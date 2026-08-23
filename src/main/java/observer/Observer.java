package observer;

import chainofresponsibility.PackageIssue;

// Observer Pattern — Stakeholders implement this to receive status update notifications
public interface Observer {
    void update(PackageIssue issue);
}
