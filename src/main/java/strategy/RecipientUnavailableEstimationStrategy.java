
package strategy;

import chainofresponsibility.PackageIssue;

public class RecipientUnavailableEstimationStrategy
        implements ResolutionEstimationStrategy {

    @Override
    public ResolutionEstimate estimate(PackageIssue issue) {
        return new ResolutionEstimate(
                2,
                0.0,
                "No automatic refund; redelivery or applicable fees require policy review."
        );
    }
}
