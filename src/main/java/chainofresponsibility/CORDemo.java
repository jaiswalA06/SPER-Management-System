package chainofresponsibility;

public class CORDemo {

    public static void main(String[] args) {

        SupportAgent supportAgent = new SupportAgent();
        HubManager hubManager = new HubManager();
        RegionalManager regionalManager = new RegionalManager();

        supportAgent.setNextHandler(hubManager);
        hubManager.setNextHandler(regionalManager);

        System.out.println("==========================================");
        System.out.println("   PACKAGE ISSUE RESOLUTION SYSTEM");
        System.out.println("==========================================");

        PackageIssue issue1 = new PackageIssue(
                101,
                "PKG1001",
                "Delayed Package",
                "Package delayed by one day",
                "LOW",
                2000
        );

        System.out.println("\n----------- TEST CASE 1 -----------");
        displayIssue(issue1);

        supportAgent.handleIssue(issue1);

        System.out.println("Final Status: " + issue1.getStatus());

        PackageIssue issue2 = new PackageIssue(
                102,
                "PKG1002",
                "Damaged Package",
                "Package damaged during delivery",
                "MEDIUM",
                15000
        );

        System.out.println("\n----------- TEST CASE 2 -----------");
        displayIssue(issue2);

        supportAgent.handleIssue(issue2);

        System.out.println("Final Status: " + issue2.getStatus());


        // TEST CASE 3
        // HIGH severity + high package value
        PackageIssue issue3 = new PackageIssue(
                103,
                "PKG1003",
                "Lost Package",
                "High-value package is missing",
                "HIGH",
                50000
        );

        System.out.println("\n----------- TEST CASE 3 -----------");
        displayIssue(issue3);

        supportAgent.handleIssue(issue3);

        System.out.println("Final Status: " + issue3.getStatus());

        PackageIssue issue4 = new PackageIssue(
                104,
                "PKG1004",
                "Damaged Package",
                "Expensive package was damaged",
                "LOW",
                40000
        );

        System.out.println("\n----------- TEST CASE 4 -----------");
        displayIssue(issue4);

        supportAgent.handleIssue(issue4);

        System.out.println("Final Status: " + issue4.getStatus());
    }

    public static void displayIssue(PackageIssue issue) {

        System.out.println("Issue ID      : " + issue.getIssueId());
        System.out.println("Tracking No.  : " + issue.getTrackingNumber());
        System.out.println("Issue Type    : " + issue.getIssueType());
        System.out.println("Severity      : " + issue.getSeverity());
        System.out.println("Package Value : ₹" + issue.getPackageValue());
        System.out.println("Initial Status: " + issue.getStatus());
    }
}
