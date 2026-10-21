
package strategy;

import chainofresponsibility.PackageIssue;
import java.util.List;

public class RecipientUnavailableStrategy implements ResolutionStrategy {

    @Override
    public ResolutionPlan createPlan(PackageIssue issue) {
        String priority = ResolutionPriority.calculate(issue);

        List<String> actions = List.of(
                "Review the previous delivery attempt.",
                "Confirm the recipient's availability.",
                "Arrange redelivery where permitted.",
                "Update the delivery record after the next attempt."
        );

        return new ResolutionPlan(
                "Recipient Unavailable Resolution",
                priority,
                "Delivery could not be completed because the recipient was unavailable.",
                actions
        );
    }
}
