package chainofresponsibility;

public class SupportAgent extends PackageIssueHandler {

    @Override
    public void handleIssue(PackageIssue issue) {

        System.out.println("\nSupport Agent checking the issue...");

        if (issue.getSeverity().equalsIgnoreCase("LOW")
                && issue.getPackageValue() <= 5000) {

            System.out.println("Support Agent can handle this issue.");

            issue.setStatus("RESOLVED BY SUPPORT AGENT");

            System.out.println("Issue resolved by Support Agent.");

        } else {

            System.out.println(
                    "Support Agent cannot resolve this issue."
            );

            System.out.println("Escalating to Hub Manager...");

            passToNextHandler(issue);
        }
    }
}