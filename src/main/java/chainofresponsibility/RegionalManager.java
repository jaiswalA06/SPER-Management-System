package chainofresponsibility;

public class RegionalManager extends PackageIssueHandler {

    @Override
    public void handleIssue(PackageIssue issue) {

        System.out.println("\nRegional Manager checking the issue...");

        System.out.println("Regional Manager will handle this issue.");

        issue.setStatus("RESOLVED BY REGIONAL MANAGER");

        System.out.println("Issue resolved by Regional Manager.");
    }
}