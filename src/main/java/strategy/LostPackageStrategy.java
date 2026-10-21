
package strategy;

import chainofresponsibility.PackageIssue;
import java.util.ArrayList;
import java.util.List;

public class LostPackageStrategy implements ResolutionStrategy {

    @Override
    public ResolutionPlan createPlan(PackageIssue issue) {
        List<String> actions = new ArrayList<>();

        actions.add("Review the latest tracking scans.");
        actions.add("Create a package investigation task.");
        actions.add("Verify the last known location and delivery records.");
        actions.add("Review claim eligibility if the package is confirmed missing.");

        String priority = ResolutionPriority.calculate(issue);

        if (priority.equals("HIGH") || priority.equals("CRITICAL")) {
            actions.add("Escalate the investigation for priority review.");
        }

        return new ResolutionPlan(
                "Lost Package Investigation",
                priority,
                "The package has been reported as lost.",
                actions
        );
    }
}
