package prototype;

import chainofresponsibility.PackageIssue;
import enums.IssueSeverity;

// Standalone demonstration of the Prototype Pattern. It needs no database or login.
public class PrototypeDemo {

    public static void main(String[] args) {

        System.out.println("========================================");
        System.out.println("      SPER PROTOTYPE PATTERN DEMO");
        System.out.println("========================================");

        IssueTemplateRegistry registry = new IssueTemplateRegistry();
        System.out.println("Templates in registry : " + registry.size());

        // 1. Clone the same template for three parcels in one damaged shipment
        System.out.println("\n--- 1. Clone a template for each parcel ---");
        String[] trackingNumbers = {"PKG1001", "PKG1002", "PKG1003"};
        for (String trackingNumber : trackingNumbers) {
            IssueTemplate copy = registry.get("DAMAGED").orElseThrow();
            PackageIssue issue = copy.createIssue(trackingNumber, 2);
            System.out.printf("%s | %s | %s | %s%n",
                    issue.getTrackingNumber(), issue.getIssueType(), issue.getSeverity(), issue.getStatus());
        }

        // 2. Customise one clone and prove the original is not affected
        System.out.println("\n--- 2. Customising a clone leaves the original unchanged ---");
        IssueTemplate original = registry.get("LOST").orElseThrow();
        IssueTemplate custom = original.clone()
                .withDescription("Parcel scanned at hub but never left the warehouse")
                .withSeverity(IssueSeverity.CRITICAL)
                .withPackageValue(45000.0);

        System.out.println("Original : " + original);
        System.out.println("Clone    : " + custom);
        System.out.println("Same object?          " + (original == custom));
        System.out.println("Same class?           " + (original.getClass() == custom.getClass()));
        System.out.println("Registry still holds  : " + registry.get("LOST").orElseThrow());

        // 3. Build a prototype from an issue that already exists
        System.out.println("\n--- 3. Prototype built from an existing issue ---");
        PackageIssue existing = new PackageIssue(7, "PKG2001", "Wrong Address",
                "Delivered to the neighbouring building", "MEDIUM", 8000.0);
        IssueTemplate fromExisting = IssueTemplate.fromIssue(existing);
        System.out.println("Source issue : #" + existing.getIssueId() + " (" + existing.getIssueType() + ")");
        System.out.println("Prototype    : " + fromExisting);

        PackageIssue duplicate = fromExisting.clone().createIssue("PKG2002", 2);
        System.out.printf("Duplicate    : %s | %s | %s | status %s | new issue id %d%n",
                duplicate.getTrackingNumber(), duplicate.getIssueType(), duplicate.getSeverity(),
                duplicate.getStatus(), duplicate.getIssueId());

        System.out.println("========================================");
    }
}
