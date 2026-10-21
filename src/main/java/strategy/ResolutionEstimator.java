
package strategy;

import chainofresponsibility.PackageIssue;

public class ResolutionEstimator {

    private final AutomaticEstimationSelector selector =
            new AutomaticEstimationSelector();

    public ResolutionEstimate estimate(PackageIssue issue) {
        ResolutionEstimationStrategy strategy =
                selector.selectStrategy(issue);

        return strategy.estimate(issue);
    }

    public String getSelectedStrategyName(PackageIssue issue) {
        return selector.selectStrategy(issue).getClass().getSimpleName();
    }
}
