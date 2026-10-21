
package strategy;

import chainofresponsibility.PackageIssue;
import java.util.ArrayList;
import java.util.List;

public class DamagedPackageStrategy implements ResolutionStrategy {

    @Override
    public ResolutionPlan createPlan(PackageIssue issue) {
        List<String> actions = new ArrayList<>();

        actions.add("Request photographs or other evidence of the damage.");
        actions.add("Verify the package condition and handling records.");
        actions.add("Review replacement or compensation eligibility.");
        actions.add("Record the verification outcome.");

        String priority = ResolutionPriority.calculate(issue);

        if (priority.equals("HIGH") || priority.equals("CRITICAL")) {
            actions.add("Escalate the case for priority review.");
        }

        return new ResolutionPlan(
                "Damaged Package Resolution",
                priority,
                "The package was reported as damaged.",
                actions
        );
    }
}
