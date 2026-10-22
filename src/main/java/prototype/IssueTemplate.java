package prototype;

import chainofresponsibility.PackageIssue;
import enums.IssueSeverity;
import enums.IssueType;

// Prototype Pattern — Concrete Prototype: a reusable blueprint of a package issue
// (type, description, severity, package value). It holds no tracking number, customer, id or status,
// because those belong to one specific issue and are supplied when the blueprint is turned into an issue.
public class IssueTemplate implements IssuePrototype {

    private String templateName;
    private IssueType issueType;
    private String description;
    private IssueSeverity severity;
    private double packageValue;

    public IssueTemplate(String templateName, IssueType issueType, String description,
                         IssueSeverity severity, double packageValue) {
        this.templateName = templateName;
        this.issueType = issueType;
        this.description = description;
        this.severity = severity;
        this.packageValue = packageValue;
    }

    // Builds a blueprint from an issue that already exists, so it can be duplicated for similar packages.
    // Id, tracking number, customer and status are deliberately not copied: a duplicate is a brand-new issue.
    public static IssueTemplate fromIssue(PackageIssue source) {
        String description = source.getDescription();
        if (description == null || description.isBlank()) {
            description = "Duplicate of issue #" + source.getIssueId();
        }
        return new IssueTemplate(
                "Copy of Issue #" + source.getIssueId(),
                parseIssueType(source.getIssueType()),
                description,
                parseSeverity(source.getSeverity()),
                source.getPackageValue());
    }

    // Shallow copy via Object.clone(). Every field is immutable (String, enum, double),
    // so the copy is fully independent and a deep copy is not needed.
    @Override
    public IssueTemplate clone() {
        try {
            return (IssueTemplate) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError("IssueTemplate implements Cloneable", e);
        }
    }

    @Override
    public PackageIssue createIssue(String trackingNumber, int customerId) {
        // The builder validates everything and throws IllegalStateException if something is wrong
        return PackageIssue.builder()
                .trackingNumber(trackingNumber)
                .issueType(issueType)
                .description(description)
                .severity(severity)
                .packageValue(packageValue)
                .customerId(customerId)
                .build();
    }

    // Customisers: used on a clone to change only what differs from the blueprint
    public IssueTemplate withName(String templateName) {
        this.templateName = templateName;
        return this;
    }

    public IssueTemplate withDescription(String description) {
        this.description = description;
        return this;
    }

    public IssueTemplate withSeverity(IssueSeverity severity) {
        this.severity = severity;
        return this;
    }

    public IssueTemplate withPackageValue(double packageValue) {
        this.packageValue = packageValue;
        return this;
    }

    public String getTemplateName() {
        return templateName;
    }

    public IssueType getIssueType() {
        return issueType;
    }

    public String getDescription() {
        return description;
    }

    public IssueSeverity getSeverity() {
        return severity;
    }

    public double getPackageValue() {
        return packageValue;
    }

    // The database stores the type as text and older rows may use labels such as "Lost Package"
    private static IssueType parseIssueType(String raw) {
        String normalized = raw == null ? "" : raw.trim().toUpperCase().replaceAll("\\s+", "_");
        try {
            return IssueType.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            if (normalized.endsWith("_PACKAGE")) {
                String withoutSuffix = normalized.substring(0, normalized.length() - "_PACKAGE".length());
                try {
                    return IssueType.valueOf(withoutSuffix);
                } catch (IllegalArgumentException ignored) {
                    // fall through to the error below
                }
            }
            throw new IllegalArgumentException("Unrecognised issue type: " + raw);
        }
    }

    private static IssueSeverity parseSeverity(String raw) {
        try {
            return IssueSeverity.valueOf(raw == null ? "" : raw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unrecognised severity: " + raw);
        }
    }

    @Override
    public String toString() {
        return String.format("%s | Type: %s | Severity: %s | Value: INR %.2f | \"%s\"",
                templateName, issueType, severity, packageValue, description);
    }
}
