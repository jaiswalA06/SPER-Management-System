package chainofresponsibility;

import enums.IssueSeverity;
import enums.IssueType;

import java.util.ArrayList;
import java.util.List;

public class PackageIssueBuilder {

    public static final String TRACKING_NUMBER_PATTERN = "^PKG\\d{4,8}$";

    private String trackingNumber;
    private IssueType issueType;
    private String description;
    private IssueSeverity severity = IssueSeverity.LOW;
    private double packageValue = 0.0;
    private int customerId;

    PackageIssueBuilder() {
    }

    public static boolean isValidTrackingNumber(String trackingNumber) {
        if (trackingNumber == null) return false;
        return trackingNumber.trim().toUpperCase().matches(TRACKING_NUMBER_PATTERN);
    }

    public PackageIssueBuilder trackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber == null ? null : trackingNumber.trim().toUpperCase();
        return this;
    }

    public PackageIssueBuilder issueType(IssueType issueType) {
        this.issueType = issueType;
        return this;
    }

    public PackageIssueBuilder description(String description) {
        this.description = description == null ? null : description.trim();
        return this;
    }

    public PackageIssueBuilder severity(IssueSeverity severity) {
        this.severity = severity;
        return this;
    }

    public PackageIssueBuilder packageValue(double packageValue) {
        this.packageValue = packageValue;
        return this;
    }

    public PackageIssueBuilder customerId(int customerId) {
        this.customerId = customerId;
        return this;
    }

    public PackageIssue build() {
        List<String> errors = new ArrayList<>();

        if (!isValidTrackingNumber(trackingNumber)) {
            errors.add("Tracking number must start with 'PKG' followed by 4 to 8 digits");
        }
        if (issueType == null) {
            errors.add("Issue type is required");
        }
        if (description == null || description.isBlank()) {
            errors.add("Description is required");
        }
        if (severity == null) {
            errors.add("Severity is required");
        }
        if (Double.isNaN(packageValue) || Double.isInfinite(packageValue) || packageValue < 0) {
            errors.add("Package value must be a non-negative number");
        }
        if (customerId <= 0) {
            errors.add("Customer ID must be positive");
        }

        if (!errors.isEmpty()) {
            throw new IllegalStateException(String.join("; ", errors));
        }

        return new PackageIssue(this);
    }

    String getTrackingNumber() {
        return trackingNumber;
    }

    IssueType getIssueType() {
        return issueType;
    }

    String getDescription() {
        return description;
    }

    IssueSeverity getSeverity() {
        return severity;
    }

    double getPackageValue() {
        return packageValue;
    }

    int getCustomerId() {
        return customerId;
    }
}
