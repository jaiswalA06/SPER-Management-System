
package strategy;

import chainofresponsibility.PackageIssue;

public class WrongPackageEstimationStrategy
        implements ResolutionEstimationStrategy {

    @Override
    public ResolutionEstimate estimate(PackageIssue issue) {
        double refund = issue.getPackageValue();

        return new ResolutionEstimate(
                3,
                refund,
                "Full-value refund estimate for an incorrectly delivered package."
        );
    }
}
