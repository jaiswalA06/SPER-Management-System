
package strategy;

import chainofresponsibility.PackageIssue;
import java.util.List;

public class WrongAddressStrategy implements ResolutionStrategy {

    @Override
    public ResolutionPlan createPlan(PackageIssue issue) {
        String priority = ResolutionPriority.calculate(issue);

        List<String> actions = List.of(
                "Verify the address recorded for the shipment.",
                "Request corrected address details from the appropriate party.",
                "Check whether rerouting is possible.",
                "Record the address correction and delivery instructions."
        );

        return new ResolutionPlan(
                "Wrong Address Resolution",
                priority,
                "The shipment may have an incorrect delivery address.",
                actions
        );
    }
}
