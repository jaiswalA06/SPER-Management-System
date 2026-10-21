
package strategy;

import chainofresponsibility.PackageIssue;

public class DelayedPackageEstimationStrategy
        implements ResolutionEstimationStrategy {

    @Override
    public ResolutionEstimate estimate(PackageIssue issue) {
        double refund = issue.getPackageValue() * 0.10;

        return new ResolutionEstimate(
                1,
                refund,
                "Illustrative delay compensation, subject to delivery policy."
        );
    }
}
