
package strategy;

import chainofresponsibility.PackageIssue;
import java.util.ArrayList;
import java.util.List;

public class DelayedPackageStrategy implements ResolutionStrategy {

    @Override
    public ResolutionPlan createPlan(PackageIssue issue) {
        List<String> actions = new ArrayList<>();

        actions.add("Review the latest tracking information.");
        actions.add("Identify the last completed delivery milestone.");
        actions.add("Request an updated delivery estimate.");
        actions.add("Schedule a follow-up if the delay continues.");

        String priority = ResolutionPriority.calculate(issue);

        if (priority.equals("HIGH") || priority.equals("CRITICAL")) {
            actions.add("Escalate the delay for priority review.");
        }

        return new ResolutionPlan(
                "Delayed Package Resolution",
                priority,
                "The package has not arrived within the expected time.",
                actions
        );
    }
}
