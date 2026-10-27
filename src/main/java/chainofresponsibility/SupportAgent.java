
package chainofresponsibility;

public class SupportAgent extends PackageIssueHandler {

    @Override
    public void handleIssue(PackageIssue issue) {
        System.out.println("\n[COR] Support Agent checking the issue...");

        if ("LOW".equalsIgnoreCase(issue.getSeverity())
                && issue.getPackageValue() <= 5000) {

            issue.setHandledBy("Sunderlal (SUPPORT_STAFF)");
            System.out.println(
                    "[COR] Support Agent accepts responsibility."
            );

        } else {
            System.out.println(
                    "[COR] Support Agent cannot handle this issue."
            );
            System.out.println("[COR] Escalating to Hub Manager...");
            passToNextHandler(issue);
        }
    }
}
