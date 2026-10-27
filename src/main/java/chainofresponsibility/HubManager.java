
package chainofresponsibility;

public class HubManager extends PackageIssueHandler {

    @Override
    public void handleIssue(PackageIssue issue) {
        System.out.println("\n[COR] Hub Manager checking the issue...");

        if ("MEDIUM".equalsIgnoreCase(issue.getSeverity())
                && issue.getPackageValue() <= 25000) {

            issue.setHandledBy("Daya (HUB_MANAGER)");
            System.out.println(
                    "[COR] Hub Manager accepts responsibility."
            );

        } else {
            System.out.println(
                    "[COR] Hub Manager cannot handle this issue."
            );
            System.out.println(
                    "[COR] Escalating to Regional Manager..."
            );
            passToNextHandler(issue);
        }
    }
}
