
package strategy;

import chainofresponsibility.PackageIssue;
import java.util.List;

public class WrongPackageStrategy implements ResolutionStrategy {

    @Override
    public ResolutionPlan createPlan(PackageIssue issue) {
        String priority = ResolutionPriority.calculate(issue);

        List<String> actions = List.of(
                "Verify the shipment tracking number and package label.",
                "Compare the package with the original order details.",
                "Review return or exchange eligibility.",
                "Coordinate delivery of the correct package."
        );

        return new ResolutionPlan(
                "Wrong Package Resolution",
                priority,
                "The recipient may have received an incorrect package.",
                actions
        );
    }
}
