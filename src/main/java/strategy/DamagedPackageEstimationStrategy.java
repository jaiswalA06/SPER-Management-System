
package strategy;

import chainofresponsibility.PackageIssue;

public class DamagedPackageEstimationStrategy
        implements ResolutionEstimationStrategy {

    @Override
    public ResolutionEstimate estimate(PackageIssue issue) {
        double refund = issue.getPackageValue() * 0.50;

        return new ResolutionEstimate(
                2,
                refund,
                "Illustrative 50% compensation for a damaged package."
        );
    }
}
