package chainofresponsibility;

public class HubManager extends PackageIssueHandler {

    @Override
    public void handleIssue(PackageIssue issue) {

        System.out.println("\nHub Manager checking the issue...");

        if (issue.getSeverity().equalsIgnoreCase("MEDIUM")
                && issue.getPackageValue() <= 25000) {

            System.out.println("Hub Manager can handle this issue.");

            issue.setStatus("RESOLVED BY HUB MANAGER");

            System.out.println("Issue resolved by Hub Manager.");

        } else {

            System.out.println(
                    "Hub Manager cannot resolve this issue."
            );

            System.out.println("Escalating to Regional Manager...");

            passToNextHandler(issue);
        }
    }
}