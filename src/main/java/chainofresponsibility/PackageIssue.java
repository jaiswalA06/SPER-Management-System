package chainofresponsibility;

import java.time.LocalDateTime;

public class PackageIssue {

    private int issueId;
    private String trackingNumber;
    private String issueType;
    private String description;
    private String severity;
    private double packageValue;
    private String status;
    private int customerId;

    public PackageIssue(int issueId,String trackingNumber,String issueType,String description,String severity,double packageValue) {
        this.issueId = issueId;
        this.trackingNumber = trackingNumber;
        this.issueType = issueType;
        this.description = description;
        this.severity = severity;
        this.packageValue = packageValue;
        this.status = "REPORTED";
    }

    PackageIssue(PackageIssueBuilder builder) {
        this(0,
                builder.getTrackingNumber(),
                builder.getIssueType().name(),
                builder.getDescription(),
                builder.getSeverity().name(),
                builder.getPackageValue());
        this.customerId = builder.getCustomerId();
    }

    public static PackageIssueBuilder builder() {
        return new PackageIssueBuilder();
    }

    public int getIssueId() {
        return issueId;
    }

    public void setIssueId(int issueId) {
        this.issueId = issueId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public String getIssueType() {
        return issueType;
    }

    public String getDescription() {
        return description;
    }

    public String getSeverity() {
        return severity;
    }

    public double getPackageValue() {
        return packageValue;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // Memento Pattern — Originator: captures the current status as a snapshot
    public Memento save() {
        return new Memento(issueId, status);
    }

    // Memento Pattern — Originator: rolls the status back to a saved snapshot
    public void restore(Memento memento) {
        if (memento.issueId != issueId) {
            throw new IllegalArgumentException("Snapshot belongs to Issue #" + memento.issueId);
        }
        this.status = memento.status;
    }

    // Memento Pattern — Memento: opaque, immutable snapshot. Only PackageIssue can create it or read its state;
    // the caretaker can only store it and see when it was taken.
    public static final class Memento {

        private final int issueId;
        private final String status;
        private final LocalDateTime savedAt;

        private Memento(int issueId, String status) {
            this.issueId = issueId;
            this.status = status;
            this.savedAt = LocalDateTime.now();
        }

        public LocalDateTime getSavedAt() {
            return savedAt;
        }
    }
}