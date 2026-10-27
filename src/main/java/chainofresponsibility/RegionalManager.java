
package chainofresponsibility;

public class RegionalManager extends PackageIssueHandler {

    @Override
    public void handleIssue(PackageIssue issue) {
        System.out.println(
                "\n[COR] Regional Manager checking the issue..."
        );

        issue.setHandledBy("Venkatesh (REGIONAL_MANAGER)");

        System.out.println(
                "[COR] Regional Manager accepts responsibility."
        );
    }
}
